import axios from 'axios';

const api = axios.create({
    baseURL: '/api/v1',
    timeout: 10000,
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('access_token');
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => {
        // 强制对齐后端 {code: 200, data, message} 协议
        const res = response.data;
        if (res.code === 200) {
            return res;
        } else {
            console.error('API Error:', res.message);
            return Promise.reject(new Error(res.message || 'Error'));
        }
    },
    (error) => {
        console.error('HTTP Error:', error);
        return Promise.reject(error);
    }
);

export default api;
