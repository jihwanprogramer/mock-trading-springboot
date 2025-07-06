import React, {useState} from "react";
import apiClient from "./api";

export default function BuyConfirm() {
    const [stockCode] = useState("NFLX");
    const [quantity] = useState(1);

    const handleBuy = async () => {
        try {
            await apiClient.post("/api/v1/trade/buy", {
                stockCode,
                quantity,
            });
            alert("매수 완료");
        } catch (err) {
            console.error("매수 실패:", err);
            alert("매수에 실패했습니다.");
        }
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>매수 확인</h2>
            <div style={styles.infoItem}>종목: {stockCode}</div>
            <div style={styles.infoItem}>수량: {quantity}개</div>
            <div style={{...styles.infoItem, marginBottom: 30}}>
                예상 금액: ₩{(quantity * 88910).toLocaleString()}
            </div>
            <button onClick={handleBuy} style={styles.button}>
                매수하기
            </button>
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
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
    },
    title: {
        fontSize: 22,
        fontWeight: "bold",
        marginBottom: 20,
    },
    infoItem: {
        fontSize: 16,
        marginBottom: 15,
        color: "#555",
        width: "100%",
    },
    button: {
        width: "100%",
        padding: "12px 0",
        fontSize: 16,
        backgroundColor: "#27ae60",
        color: "#fff",
        border: "none",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: "bold",
    },
};
