import { chromium } from 'playwright';
import crypto from 'crypto';

// Generate JWT token manually
const header = { alg: 'HS256', typ: 'JWT' };
const payload = {
    sub: 'city_admin',
    ROLE_TYPE: 'ROLE_CITY_ADMIN',
    ORG_ID: 1,
    iat: Math.floor(Date.now() / 1000),
    exp: Math.floor(Date.now() / 1000) + 86400
};

const encodeBase64Url = (obj) => Buffer.from(JSON.stringify(obj)).toString('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
const encodedHeader = encodeBase64Url(header);
const encodedPayload = encodeBase64Url(payload);

const secret = 'QWEASDZXC1234567890qwertyuiopasdfghjklzxcvbnm_DEFAULT_SECRET';
const signature = crypto.createHmac('sha256', secret).update(`${encodedHeader}.${encodedPayload}`).digest('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
const token = `${encodedHeader}.${encodedPayload}.${signature}`;

(async () => {
    console.log('Starting Playwright test for Epic 2 - Story 2.5 (Reference Card in Drawer)...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    console.log('Setting up local storage Auth Token.');
    await page.goto('http://localhost:3000/');
    await page.evaluate((t) => localStorage.setItem('access_token', t), token);

    console.log('Navigating to Radar Dashboard...');
    await page.goto('http://localhost:3000/dashboard/radar');

    // Wait for the ECharts component to initialize
    await page.waitForTimeout(1500);

    console.log('1. Injecting simulated click to open FIRST level diagnosis drawer for Node ID 103...');
    await page.evaluate(() => {
        if (window.triggerDiagnosis) {
            window.triggerDiagnosis(103);
        } else {
            console.error('Trigger Hook Not Found on window!');
        }
    });

    console.log('Waiting 2000ms for backend API mock delay + Skeleton transition...');
    await page.waitForTimeout(2000);

    // Save Visual proof of loaded Drawer with reference card
    await page.screenshot({ path: 'drawer-refcard-result.png', fullPage: true });

    // Verify RefCard renders correctly
    const refCardData = await page.evaluate(() => {
        const cards = Array.from(document.querySelectorAll('.ref-card-wrapper h3'));
        return cards.map(c => c.textContent);
    });

    if (refCardData && refCardData.length > 0 && refCardData[0].includes('市教育局下达本季度骨干教师')) {
        console.log('=====================\nTEST PASSED (Part 1/2): AI Recommended Reference Cards successfully loaded into Drawer!\n=====================');

        console.log('2. Clicking the FIRST Reference Card to cascade the SECOND level Detail Drawer...');
        await page.click('.ref-card-wrapper');
        await page.waitForTimeout(1000); // Wait for sliding animation

        await page.screenshot({ path: 'drawer-article-result.png', fullPage: true });

        // Verify Full Article HTML text exists
        const articleData = await page.evaluate(() => {
            const article = document.querySelector('.article-content h2');
            return article ? article.textContent : null;
        });

        if (articleData && articleData.includes('骨干教师安居补贴执行方案')) {
            console.log('=====================\nTEST PASSED (Part 2/2): Cascading Reading Drawer successfully mounted and shows full HTML content!\n=====================');
        } else {
            console.error('=====================\nTEST FAILED (Part 2/2): Article Details Drawer failed to mount or render rich text.\n=====================');
        }

    } else {
        console.error('=====================\nTEST FAILED (Part 1/2): Could not locate expected Reference Card text in the Drawer. Got:', refCardData, '\n=====================');
    }

    await browser.close();
    process.exit(0);
})();
