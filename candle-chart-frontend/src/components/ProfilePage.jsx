import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import apiClient from './api'; // axios 인스턴스

function ProfilePage() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await apiClient.get('/api/users/me');
                setUser(res.data.data);
            } catch (error) {
                console.error('프로필 조회 실패', error);
                if (error.response && error.response.status === 401) {
                    navigate('/login');
                }
            }
        };

        fetchProfile();
    }, [navigate]);

    const handleLogout = async () => {
        try {
            await apiClient.post('/api/auth/logout');
            alert('로그아웃 되었습니다.');
            // 로컬 스토리지 등에서 토큰 제거
            localStorage.removeItem('token');
            navigate('/login');
        } catch (error) {
            console.error('로그아웃 실패', error);
            alert('로그아웃 실패');
        }
    };

    if (!user) {
        return <div style={{padding: 20, fontFamily: 'sans-serif'}}>로딩 중...</div>;
    }

    const menu = [
        {label: '계정'},
        {label: '계좌'},
        {label: '보안'},
        {label: '청구/결제'},
        {label: '언어', right: '한국어'},
        {label: '설정'},
        {label: 'FAQ'},
    ];

    return (
        <div style={styles.container}>
            <button style={styles.backBtn} onClick={() => navigate(-1)}>←</button>
            <h2 style={styles.title}>프로필</h2>
            <div style={styles.profileBox}>
                <div style={styles.avatar}></div>
                <div>
                    <strong>{user.nickname || user.email}</strong>
                    <p>{user.email}</p>
                </div>
            </div>
            <div style={styles.menuList}>
                {menu.map((item, i) => (
                    <div key={i} style={styles.menuItem}>
                        <span style={{marginRight: 10}}></span>
                        <span>{item.label}</span>
                        {item.right && <span style={styles.rightText}>{item.right}</span>}
                    </div>
                ))}
            </div>
            <button style={styles.logoutBtn} onClick={handleLogout}>
                로그아웃
            </button>
        </div>
    );
}

const styles = {
    container: {padding: 20, fontFamily: 'sans-serif'},
    backBtn: {background: 'none', border: 'none', fontSize: 20, cursor: 'pointer', marginBottom: 10},
    title: {fontSize: 20, fontWeight: 'bold', marginBottom: 20},
    profileBox: {display: 'flex', gap: 15, alignItems: 'center', marginBottom: 20},
    avatar: {width: 50, height: 50, borderRadius: '50%', background: '#eee'},
    friendBtn: {width: '100%', padding: 12, border: '1px solid #ddd', borderRadius: 10, marginBottom: 20},
    menuList: {display: 'flex', flexDirection: 'column', gap: 15},
    menuItem: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        borderBottom: '1px solid #eee',
        paddingBottom: 10,
    },
    rightText: {color: '#888', fontSize: 12},
    logoutBtn: {
        width: '100%',
        padding: 12,
        backgroundColor: '#006ac6',
        color: 'white',
        border: 'none',
        borderRadius: 10,
        cursor: 'pointer',
        fontWeight: 'bold',
        marginTop: 30,
    },
};

export default ProfilePage;
