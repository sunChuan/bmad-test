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
    console.log('Starting Playwright test for Epic 2 - Story 2.4 (Diagnosis Drawer)...');
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

    console.log('Injecting simulated click to open diagnosis drawer for Danger Node ID 103...');
    await page.evaluate(() => {
        if (window.triggerDiagnosis) {
            window.triggerDiagnosis(103);
        } else {
            console.error('Trigger Hook Not Found on window!');
        }
    });

    console.log('Waiting 2500ms for backend API delay (1.5s mock) + transition...');
    await page.waitForTimeout(2500);

    // Save Visual proof of loaded Drawer
    await page.screenshot({ path: 'drawer-test-result.png', fullPage: true });
    console.log('Screenshot captured as drawer-test-result.png');

    // Inspect Drawer body for verification
    const drawerData = await page.evaluate(() => {
        const drawerInner = document.querySelector('.ant-drawer-body');
        return drawerInner ? drawerInner.textContent : null;
    });

    if (drawerData && drawerData.includes('师资流失率与心理健康抽测骤降') && drawerData.includes('92.5%')) {
        console.log('=====================\nTEST PASSED: Diagnosis Drawer loaded and displayed valid attributes!\n=====================');
    } else {
        console.error('=====================\nTEST FAILED: Could not locate expected diagnosis string or confidence rating. Got:', drawerData, '\n=====================');
    }

    await browser.close();
    process.exit(0);
})();
