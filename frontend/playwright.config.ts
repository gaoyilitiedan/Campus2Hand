import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : 4,
  reporter: [['html', { open: 'never' }], ['list']],
  timeout: 30000,
  expect: {
    timeout: 10000
  },
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  globalSetup: './tests/global-setup.ts',
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        executablePath: 'C:\\Users\\27355\\AppData\\Local\\ms-playwright\\chromium-1223\\chrome-win64\\chrome.exe',
        headless: true,
        storageState: 'tests/.auth/user.json'
      },
      testMatch: /.*\.spec\.ts/,
      testIgnore: 'tests/auth.spec.ts'
    },
    {
      name: 'chromium-auth',
      use: {
        ...devices['Desktop Chrome'],
        executablePath: 'C:\\Users\\27355\\AppData\\Local\\ms-playwright\\chromium-1223\\chrome-win64\\chrome.exe',
        headless: true
      },
      testMatch: 'tests/auth.spec.ts'
    }
  ],
  webServer: {
    command: 'powershell -ExecutionPolicy Bypass -Command "npx vite --port 3000"',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 30000
  }
})