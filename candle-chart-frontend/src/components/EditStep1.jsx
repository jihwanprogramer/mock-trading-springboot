import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import apiClient from './api';

function EditStep1() {
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const email = localStorage.getItem('email');
            console.log('EditStep1 email from localStorage:', email);
            if (!email) {
                setError('로그인이 필요합니다.');
                return;
            }

            await apiClient.post('/api/auth/login', {
                email,
                password,
            });

            navigate('/user/edit2');
        } catch (err) {
            console.error(err);
            setError('비밀번호가 올바르지 않습니다.');
        }
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>회원정보 수정</h2>
            <p style={styles.subtitle}>현재 비밀번호를 입력해주세요.</p>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Password"
                    style={styles.input}
                    required
                    autoFocus
                />
                <button type="submit" style={styles.button}>확인</button>
            </form>
            {error && <p style={{color: 'red', marginTop: 10}}>{error}</p>}
        </div>
    );
}

const styles = {
    container: {
        maxWidth: 400,
        margin: '30px auto',
        padding: 20,
        fontFamily: 'sans-serif',
        border: '1px solid #ddd',
        borderRadius: 10,
        backgroundColor: '#fafafa',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 10,
        textAlign: 'center',
    },
    subtitle: {
        marginBottom: 20,
        textAlign: 'center',
        color: '#555',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: 10,
    },
    input: {
        padding: 10,
        fontSize: 16,
        borderRadius: 6,
        border: '1px solid #ccc',
        outline: 'none',
    },
    button: {
        padding: 12,
        backgroundColor: '#4461F2',
        color: '#fff',
        border: 'none',
        borderRadius: 6,
        cursor: 'pointer',
        fontWeight: 'bold',
        fontSize: 16,
    },
};

export default EditStep1;
