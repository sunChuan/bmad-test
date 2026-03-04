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
    console.log('Starting Playwright Deep Debug WITH Auth...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    let errors = [];
    page.on('console', msg => {
        if (msg.type() === 'error') {
            errors.push(`[BROWSER CONSOLE ERROR] ${msg.text()}`);
        }
    });
    page.on('pageerror', error => {
        errors.push(`[PAGE ERROR] ${error.message}`);
    });

    try {
        await page.goto('http://localhost:3000/');
        await page.evaluate((t) => localStorage.setItem('access_token', t), token);

        console.log('Token injected, navigating to radar dashboard...');
        await page.goto('http://localhost:3000/dashboard/radar', { waitUntil: 'domcontentloaded', timeout: 15000 });

        console.log('Waiting 3 seconds for rendering...');
        await page.waitForTimeout(3000);

        const bodyHtml = await page.evaluate(() => document.body.outerHTML);
        console.log('\n--- BODY HTML ---');
        console.log(bodyHtml.substring(0, 1000));
        if (bodyHtml.length > 1000) console.log('... (truncated)');

        const innerApp = await page.evaluate(() => {
            const el = document.getElementById('app');
            return el ? el.innerHTML : 'NO #app element';
        });

        console.log('\n--- APP HTML ---');
        console.log(innerApp.substring(0, 500));
        if (innerApp.length > 500) console.log('... (truncated)');

        if (errors.length > 0) {
            console.error('\n--- CAPTURED ERRORS ---');
            errors.forEach(e => console.error(e));
        } else {
            console.log('\nNo obvious JS errors caught.');
        }

    } catch (e) {
        console.log('Navigation or evaluating failed:', e.message);
    }

    await browser.close();
    process.exit(0);
})();
