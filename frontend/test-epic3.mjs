import { chromium } from 'playwright';
import path from 'path';
import fs from 'fs';

const ARTIFACTS_DIR = 'C:\\Users\\work\\.gemini\\antigravity\\brain\\e5ec1829-2ca3-4f3e-b332-fe2669fe74a2';

(async () => {
    console.log('Starting Epic 3 E2E UI verification...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    // 1. Setup Auth
    const tokenVal = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjaXR5X2FkbWluIiwiUk9MRV9UWVBFIjoiUk9MRV9DSVRZX0FETUlOIiwiT1JHX0lEIjoxLCJpYXQiOjE3NzI1OTA4MjAsImV4cCI6MTc3MjY3NzIyMH0.d_ByjYHqR6iXlR3VJCRkJ6-A-B6VltoNR9_rgugX2nE';
    await page.goto('http://localhost:3000/login', { waitUntil: 'domcontentloaded' });
    await page.evaluate((t) => localStorage.setItem('access_token', t), tokenVal);

    // 2. Test Sandbox
    console.log('Visiting Sandbox...');
    await page.goto('http://localhost:3000/admin/sandbox', { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'epic3-sandbox.png'), fullPage: true });

    // 3. Test Data Fallback
    console.log('Visiting Data Fallback...');
    await page.goto('http://localhost:3000/admin/data/fallback', { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'epic3-data-fallback.png'), fullPage: true });

    // 4. Test Article CMS
    console.log('Visiting Article CMS...');
    await page.goto('http://localhost:3000/admin/cms/article', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000); // give it a sec to mock fetch
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'epic3-cms.png'), fullPage: true });

    // 5. Test Home Dashboard entry
    console.log('Visiting Home...');
    await page.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'epic3-home.png'), fullPage: true });

    console.log('--- ALL SCREENSHOTS TAKEN IN ARTIFACTS DIR ---');

    await browser.close();
    process.exit(0);
})();
