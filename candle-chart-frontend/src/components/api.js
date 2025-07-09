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

// 401Unauthorized 응답받으면 토큰제거후 로그인페이지로 이동
apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default apiClient;
