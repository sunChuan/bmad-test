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
    console.log('Starting Playwright debugging...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    let errors = [];
    page.on('console', msg => {
        if (msg.type() === 'error') {
            errors.push(`Console Error: ${msg.text()}`);
        }
    });
    page.on('pageerror', error => {
        errors.push(`Page Error: ${error.message}`);
    });

    console.log('Injecting auth token & navigating...');
    await page.goto('http://localhost:3000/');
    await page.evaluate((t) => localStorage.setItem('access_token', t), token);

    await page.goto('http://localhost:3000/dashboard/radar');

    console.log('Waiting for potential rendering & errors...');
    await page.waitForTimeout(3000);

    if (errors.length > 0) {
        console.error('--- CAPTURED ERRORS ---');
        errors.forEach(e => console.error(e));
    } else {
        console.log('No obvious JS errors caught. Taking screenshot to verify visually...');
    }

    await browser.close();
    process.exit(errors.length > 0 ? 1 : 0);
})();
