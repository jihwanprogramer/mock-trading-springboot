import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "./api";

export default function AccountCreate() {
    const [accountName, setAccountName] = useState("");
    const [password, setPassword] = useState("");
    const [initialBalance, setInitialBalance] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async () => {
        try {
            await apiClient.post("/api/accounts", {
                accountName,
                password,
                initialBalance: parseFloat(initialBalance),
            });
            alert("계좌가 성공적으로 생성되었습니다.");
            setAccountName("");
            setPassword("");
            setInitialBalance("");
        } catch (err) {
            console.error("계좌 생성 실패:", err);
            alert("계좌 생성에 실패했습니다.");
        }
    };

    return (
        <div style={styles.container}>
            <img src="/logo.png" alt="logo" style={styles.logo}/>
            <h2 style={styles.title}>계좌 생성</h2>
            <p style={styles.subtitle}>계좌 정보를 입력해주세요.</p>

            <div style={styles.form}>
                <input
                    type="text"
                    placeholder="계좌 이름"
                    value={accountName}
                    onChange={(e) => setAccountName(e.target.value)}
                    style={styles.input}
                />
                <input
                    type="password"
                    placeholder="비밀번호"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={styles.input}
                />
                <input
                    type="number"
                    placeholder="초기 자산 (원)"
                    value={initialBalance}
                    onChange={(e) => setInitialBalance(e.target.value)}
                    style={styles.input}
                />
                <button onClick={handleSubmit} style={styles.button}>
                    계좌 생성
                </button>
            </div>

            {/* 계좌 선택하기 버튼 */}
            <button
                onClick={() => navigate("/account/select")}
                style={{...styles.button, marginTop: 20, backgroundColor: "#888"}}
            >
                계좌 선택하기
            </button>
        </div>
    );
}

const styles = {
    container: {
        maxWidth: 500,
        margin: "30px auto",
        padding: "40px 20px",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        fontFamily: "sans-serif",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#fafafa",
    },
    logo: {
        width: 60,
        height: 60,
        marginBottom: 20,
    },
    title: {
        fontSize: 22,
        fontWeight: "bold",
        marginBottom: 5,
    },
    subtitle: {
        fontSize: 14,
        color: "#666",
        marginBottom: 30,
    },
    form: {
        width: "100%",
        display: "flex",
        flexDirection: "column",
        gap: "15px",
    },
    input: {
        padding: "12px 15px",
        fontSize: 14,
        border: "1px solid #ddd",
        borderRadius: 10,
        outline: "none",
    },
    button: {
        padding: "12px 20px",
        fontSize: 14,
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: "bold",
    },
};
