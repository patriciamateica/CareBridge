import { test, expect } from '@playwright/test';

test.describe('Authentication & Access Control', () => {

  test('Successful login redirects to the Patient Roster', async ({ page }) => {
    await page.goto('http://localhost:4200/user-login');

    await page.getByPlaceholder('Enter your email').fill('nurse.john@carebridge.com');
    await page.locator('input[placeholder="Enter your password"]').fill('Test1234!');
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page).toHaveURL('http://localhost:4200/dashboard/home-nurse');
  });
});
