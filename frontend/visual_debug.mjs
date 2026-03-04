import { chromium } from 'playwright';
import crypto from 'crypto';

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
    console.log('Starting Visual Debugging...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
    const page = await context.newPage();

    let errors = [];
    page.on('console', msg => {
        if (msg.type() === 'error') {
            errors.push(`[CONSOLE] ${msg.text()}`);
        }
    });
    page.on('pageerror', error => {
        errors.push(`[JS_EXCEPTION] ${error.message}`);
    });

    try {
        console.log('Injecting auth token & navigating to /dashboard/radar ...');
        await page.goto('http://localhost:3000/');
        await page.evaluate((t) => localStorage.setItem('access_token', t), token);

        await page.goto('http://localhost:3000/dashboard/radar', { waitUntil: 'domcontentloaded', timeout: 15000 });

        console.log('Waiting 3 seconds to let ECharts render...');
        await page.waitForTimeout(3000);

        const screenshotPath = 'visual-debug-result.png';
        await page.screenshot({ path: screenshotPath, fullPage: true });
        console.log(`\n--- TAKEN SCREENSHOT: ${screenshotPath} ---`);

    } catch (e) {
        console.log('Navigation failed:', e.message);
    }

    await browser.close();
    process.exit(0);
})();
