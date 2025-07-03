import React, {useState} from 'react';
import LoginPage from "./LoginPage";

function EditStep2() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        // 서버 연동 로직 필요
        alert('회원정보가 수정되었습니다.');
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>회원정보 수정</h2>
            <p style={styles.subtitle}>새로운 아이디와 비밀번호를 입력해주세요.</p>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email"
                       style={styles.input} required/>
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                       placeholder="Password" style={styles.input} required/>
                <button type="submit" style={styles.button}>확인</button>
            </form>
        </div>
    );
}

const styles = {...LoginPage.styles};
export default EditStep2;
