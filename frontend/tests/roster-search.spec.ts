import { test, expect } from '@playwright/test';

test.describe('Patient Roster Search Capabilities', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:4200/dashboard/patient-management');
  });

  test('Filtering the roster updates table and shows empty state', async ({ page }) => {
    const searchInput = page.getByPlaceholder('Search Patients...');
    const tableRows = page.locator('.p-datatable-tbody tr.cursor-pointer');

    await expect(tableRows).not.toHaveCount(0);

    await searchInput.fill('Vaida');
    await expect(page.getByText('Vaida')).toBeVisible();

    await searchInput.fill('Zyxwvutsrqponmlkjihgfedcba');

    await expect(tableRows).toHaveCount(0);
    await expect(page.getByText('No patients found')).toBeVisible();
    await expect(page.getByText('Try clearing the search filter')).toBeVisible();
  });
});
