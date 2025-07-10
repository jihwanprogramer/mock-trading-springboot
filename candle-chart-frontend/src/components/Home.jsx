import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "./api";

export default function Home() {
    const [stocks, setStocks] = useState([]);
    const navigate = useNavigate();

    const fetchStocks = async () => {
        try {
            const res = await apiClient.get("/api/v1/stocks");
            setStocks(res.data.data);
        } catch (err) {
            console.error("종목 불러오기 실패:", err);
            alert("종목 정보를 가져오지 못했습니다.");
        }
    };

    useEffect(() => {
        fetchStocks();
    }, []);

    return (
        <div style={styles.container}>
            <button style={styles.interestButton} onClick={() => navigate("/interest")}>
                내 관심종목
            </button>

            <h2 style={styles.title}>전체 종목</h2>
            <ul style={styles.stockList}>
                {stocks.map((stock) => (
                    <li key={stock.code} style={styles.stockItem}>
                        <div style={styles.stockText}>
                            <div style={styles.stockName}>{stock.name}</div>
                            <div style={styles.stockCode}>{stock.code}</div>
                        </div>
                    </li>
                ))}
            </ul>
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
    interestButton: {
        padding: "10px 20px",
        marginBottom: 20,
        fontSize: 14,
        fontWeight: "bold",
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 8,
        cursor: "pointer",
        alignSelf: "flex-end",
    },
    title: {
        fontSize: 22,
        fontWeight: "bold",
        marginBottom: 20,
        textAlign: "center",
    },
    stockList: {
        listStyle: "none",
        padding: 0,
        display: "flex",
        flexDirection: "column",
        gap: 15,
    },
    stockItem: {
        padding: "15px",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#f9f9f9",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
    },
    stockText: {
        display: "flex",
        flexDirection: "column",
    },
    stockName: {
        fontSize: 16,
        fontWeight: "bold",
    },
    stockCode: {
        fontSize: 14,
        color: "#666",
    },
};
