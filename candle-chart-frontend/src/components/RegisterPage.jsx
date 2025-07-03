import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import apiClient from './api'; // axios 인스턴스 경로 맞게

function RegisterPage() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        email: '',
        password: '',
        nickname: '',
        walletAddress: '',
        userRole: 'USER', // 기본값 USER
    });

    const handleChange = (e) => {
        const {name, value} = e.target;
        setForm(prev => ({...prev, [name]: value}));
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            await apiClient.post('/users/signup', form);
            alert('회원가입 성공');
            navigate('/login');
        } catch (error) {
            console.error('회원가입 실패', error);
            alert('회원가입 실패: ' + (error.response?.data?.message || '서버 오류'));
        }
    };

    return (
        <div style={styles.container}>
            <img src="/logo.png" alt="logo" style={styles.logo}/>
            <h2>회원가입</h2>
            <form style={styles.form} onSubmit={handleRegister}>
                <input
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={form.email}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <input
                    type="password"
                    name="password"
                    placeholder="Password"
                    value={form.password}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <input
                    type="text"
                    name="nickname"
                    placeholder="닉네임"
                    value={form.nickname}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <input
                    type="text"
                    name="walletAddress"
                    placeholder="지갑 주소"
                    value={form.walletAddress}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <label htmlFor="userRole" style={{marginTop: 10}}>
                    사용자 권한 선택:
                </label>
                <select
                    id="userRole"
                    name="userRole"
                    value={form.userRole}
                    onChange={handleChange}
                    style={styles.input}
                    required
                >
                    <option value="USER">일반 사용자</option>
                    <option value="ADMIN">관리자</option>
                </select>

                <button type="submit" style={styles.button}>
                    회원가입
                </button>
            </form>

            {/* 아래 부분 추가 */}
            <p style={styles.linkText}>
                이미 계정이 있으신가요?{' '}
                <span style={styles.link} onClick={() => navigate('/login')}>
                    로그인
                </span>
            </p>
        </div>
    );
}

const styles = {
    logo: {
        width: 60,
        height: 60,
        marginBottom: 20,
    },
    container: {maxWidth: 400, margin: '0 auto', padding: 20, fontFamily: 'sans-serif'},
    form: {display: 'flex', flexDirection: 'column', gap: 10},
    input: {padding: 10, fontSize: 14, borderRadius: 5, border: '1px solid #ccc'},
    button: {
        padding: 12,
        backgroundColor: '#4461F2',
        color: 'white',
        border: 'none',
        borderRadius: 8,
        cursor: 'pointer',
        fontWeight: 'bold',
        marginTop: 10,
    },
    linkText: {
        marginTop: 20,
        fontSize: 14,
        color: '#555',
        textAlign: 'center',
    },
    link: {
        color: '#4461F2',
        cursor: 'pointer',
        fontWeight: 'bold',
        textDecoration: 'underline',
    },
};

export default RegisterPage;
