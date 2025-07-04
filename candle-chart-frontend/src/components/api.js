import axios from 'axios';

const apiClient = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL,
    withCredentials: false,  // 쿠키 기반 인증이 필요할 때 사용
});


apiClient.interceptors.request.use((config) => {
    let token = localStorage.getItem('token')?.trim();
    if (token) {

        if (!token.startsWith('Bearer ')) {
            token = `Bearer ${token}`;
        }
        token = token.replace(/^Bearer\s+Bearer\s+/i, 'Bearer ');

        config.headers.Authorization = token;
    }
    return config;
});


export default apiClient;
