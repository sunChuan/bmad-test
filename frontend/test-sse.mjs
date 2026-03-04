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
    console.log('Starting Playwright test...');
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.goto('http://localhost:3000/');
    await page.evaluate((t) => localStorage.setItem('access_token', t), token);

    console.log('Injected mock JWT token. Navigating to Radar Dashboard...');
    // Go to radar dashboard
    await page.goto('http://localhost:3000/dashboard/radar');

    // Wait for the SSE connection message in console
    page.on('console', msg => {
        // Filter Vite logs
        if (msg.text().includes('SSE')) {
            console.log('PAGE LOG:', msg.text());
        }
    });

    console.log('Waiting for frontend SSE to establish...');
    await page.waitForTimeout(3000);

    // Trigger alert via API
    console.log('Triggering alert from backend...');
    const res = await fetch('http://localhost:8080/api/v1/dashboard/sse/trigger-alert?filterPrefix=ROLE_CITY_ADMIN&message=Emergency_Detected_System_Down', {
        method: 'POST'
    });
    console.log('Trigger response:', await res.text());

    console.log('Waiting for frontend UI to render the notification...');
    await page.waitForTimeout(2000);

    // Capture screenshot just in case
    await page.screenshot({ path: 'sse-test-result.png' });

    // Check if Notification exists
    const notificationText = await page.evaluate(() => {
        const notice = document.querySelector('.ant-notification-notice-description');
        return notice ? notice.textContent : null;
    });

    console.log('Notification found in UI:', notificationText);
    if (notificationText && notificationText.includes('Emergency_Detected_System_Down')) {
        console.log('=====================\nTEST PASSED: UI Notification Successfully Displayed!\n=====================');
    } else {
        console.log('=====================\nTEST FAILED: Target notification text not rendered as expected.\n=====================');
    }

    await browser.close();
    process.exit(0);
})();
