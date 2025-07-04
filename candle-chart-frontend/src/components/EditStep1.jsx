import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import LoginPage from "./LoginPage";

function EditStep1() {
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        // 실제 서버 검증 로직 필요
        navigate('/api/edit/step2');
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>회원정보 수정</h2>
            <p style={styles.subtitle}>현재 비밀번호를 입력해주세요.</p>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                       placeholder="Password" style={styles.input} required/>
                <button type="submit" style={styles.button}>확인</button>
            </form>
        </div>
    );
}

const styles = {...LoginPage.styles};
export default EditStep1;
