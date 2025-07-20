import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import apiClient from './api';

const styles = {
    container: {padding: 20, display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 400, margin: '30px auto'},
    form: {display: 'flex', flexDirection: 'column', gap: 10},
    input: {padding: 12, borderRadius: 6, border: '1px solid #ccc', fontSize: 16, outline: 'none'},
    button: {
        padding: 14,
        background: '#4f46e5',
        color: 'white',
        border: 'none',
        borderRadius: 6,
        fontWeight: 'bold',
        cursor: 'pointer',
    },
    deleteButton: {
        padding: 14,
        background: '#dc2626',
        color: 'white',
        border: 'none',
        borderRadius: 6,
        fontWeight: 'bold',
        cursor: 'pointer',
    },
};

function EditStep2() {
    const navigate = useNavigate();
    const [nickname, setNickname] = useState('');
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [deletePassword, setDeletePassword] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        if (!nickname && !newPassword) {
            setError('닉네임 또는 새 비밀번호 중 하나는 입력해야 합니다.');
            setLoading(false);
            return;
        }

        if (newPassword && !oldPassword) {
            setError('새 비밀번호를 변경하려면 현재 비밀번호를 입력하세요.');
            setLoading(false);
            return;
        }

        try {
            const updateData = {};
            if (nickname) updateData.nickname = nickname;
            if (newPassword) {
                updateData.newPassword = newPassword;
                updateData.oldPassword = oldPassword;
            }

            await apiClient.patch('/api/users/me', updateData);

            alert('회원정보가 수정되었습니다.');
            navigate('/profile');
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || '수정 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };


    const handleDelete = async () => {
        if (!deletePassword) {
            alert('비밀번호를 입력하세요.');
            return;
        }
        const confirmed = window.confirm('정말로 회원 탈퇴하시겠습니까?');
        if (!confirmed) return;

        try {
            await apiClient.delete('/api/users/me', {
                data: {password: deletePassword}
            });

            alert('회원탈퇴가 완료되었습니다.');
            localStorage.removeItem('token');
            localStorage.removeItem('email');
            navigate('/');
        } catch (err) {
            console.error(err);
            alert(err.response?.data?.message || '회원탈퇴 실패');
        }
    };


    return (
        <div style={styles.container}>
            <h2>회원정보 수정</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    style={styles.input}
                    type="text"
                    placeholder="새 닉네임"
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                />
                <input
                    style={styles.input}
                    type="password"
                    placeholder="현재 비밀번호"
                    value={oldPassword}
                    onChange={(e) => setOldPassword(e.target.value)}
                    required={newPassword.length > 0} // 새비밀번호 입력시에만 필수
                />
                <input
                    style={styles.input}
                    type="password"
                    placeholder="새 비밀번호"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                />
                <button style={styles.button} type="submit" disabled={loading}>
                    {loading ? '수정 중...' : '확인'}
                </button>
            </form>

            <hr style={{margin: '20px 0'}}/>

            <input
                style={styles.input}
                type="password"
                placeholder="비밀번호를 입력하세요"
                value={deletePassword}
                onChange={(e) => setDeletePassword(e.target.value)}
                required
            />
            <button
                type="button"
                style={styles.deleteButton}
                onClick={handleDelete}
            >
                회원 탈퇴
            </button>

            {error && <p style={{color: 'red', marginTop: 10}}>{error}</p>}
        </div>
    );
}

export default EditStep2;
