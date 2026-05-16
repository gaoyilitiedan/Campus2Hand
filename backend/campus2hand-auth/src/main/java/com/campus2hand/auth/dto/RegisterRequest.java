package com.campus2hand.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{8,12}$", message = "学号格式不正确")
    private String studentId;

    @NotBlank(message = "校园卡密码不能为空")
    private String campusPassword;

    @NotBlank(message = "平台密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度8-32字符")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码需包含大小写字母和数字")
    private String platformPassword;

    @NotBlank(message = "校园邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String campusEmail;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}