import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "./api";

export default function AccountSelect() {
    const [accounts, setAccounts] = useState([]);
    const [selectedIndex, setSelectedIndex] = useState(null);
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const fetchAccounts = async () => {
        try {
            const res = await apiClient.get("/api/accounts");
            setAccounts(res.data.data);
        } catch (err) {
            console.error("계좌 목록 조회 실패:", err);
            alert("계좌 목록을 불러오지 못했습니다.");
        }
    };

    const handleSignIn = async () => {
        console.log("selectedIndex:", selectedIndex);
        console.log("accounts:", accounts);

        if (selectedIndex === null || password.trim() === "") {
            alert("계좌와 비밀번호를 입력해주세요.");
            return;
        }

        const selectedAccount = accounts[selectedIndex];
        if (!selectedAccount || !selectedAccount.accountName) {
            alert("선택된 계좌 정보가 유효하지 않습니다.");
            return;
        }

        try {
            const res = await apiClient.post(
                "/api/accounts/sign",
                {
                    accountId: selectedAccount.id,
                    password,
                },
                {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
                    },
                }
            );

            const token = res.data.message;
            if (token?.startsWith("Bearer ")) {
                localStorage.setItem("accountToken", token.split(" ")[1]);
                alert("계좌 로그인 완료");
                navigate("/account/info");

            } else {
                alert("유효하지 않은 토큰입니다.");
            }
        } catch (err) {
            console.error("계좌 로그인 실패:", err);
            alert("계좌 로그인에 실패했습니다.");
        }
    };


    const handleDelete = async (indexToDelete) => {
        if (!window.confirm("정말 삭제하시겠습니까?")) return;

        const accountToDelete = accounts[indexToDelete];
        console.log("삭제 대상 account:", accountToDelete);
        const accountId = accountToDelete.id || accountToDelete.accountId; // id 없으면 accountId 사용
        if (!accountId) {
            alert("계좌 ID를 찾을 수 없습니다.");
            return;
        }

        try {
            await apiClient.delete(`/api/accounts/${accountToDelete.id}`, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
                },
            });

            setAccounts((prev) => prev.filter((_, idx) => idx !== indexToDelete));
            if (selectedIndex === indexToDelete) {
                setSelectedIndex(null);
            } else if (selectedIndex > indexToDelete) {
                setSelectedIndex((prev) => prev - 1);
            }

            alert("계좌가 삭제되었습니다.");
        } catch (err) {
            console.error("계좌 삭제 실패:", err);
            alert("계좌 삭제에 실패했습니다.");
        }
    };


    useEffect(() => {
        fetchAccounts();
    }, []);

    return (
        <div style={styles.container}>
            <img src="/logo.png" alt="logo" style={styles.logo}/>
            <h2 style={styles.title}>Mockstalk</h2>
            <p style={styles.subtitle}>계좌를 선택하고 로그인하세요.</p>

            <div style={styles.accountList}>
                {accounts.map((acc, index) => (
                    <div key={index} style={styles.accountItem}>
                        <label style={styles.accountLabel}>
                            <input
                                type="radio"
                                name="account"
                                checked={selectedIndex === index}
                                onChange={() => setSelectedIndex(index)}
                                style={styles.radio}
                            />
                            <span style={styles.accountName}>{acc.accountName}</span>
                        </label>
                        <button onClick={() => handleDelete(index)} style={styles.deleteButton}>
                            삭제
                        </button>
                    </div>
                ))}
            </div>

            <input
                type="password"
                placeholder="계좌 비밀번호"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={styles.input}
            />

            <button onClick={handleSignIn} style={styles.button}>
                로그인
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
    accountList: {
        width: "100%",
        marginBottom: 30,
        display: "flex",
        flexDirection: "column",
        gap: 12,
    },
    accountItem: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        border: "1px solid #ddd",
        borderRadius: 10,
        padding: "12px 15px",
        backgroundColor: "#f9f9f9",
    },
    accountLabel: {
        display: "flex",
        alignItems: "center",
        gap: 10,
        cursor: "pointer",
        userSelect: "none",
    },
    radio: {
        cursor: "pointer",
        width: 16,
        height: 16,
    },
    accountName: {
        fontWeight: "500",
        fontSize: 16,
    },
    deleteButton: {
        color: "#e53e3e",
        background: "none",
        border: "none",
        fontSize: 14,
        cursor: "pointer",
        fontWeight: "bold",
    },
    input: {
        width: "100%",
        padding: "12px 15px",
        fontSize: 14,
        border: "1px solid #ddd",
        borderRadius: 10,
        outline: "none",
        marginBottom: 20,
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
        width: "100%",
    },
};
