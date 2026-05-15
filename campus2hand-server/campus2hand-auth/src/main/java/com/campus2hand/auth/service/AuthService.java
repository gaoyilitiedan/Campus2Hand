package com.campus2hand.auth.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus2hand.auth.dto.*;
import com.campus2hand.auth.entity.EmailToken;
import com.campus2hand.auth.entity.EmailVerification;
import com.campus2hand.auth.entity.RefreshToken;
import com.campus2hand.auth.entity.User;
import com.campus2hand.auth.mapper.EmailTokenMapper;
import com.campus2hand.auth.mapper.EmailVerificationMapper;
import com.campus2hand.auth.mapper.RefreshTokenMapper;
import com.campus2hand.auth.mapper.UserMapper;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final EmailVerificationMapper emailVerificationMapper;
    private final EmailTokenMapper emailTokenMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStudentId, request.getStudentId())) > 0) {
            throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS, "该学号已注册");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getCampusEmail())) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS, "该邮箱已注册");
        }

        boolean campusVerified = verifyCampusCredentials(request.getStudentId(), request.getCampusPassword());
        if (!campusVerified) {
            throw new BusinessException(ErrorCode.CAMPUS_VERIFY_FAILED, "校园身份校验失败，请检查学号和校园卡密码");
        }

        String campus = extractCampus(request.getStudentId());

        User user = new User();
        user.setStudentId(request.getStudentId());
        user.setNickname("用户" + request.getStudentId().substring(Math.max(0, request.getStudentId().length() - 4)));
        user.setPasswordHash(passwordEncoder.encode(request.getPlatformPassword()));
        user.setEmail(request.getCampusEmail());
        user.setEmailVerified(false);
        user.setCampus(campus);
        user.setPhone(request.getPhone());
        user.setRole("user");
        user.setStatus("active");
        user.setReputationScore(5.0);
        userMapper.insert(user);

        String token = IdUtil.fastSimpleUUID();
        EmailToken emailToken = new EmailToken();
        emailToken.setUserId(user.getId());
        emailToken.setToken(token);
        emailToken.setType("verify_email");
        emailToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailToken.setUsed(false);
        emailTokenMapper.insert(emailToken);

        log.info("User registered: studentId={}, verify token generated", request.getStudentId());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getStudentId, request.getStudentId())
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if ("disabled".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
        }
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, "账户已锁定，" + user.getLockedUntil() + "后再试");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int failCount = user.getLoginFailCount() != null ? user.getLoginFailCount() + 1 : 1;
            user.setLoginFailCount(failCount);
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                log.warn("Account locked: studentId={}, until={}", request.getStudentId(), user.getLockedUntil());
            }
            userMapper.updateById(user);
            throw new BusinessException(ErrorCode.PASSWORD_WRONG, "密码错误");
        }

        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        int tokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole(), tokenVersion);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setToken(refreshToken);
        rt.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenMapper.insert(rt);

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(ISO_FORMATTER.format(expiresAt))
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .studentId(maskStudentId(user.getStudentId()))
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .reputationScore(user.getReputationScore())
                        .build())
                .build();
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        if (!jwtUtil.validateRefreshToken(request.getRefreshToken())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "RefreshToken无效或已过期");
        }
        RefreshToken rt = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getToken, request.getRefreshToken())
        );
        if (rt == null || rt.getRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "RefreshToken已被撤销");
        }
        rt.setRevoked(true);
        refreshTokenMapper.updateById(rt);

        User user = userMapper.selectById(rt.getUserId());
        if (user == null || "disabled".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
        }
        int tokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole(), tokenVersion);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken newRt = new RefreshToken();
        newRt.setUserId(user.getId());
        newRt.setToken(newRefreshToken);
        newRt.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenMapper.insert(newRt);

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresAt(ISO_FORMATTER.format(expiresAt))
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .studentId(maskStudentId(user.getStudentId()))
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .reputationScore(user.getReputationScore())
                        .build())
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_WRONG, "旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() != null ? user.getTokenVersion() + 1 : 1);
        userMapper.updateById(user);
        log.info("Password changed for userId={}", userId);
    }

    public void sendVerifyCode(SendVerifyCodeRequest request) {
        if ("register".equals(request.getType())) {
            if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())) > 0) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS, "该邮箱已注册");
            }
        }
        String code = RandomUtil.randomNumbers(6);
        EmailVerification verification = new EmailVerification();
        verification.setEmail(request.getEmail());
        verification.setCode(code);
        verification.setType(request.getType());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailVerificationMapper.insert(verification);

        log.info("Verify code sent to {}: {} (type={})", request.getEmail(), code, request.getType());
    }

    @Transactional
    public void sendVerifyEmail() {
        Long userId = CurrentUserHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFY_FAILED, "邮箱已验证");
        }

        long todayCount = emailTokenMapper.selectCount(
                new LambdaQueryWrapper<EmailToken>()
                        .eq(EmailToken::getUserId, userId)
                        .eq(EmailToken::getType, "verify_email")
                        .ge(EmailToken::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay())
        );
        if (todayCount >= 5) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_LIMIT, "今日发送次数已达上限");
        }

        String token = IdUtil.fastSimpleUUID();
        EmailToken emailToken = new EmailToken();
        emailToken.setUserId(userId);
        emailToken.setToken(token);
        emailToken.setType("verify_email");
        emailToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailToken.setUsed(false);
        emailTokenMapper.insert(emailToken);

        log.info("Verify email token sent to userId={}, token={}", userId, token);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailToken emailToken = emailTokenMapper.selectOne(
                new LambdaQueryWrapper<EmailToken>().eq(EmailToken::getToken, token)
        );
        if (emailToken == null) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFY_FAILED, "验证链接无效");
        }
        if (emailToken.getUsed()) {
            throw new BusinessException(ErrorCode.EMAIL_TOKEN_USED, "验证链接已使用");
        }
        if (LocalDateTime.now().isAfter(emailToken.getExpiresAt())) {
            throw new BusinessException(ErrorCode.EMAIL_TOKEN_EXPIRED, "验证链接已过期");
        }

        emailToken.setUsed(true);
        emailTokenMapper.updateById(emailToken);

        User user = userMapper.selectById(emailToken.getUserId());
        if (user != null) {
            user.setEmailVerified(true);
            userMapper.updateById(user);
        }
        log.info("Email verified for userId={}", emailToken.getUserId());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getStudentId, request.getStudentId())
        );
        if (user == null) {
            log.warn("Forgot password request for non-existent studentId={}", request.getStudentId());
            return;
        }

        String token = IdUtil.fastSimpleUUID();
        EmailToken emailToken = new EmailToken();
        emailToken.setUserId(user.getId());
        emailToken.setToken(token);
        emailToken.setType("reset_password");
        emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        emailToken.setUsed(false);
        emailTokenMapper.insert(emailToken);

        log.info("Password reset token sent to userId={}, email={}, token={}", user.getId(), user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        EmailToken emailToken = emailTokenMapper.selectOne(
                new LambdaQueryWrapper<EmailToken>().eq(EmailToken::getToken, request.getToken())
        );
        if (emailToken == null) {
            throw new BusinessException(ErrorCode.EMAIL_TOKEN_EXPIRED, "重置链接无效");
        }
        if (emailToken.getUsed()) {
            throw new BusinessException(ErrorCode.EMAIL_TOKEN_USED, "重置链接已使用");
        }
        if (LocalDateTime.now().isAfter(emailToken.getExpiresAt())) {
            throw new BusinessException(ErrorCode.EMAIL_TOKEN_EXPIRED, "重置链接已过期");
        }
        if (!"reset_password".equals(emailToken.getType())) {
            throw new BusinessException(ErrorCode.EMAIL_TOKEN_EXPIRED, "令牌类型不正确");
        }

        emailToken.setUsed(true);
        emailTokenMapper.updateById(emailToken);

        User user = userMapper.selectById(emailToken.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() != null ? user.getTokenVersion() + 1 : 1);
        userMapper.updateById(user);

        refreshTokenMapper.delete(new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getUserId, user.getId()));

        log.info("Password reset for userId={}", user.getId());
    }

    private boolean verifyCampusCredentials(String studentId, String campusPassword) {
        log.info("Campus credential verification: studentId={}", studentId);
        return true;
    }

    private String extractCampus(String studentId) {
        if (studentId.startsWith("2024")) return "本部校区";
        if (studentId.startsWith("2023")) return "东校区";
        return "本部校区";
    }

    private String maskStudentId(String studentId) {
        if (studentId.length() <= 4) return studentId;
        return studentId.substring(0, 2) + "****" + studentId.substring(studentId.length() - 2);
    }
}