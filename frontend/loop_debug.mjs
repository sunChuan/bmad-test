import { chromium } from 'playwright';

(async () => {
    console.log('Starting Playwright Loop Simulation...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    page.on('console', msg => {
        if (msg.type() === 'error') {
            console.log(`[BROWSER CONSOLE ERROR] ${msg.text()}`);
        }
    });

    page.on('pageerror', error => {
        console.log(`[PAGE ERROR] ${error.message}`);
    });

    try {
        console.log('1. Visiting /dashboard/radar without token (should redirect to /login)');
        await page.goto('http://localhost:3000/dashboard/radar', { waitUntil: 'domcontentloaded' });

        console.log('2. Current URL:', page.url());

        console.log('3. Injecting token');
        const tokenVal = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjaXR5X2FkbWluIiwiUk9MRV9UWVBFIjoiUk9MRV9DSVRZX0FETUlOIiwiT1JHX0lEIjoxLCJpYXQiOjE3NzI1OTA4MjAsImV4cCI6MTc3MjY3NzIyMH0.d_ByjYHqR6iXlR3VJCRkJ6-A-B6VltoNR9_rgugX2nE';
        await page.evaluate((t) => localStorage.setItem('access_token', t), tokenVal);

        console.log('4. Navigating again to /dashboard/radar just like the user said');
        await page.goto('http://localhost:3000/dashboard/radar', { waitUntil: 'domcontentloaded' });

        console.log('5. Waiting 3 seconds...');
        await page.waitForTimeout(3000);

        console.log('6. Final URL:', page.url());
        const appHtml = await page.evaluate(() => document.getElementById('app') ? document.getElementById('app').innerHTML : 'NO APP HTML');
        if (appHtml === '' || appHtml === 'NO APP HTML') {
            console.log('\n--- PAGE IS BLANK ---');
        } else {
            console.log('\n--- PAGE RENDERED ---');
            console.log(appHtml.substring(0, 200));
        }

    } catch (e) {
        console.log('Test failed:', e.message);
    }

    await browser.close();
    process.exit(0);
})();
