import React, {useCallback, useEffect, useRef, useState} from "react";
import apiClient from "./api";

const RealtimePrice = () => {
    const [stockName, setStockName] = useState("");
    const [price, setPrice] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [autoRefresh, setAutoRefresh] = useState(false);
    const intervalRef = useRef(null);

    const fetchPrice = useCallback(async () => {
        if (!stockName.trim()) {
            setError("종목명을 입력해주세요.");
            setPrice(null);
            return;
        }

        setLoading(true);
        setError("");

        try {
            const res = await apiClient.get(`/api/price`, {
                params: {stockName},
            });

            if (!res.data) {
                setPrice(null);
                setError("가격 정보 없음");
            } else {
                setPrice(res.data);
            }
        } catch (err) {
            console.error("실시간 가격 조회 실패:", err);
            if (err.response?.status === 403) {
                setError("해당 종목의 가격 정보가 없습니다.");
            } else if (err.response?.status === 404) {
                setError("해당 종목을 찾을 수 없습니다.");
            } else {
                setError("가격 조회 실패");
            }
            setPrice(null);
        }

        setLoading(false);
    }, [stockName]);

    const handleSubmit = (e) => {
        e.preventDefault();
        fetchPrice();
        setAutoRefresh(true);
    };

    useEffect(() => {
        if (autoRefresh && stockName.trim()) {
            intervalRef.current = setInterval(() => {
                fetchPrice();
            }, 5000);

            return () => clearInterval(intervalRef.current);
        }
    }, [autoRefresh, stockName, fetchPrice]);

    useEffect(() => {
        return () => clearInterval(intervalRef.current);
    }, []);

    return (
        <div style={styles.container}>
            <img src="/logo.png" alt="로고" style={styles.logo}/>
            <h2 style={styles.title}>실시간 주가 조회</h2>

            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="text"
                    value={stockName}
                    onChange={(e) => {
                        setStockName(e.target.value);
                        setAutoRefresh(false);
                        clearInterval(intervalRef.current);
                    }}
                    placeholder="종목명 입력"
                    style={styles.input}
                />
                <button type="submit" style={styles.button}>
                    조회
                </button>
            </form>

            {stockName && <h3 style={styles.subtitle}>{stockName} 현재가</h3>}
            {error && <p style={styles.error}>{error}</p>}
            {!error && loading && <p>불러오는 중...</p>}
            {!error && !loading && !isNaN(Number(price)) && (
                <p style={styles.price}>{Number(price).toLocaleString()} 원</p>
            )}
        </div>
    );
};

const styles = {
    container: {
        maxWidth: 500,
        margin: "30px auto",
        padding: "40px 20px",
        fontFamily: "sans-serif",
        textAlign: "center",
        backgroundColor: "#f9f9f9",
        borderRadius: 10,
        border: "1px solid #ddd",
    },
    logo: {
        width: 80,
        height: "auto",
        marginBottom: 20,
    },
    title: {
        fontSize: 22,
        fontWeight: "bold",
        marginBottom: 10,
    },
    subtitle: {
        fontSize: 18,
        fontWeight: "bold",
        marginTop: 30,
    },
    form: {
        display: "flex",
        gap: 10,
        marginTop: 20,
        justifyContent: "center",
    },
    input: {
        padding: "10px",
        fontSize: 14,
        borderRadius: 8,
        border: "1px solid #ccc",
        flex: 1,
    },
    button: {
        padding: "10px 20px",
        fontSize: 14,
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 8,
        cursor: "pointer",
        fontWeight: "bold",
    },
    price: {
        fontSize: 24,
        fontWeight: "bold",
        marginTop: 10,
        color: "#333",
    },
    error: {
        color: "red",
        fontSize: 14,
        marginTop: 10,
    },
};

export default RealtimePrice;
