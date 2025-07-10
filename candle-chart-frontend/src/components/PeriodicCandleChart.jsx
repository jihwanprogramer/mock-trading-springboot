import React, {useEffect, useRef, useState} from "react";
import apiClient from "./api";
import {createChart} from "lightweight-charts";

const candleOptions = [
    {label: "일봉", value: "D"},
    {label: "주봉", value: "W"},
    {label: "월봉", value: "M"},
    {label: "년봉", value: "Y"},
];

const PeriodicCandleChart = () => {
    const chartContainerRef = useRef();
    const candleSeriesRef = useRef();
    const chartRef = useRef(null);

    const [stockCode, setStockCode] = useState("");
    const [candleType, setCandleType] = useState("D");
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    // 차트 초기화 (마운트 시)
    useEffect(() => {
        if (!chartContainerRef.current) return;

        const chart = createChart(chartContainerRef.current, {
            width: 600,
            height: 400,
            layout: {
                backgroundColor: "#fafafa",
                textColor: "#000",
            },
            grid: {
                vertLines: {color: "#eee"},
                horzLines: {color: "#eee"},
            },
            priceScale: {
                borderVisible: false,
            },
            timeScale: {
                borderVisible: false,
                timeVisible: true,
                secondsVisible: false,
            },
        });

        const candleSeries = chart.addCandlestickSeries({
            upColor: "#00B15D",
            downColor: "#E44343",
            borderVisible: false,
            wickUpColor: "#00B15D",
            wickDownColor: "#E44343",
        });

        candleSeriesRef.current = candleSeries;
        chartRef.current = chart;

        const handleResize = () => {
            if (chartContainerRef.current) {
                chart.applyOptions({width: chartContainerRef.current.clientWidth});
            }
        };

        window.addEventListener("resize", handleResize);

        return () => {
            window.removeEventListener("resize", handleResize);
            chart.remove();
        };
    }, []);

    // 데이터 조회 함수
    const fetchCandles = async () => {
        if (!stockCode) {
            setErrorMsg("종목코드를 입력해주세요");
            return;
        }
        setLoading(true);
        setErrorMsg("");
        try {
            const res = await apiClient.get(`/api/period/${stockCode}/candle/${candleType}`);
            const data = res.data.data || [];

            if (data.length === 0) {
                setErrorMsg(`데이터가 없습니다. (DB에 해당 종목의 ${candleType}봉 데이터가 존재하지 않음)`);
                candleSeriesRef.current.setData([]);
            } else {
                // 시간 기준 오름차순 정렬 및 중복 제거
                const processedData = data
                    .map(c => ({
                        time: Math.floor(new Date(c.date).getTime() / 1000), // timestamp 초단위
                        open: c.openingPrice,
                        high: c.highPrice,
                        low: c.lowPrice,
                        close: c.closingPrice,
                    }))
                    .sort((a, b) => a.time - b.time)
                    .filter((item, index, arr) => index === 0 || item.time !== arr[index - 1].time);

                candleSeriesRef.current.setData(processedData);
                chartRef.current.timeScale().fitContent();
            }
        } catch (err) {
            console.error("차트 데이터 요청 실패:", err);
            setErrorMsg("서버와 연결에 실패했습니다.");
            candleSeriesRef.current.setData([]);
        }
        setLoading(false);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        fetchCandles();
    };

    return (
        <div style={styles.container}>
            <div style={{textAlign: "center", marginBottom: 20}}>
                <img src="/logo.png" alt="앱 로고" style={{width: 120, height: "auto"}}/>
            </div>
            <h2 style={styles.title}>기간별 봉 차트 조회</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="text"
                    value={stockCode}
                    onChange={(e) => setStockCode(e.target.value)}
                    placeholder="종목코드 입력"
                    style={styles.input}
                    required
                />
                <select value={candleType} onChange={(e) => setCandleType(e.target.value)} style={styles.select}>
                    {candleOptions.map((opt) => (
                        <option key={opt.value} value={opt.value}>
                            {opt.label}
                        </option>
                    ))}
                </select>
                <button type="submit" style={styles.button}>
                    조회
                </button>
            </form>
            {loading && <p>로딩 중...</p>}
            {!loading && errorMsg && <p style={{color: "red"}}>{errorMsg}</p>}
            <div ref={chartContainerRef}/>
        </div>
    );
};

const styles = {
    container: {
        maxWidth: 600,
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
    select: {
        padding: 12,
        fontSize: 16,
        borderRadius: 10,
        border: "1px solid #ddd",
        outline: "none",
        backgroundColor: "#fff",
        cursor: "pointer",
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
};

export default PeriodicCandleChart;
