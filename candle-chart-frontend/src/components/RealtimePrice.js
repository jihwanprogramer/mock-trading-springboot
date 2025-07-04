import React, {useEffect, useState} from "react";
import axios from "axios";

const styles = {
    container: {
        maxWidth: 400,
        margin: "30px auto",
        padding: "40px 20px",
        fontFamily: "sans-serif",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#fafafa",
    },
    title: {
        fontSize: 24,
        fontWeight: "bold",
        marginBottom: 20,
    },
    form: {
        width: "100%",
        display: "flex",
        gap: 10,
        marginBottom: 30,
    },
    input: {
        flex: 1,
        padding: 12,
        fontSize: 16,
        borderRadius: 10,
        border: "1px solid #ddd",
        outline: "none",
    },
    button: {
        padding: "12px 25px",
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: "bold",
        fontSize: 16,
    },
    price: {
        fontSize: 28,
        fontWeight: "bold",
        color: "#4461F2",
    },
    error: {
        color: "red",
        marginBottom: 10,
    },
};

function RealtimePrice() {
    const [inputCode, setInputCode] = useState("");
    const [stockCode, setStockCode] = useState(null);
    const [price, setPrice] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!stockCode) return;

        const fetchPrice = async () => {
            try {
                const response = await axios.get(`/api/realtime-price/${stockCode}`);
                setPrice(response.data.data);
                setError(null);
            } catch (err) {
                setError("가격 정보를 불러오는 데 실패했습니다.");
                setPrice(null);
            }
        };

        fetchPrice();

        const intervalId = setInterval(fetchPrice, 5000);

        return () => clearInterval(intervalId);
    }, [stockCode]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (inputCode.trim() === "") {
            alert("종목코드를 입력해주세요.");
            return;
        }
        setStockCode(inputCode.trim());
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>실시간 현재가 조회</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="text"
                    placeholder="종목코드 입력"
                    value={inputCode}
                    onChange={(e) => setInputCode(e.target.value)}
                    style={styles.input}
                />
                <button type="submit" style={styles.button}>
                    조회
                </button>
            </form>

            {stockCode && <h3>{stockCode} 현재가</h3>}

            {error && <p style={styles.error}>{error}</p>}

            {price !== null ? (
                <p style={styles.price}>{price.toLocaleString()} 원</p>
            ) : stockCode ? (
                <p>가격 정보를 불러오는 중...</p>
            ) : (
                <p>종목코드를 입력하고 조회 버튼을 눌러주세요.</p>
            )}
        </div>
    );
}

export default RealtimePrice;
