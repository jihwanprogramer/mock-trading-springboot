import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import apiClient from './api'; // 실제 경로에 맞게 조정

function LoginPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [userRole, setUserRole] = useState('USER');

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const res = await apiClient.post('/api/auth/login', {
                email,
                password,
                userRole,
            });

            const token = res.data.data?.trim();
            if (token) {
                localStorage.setItem('email', email);
                localStorage.setItem('token', token);
                navigate('/profile');
            } else {
                alert('토큰이 없습니다. 로그인 실패.');
            }
        } catch (err) {
            console.error(err);
            const message = err.response?.data?.message || '이메일 또는 비밀번호가 잘못되었습니다.';
            alert(message);
        }
    };

    return (
        <div style={styles.container}>
            <img src="/logo.png" alt="logo" style={styles.logo}/>
            <h2 style={styles.welcome}>어서오세요 👋</h2>
            <p style={styles.subtitle}>다시 오신 것을 환영합니다. 로그인해주세요.</p>
            <form style={styles.form} onSubmit={handleLogin}>
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    style={styles.input}
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={styles.input}
                    required
                />
                <select
                    value={userRole}
                    onChange={(e) => setUserRole(e.target.value)}
                    style={{...styles.input, padding: '10px'}}
                >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                </select>
                <button type="submit" style={styles.button}>로그인</button>
            </form>
            <p style={styles.linkText}>
                계정이 없으신가요?{' '}
                <span style={styles.link} onClick={() => navigate('/register')}>
                    회원가입
                </span>
            </p>
        </div>
    );
}

const styles = {
    container: {
        maxWidth: 400,
        margin: '0 auto',
        padding: '50px 20px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        fontFamily: 'sans-serif',
    },
    logo: {
        width: 60,
        height: 60,
        marginBottom: 20,
    },
    welcome: {
        fontSize: 22,
        fontWeight: 'bold',
        marginBottom: 5,
    },
    subtitle: {
        fontSize: 14,
        color: '#666',
        marginBottom: 30,
    },
    form: {
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        gap: 15,
    },
    input: {
        padding: '12px 15px',
        fontSize: 14,
        border: '1px solid #ddd',
        borderRadius: 10,
        outline: 'none',
    },
    button: {
        padding: '12px',
        fontSize: 16,
        backgroundColor: '#4461F2',
        color: '#fff',
        border: 'none',
        borderRadius: 10,
        cursor: 'pointer',
        fontWeight: 'bold',
    },
    linkText: {
        marginTop: 20,
        fontSize: 14,
        color: '#888',
    },
    link: {
        color: '#4461F2',
        cursor: 'pointer',
        fontWeight: 'bold',
    },
};

export default LoginPage;
