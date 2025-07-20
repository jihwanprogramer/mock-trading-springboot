import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "./api";

export default function InterestStockPage() {
    const [interestStocks, setInterestStocks] = useState([]);
    const [stockName, setStockName] = useState("");
    const [stockCode, setStockCode] = useState("");
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const fetchInterestStocks = async () => {
        try {
            const res = await apiClient.get("api/interests");
            setInterestStocks(res.data.data);
            setError(null);
        } catch {
            setError("관심종목 조회 중 오류가 발생했습니다.");
        }
    };

    useEffect(() => {
        fetchInterestStocks();
    }, []);

    const addInterest = async () => {
        if (!stockName || !stockCode) {
            setError("종목명과 종목코드를 모두 입력하세요.");
            return;
        }
        try {
            await apiClient.post("api/interests", {
                StockName: stockName,
                StockCode: stockCode,
            });
            setStockName("");
            setStockCode("");
            setError(null);
            fetchInterestStocks();
        } catch (err) {
            setError(err.response?.data?.message || "관심종목 등록에 실패했습니다.");
        }
    };

    const deleteInterest = async (id) => {
        if (!window.confirm("정말 삭제하시겠습니까?")) return;

        try {
            await apiClient.delete(`/api/interests/${id}`);
            setError(null);
            fetchInterestStocks();
        } catch {
            setError("관심종목 삭제 중 오류가 발생했습니다.");
        }
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>관심종목 관리</h2>
            {error && <div style={styles.error}>{error}</div>}
            <div style={styles.form}>
                <input type="text" placeholder="종목명" value={stockName} onChange={(e) => setStockName(e.target.value)}
                       style={styles.input}/>
                <input type="text" placeholder="종목코드" value={stockCode} onChange={(e) => setStockCode(e.target.value)}
                       style={styles.input}/>
                <button onClick={addInterest} style={styles.button}>추가</button>
            </div>
            <ul style={styles.list}>
                {interestStocks.length === 0 && <li>등록된 관심종목이 없습니다.</li>}
                {interestStocks.map((item) => (
                    <li key={item.id} style={styles.listItem}>
                        {item.stockName} ({item.stockCode})
                        <div>
                            <button onClick={() => navigate(`/chart`)} style={styles.chartButton}>차트
                            </button>
                            <button onClick={() => deleteInterest(item.id)} style={styles.deleteButton}>삭제</button>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
}

const styles = {
    container: {
        maxWidth: 600,
        margin: "30px auto",
        padding: 30,
        border: "1px solid #ddd",
        borderRadius: 10,
        fontFamily: "sans-serif",
        backgroundColor: "#fafafa"
    },
    title: {marginBottom: 20, fontSize: 24, fontWeight: "bold"},
    error: {marginBottom: 15, color: "red"},
    form: {display: "flex", gap: 10, marginBottom: 25},
    input: {flex: 1, padding: "12px 15px", fontSize: 14, border: "1px solid #ddd", borderRadius: 10, outline: "none"},
    button: {
        padding: "12px 25px",
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: "bold"
    },
    list: {listStyleType: "none", paddingLeft: 0},
    listItem: {marginBottom: 12, fontSize: 16, display: "flex", justifyContent: "space-between", alignItems: "center"},
    chartButton: {
        marginLeft: 8,
        padding: "6px 12px",
        backgroundColor: "#eee",
        border: "1px solid #ccc",
        borderRadius: 6,
        cursor: "pointer",
        fontSize: 13
    },
    deleteButton: {
        marginLeft: 8,
        backgroundColor: "transparent",
        border: "none",
        color: "red",
        cursor: "pointer",
        fontWeight: "bold"
    }
};
