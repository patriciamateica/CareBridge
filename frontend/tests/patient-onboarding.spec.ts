import { test, expect } from '@playwright/test';

async function loginAndGoToRoster(page: any) {
  await page.goto('http://localhost:4200/user-login');
  await page.getByPlaceholder('Enter your email').fill('nurse.john@carebridge.com');
  await page.locator('input[placeholder="Enter your password"]').fill('Test1234!');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.waitForURL('http://localhost:4200/dashboard/home-nurse');
  await page.goto('http://localhost:4200/dashboard/patient-management');
}

test.describe('End-to-End Patient Onboarding', () => {

  test('Successfully creates a patient', async ({ page }) => {
    await loginAndGoToRoster(page);

    await page.getByRole('button', { name: '+ Add Patient' }).click();

    const modalHeader = page.getByText('Add Patient to Roster');
    await expect(modalHeader).toBeVisible({ timeout: 10000 });

    await page.getByPlaceholder('First name').fill('Jane');
    await page.getByPlaceholder('Last name').fill('Doe');
    await page.getByPlaceholder('e.g. Hypertension').fill('Acute Asthma');
    await page.getByPlaceholder('Nurse full name').fill('Sarah Jenkins');

    await page.getByRole('button', { name: 'Add to Roster' }).click();

    await expect(modalHeader).toBeHidden({ timeout: 8000 });

    await expect(page.locator('p-table')).toContainText('Jane');
    await expect(page.locator('p-table')).toContainText('Doe');
    await expect(page.locator('p-table')).toContainText('Acute Asthma');
  });

  test('Opens Add Patient modal and validates required fields', async ({ page }) => {
    await loginAndGoToRoster(page);

    await page.getByRole('button', { name: '+ Add Patient' }).click();

    const modalHeader = page.getByText('Add Patient to Roster');
    await expect(modalHeader).toBeVisible({ timeout: 10000 });

    await page.getByRole('button', { name: 'Add to Roster' }).click();

    await expect(page.getByText('Required.').first()).toBeVisible();

    await expect(modalHeader).toBeVisible();
  });

  test('Cancel button closes the modal without adding a patient', async ({ page }) => {
    await loginAndGoToRoster(page);

    const tableRowsBefore = page.locator('.p-datatable-tbody tr.cursor-pointer');
    const countBefore = await tableRowsBefore.count();

    await page.getByRole('button', { name: '+ Add Patient' }).click();
    const modalHeader = page.getByText('Add Patient to Roster');
    await expect(modalHeader).toBeVisible({ timeout: 10000 });

    await page.getByRole('button', { name: 'Cancel' }).click();

    await expect(modalHeader).toBeHidden({ timeout: 8000 });
    await expect(tableRowsBefore).toHaveCount(countBefore);
  });
});
