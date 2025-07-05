import React, {useEffect, useState} from "react";
import apiClient from "./api";

export default function AccountInfo() {
    const [account, setAccount] = useState(null);
    const [error, setError] = useState(null);

    const fetchAccount = async () => {
        try {
            let token = localStorage.getItem("accountToken");

            if (!token) {
                setError("로그인 토큰이 없습니다.");
                return;
            }

            // token이 'Bearer ' 포함되어 있으면 그대로 쓰고, 아니면 붙이기
            if (!token.startsWith("Bearer ")) {
                token = `Bearer ${token}`;
            }

            const res = await apiClient.get(`/api/accounts/info`, {
                headers: {
                    "X-ACCOUNT-Authorization": token,
                },
            });

            setAccount(res.data.data);
            setError(null);
        } catch (err) {
            console.error("계좌 정보 조회 실패:", err);
            setError("계좌 정보를 불러오지 못했습니다.");
        }
    };

    useEffect(() => {
        fetchAccount();
    }, []);

    if (error)
        return (
            <div style={styles.loading}>
                <p style={{color: "red"}}>{error}</p>
            </div>
        );

    if (!account)
        return <div style={styles.loading}>로딩 중...</div>;

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>내 계좌 정보</h2>
            <div style={styles.infoItem}>
                <strong>계좌 이름: </strong> {account.accountName}
            </div>
            <div style={styles.infoItem}>
                <strong>현재 잔액: </strong>{" "}
                {account.currentBalance != null
                    ? `₩${account.currentBalance.toLocaleString()}`
                    : "정보 없음"}
            </div>
            <div style={styles.infoItem}>
                <strong>초기 자산: </strong>{" "}
                {account.initialBalance != null
                    ? `₩${account.initialBalance.toLocaleString()}`
                    : "정보 없음"}
            </div>

            {account.totalAsset != null && (
                <div style={styles.infoItem}>
                    <strong>총 자산: </strong> ₩{account.totalAsset.toLocaleString()}
                </div>
            )}
            {account.profitRate != null && (
                <div style={{...styles.infoItem, marginBottom: 0}}>
                    <strong>수익률: </strong> {account.profitRate.toFixed(2)}%
                </div>
            )}

            {account.createdAt && (
                <div
                    style={{...styles.infoItem, fontSize: 14, marginTop: 20, color: "#888"}}
                >
                    생성일: {new Date(account.createdAt).toLocaleString()}
                </div>
            )}
        </div>
    );
}

const styles = {
    container: {
        maxWidth: 500,
        margin: "30px auto",
        padding: "40px 20px",
        fontFamily: "sans-serif",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#fafafa",
        color: "#333",
    },
    title: {
        fontSize: 22,
        fontWeight: "bold",
        marginBottom: 20,
        textAlign: "center",
    },
    infoItem: {
        fontSize: 16,
        marginBottom: 15,
        color: "#555",
    },
    loading: {
        maxWidth: 500,
        margin: "30px auto",
        padding: 40,
        fontFamily: "sans-serif",
        textAlign: "center",
        fontSize: 18,
        color: "#666",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#fafafa",
    },
};
