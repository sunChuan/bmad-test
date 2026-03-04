import { onUnmounted } from 'vue';
import { notification } from 'ant-design-vue';

/**
 * Story 2.3 - 前端 SSE 消息监听 Composable
 * 
 * 职责：
 * 1. 建立与后端的持久化 EventSource 连接。
 * 2. 自动从 localStorage 提取 Token 并作为 Query Param 传输。
 * 3. 监听 'alert' 事件，触发全局 Notification 提示。
 */
export function useSseMessage() {
    let eventSource: EventSource | null = null;

    const connect = () => {
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.warn('SSE: 未发现认证 Token，跳过订阅。');
            return;
        }

        // 构建带身份令牌的订阅地址
        const url = `/api/v1/dashboard/sse/subscribe?token=${encodeURIComponent(token)}`;

        eventSource = new EventSource(url);

        // 默认监听
        eventSource.onopen = () => {
            console.log('SSE: 已成功建立后端单向事件流。');
        };

        // 监听特定名为 "alert" 的事件消息
        eventSource.addEventListener('alert', (event: MessageEvent) => {
            console.log('SSE: 收到实时预警 ->', event.data);

            // 触发 Vben / AntDV 强提醒通知
            notification.error({
                message: '突发预警指标',
                description: event.data,
                duration: 0, // 强制不自动关闭 (Story 2.3 需求)
                placement: 'topRight'
            });
        });

        eventSource.onerror = (err) => {
            console.error('SSE: 连接异常或被服务器挂断:', err);
            eventSource?.close();

            // 简单的退避重连机制 (5秒后重试)
            setTimeout(() => {
                console.log('SSE: 正在尝试自动重新连接...');
                connect();
            }, 5000);
        };
    };

    const close = () => {
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
    };

    onUnmounted(() => {
        close();
    });

    return {
        connect,
        close
    };
}
