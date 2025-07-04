import React, {useEffect, useState} from "react";
import apiClient from "./api";

export default function AccountInfo() {
    const [account, setAccount] = useState(null);

    const fetchAccount = async () => {
        try {
            const res = await apiClient.get("/accounts/info");
            setAccount(res.data.data);
        } catch (err) {
            console.error("계좌 정보 조회 실패:", err);
            alert("계좌 정보를 불러오지 못했습니다.");
        }
    };

    useEffect(() => {
        fetchAccount();
    }, []);

    if (!account)
        return <div style={styles.loading}>로딩 중...</div>;

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>내 계좌 정보</h2>
            <div style={styles.infoItem}>
                <strong>계좌 이름: </strong> {account.accountName}
            </div>
            <div style={styles.infoItem}>
                <strong>현재 잔액: </strong> ₩{account.currentBalance.toLocaleString()}
            </div>
            <div style={styles.infoItem}>
                <strong>초기 자산: </strong> ₩{account.initialBalance.toLocaleString()}
            </div>
            <div style={styles.infoItem}>
                <strong>총 자산: </strong> ₩{account.totalAsset.toLocaleString()}
            </div>
            <div style={{...styles.infoItem, marginBottom: 0}}>
                <strong>수익률: </strong> {account.profitRate.toFixed(2)}%
            </div>
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
