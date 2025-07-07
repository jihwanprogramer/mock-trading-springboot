import React, {useEffect, useRef, useState} from "react";
import apiClient from "./api";
import {createChart} from "lightweight-charts";

const periodOptions = [
    {label: "일", value: "D"},
    {label: "주", value: "W"},
    {label: "월", value: "M"},
    {label: "년", value: "Y"},
];

const CombinedChart = () => {
    const chartContainerRef = useRef(null);
    const chartRef = useRef(null);
    const candleSeriesRef = useRef(null);
    const maSeriesRef = useRef(null);
    const periodicCandleSeriesRef = useRef(null);

    const [stockName, setStockName] = useState("");
    const [date, setDate] = useState("");
    const [interval, setInterval] = useState(1);
    const [period, setPeriod] = useState("D");
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    useEffect(() => {
        if (!chartContainerRef.current) return;

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: 400,
            layout: {
                backgroundColor: "#fafafa",
                textColor: "#000",
            },
            grid: {
                vertLines: {color: "#eee"},
                horzLines: {color: "#eee"},
            },
            priceScale: {borderVisible: false},
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

        const maSeries = chart.addLineSeries({
            color: "rgba(68, 97, 242, 0.5)",
        });

        const periodicCandleSeries = chart.addCandlestickSeries({
            upColor: "rgba(0, 0, 255, 0.1)",
            downColor: "rgba(255, 0, 0, 0.1)",
            borderVisible: false,
            wickUpColor: "rgba(0, 0, 255, 0.05)",
            wickDownColor: "rgba(255, 0, 0, 0.05)",
        });

        chartRef.current = chart;
        candleSeriesRef.current = candleSeries;
        maSeriesRef.current = maSeries;
        periodicCandleSeriesRef.current = periodicCandleSeries;

        const handleResize = () => {
            if (chartContainerRef.current) {
                chart.applyOptions({width: chartContainerRef.current.clientWidth});
            }
        };

        window.addEventListener("resize", handleResize);
        handleResize();

        return () => {
            window.removeEventListener("resize", handleResize);
            chart.remove();
        };
    }, []);

    const fetchChartData = async () => {
        if (!stockName || !date) return;
        setLoading(true);
        setErrorMsg("");

        try {
            const dateStr = date.replace(/-/g, "");

            let intraData = [];
            if (period === "D") {
                const intraRes = await apiClient.get(`/api/intra/stocks/${stockName}/candles`, {
                    params: {date: dateStr, interval},
                });
                const intraRaw = intraRes.data?.data || intraRes.data || [];
                intraData = intraRaw
                    .map((c) => ({
                        time: Math.floor(new Date(c.timeStamp).getTime() / 1000),
                        open: c.openingPrice,
                        high: c.highPrice,
                        low: c.lowPrice,
                        close: c.closingPrice,
                    }))
                    .sort((a, b) => a.time - b.time)
                    .filter((v, i, arr) => i === 0 || v.time !== arr[i - 1].time);
                console.log("✅ 가공된 intraData:", intraData);
                console.log("raw intraRes:", intraRes.data);

            }

            const periodRes = await apiClient.get(`/api/period/${stockName}/candle/${period}`);
            const periodRaw = periodRes.data?.data || [];
            const periodicCandles = periodRaw
                .map((c) => ({
                    time: Math.floor(new Date(c.date).getTime() / 1000),
                    open: c.openingPrice,
                    high: c.highPrice,
                    low: c.lowPrice,
                    close: c.closingPrice,
                }))
                .sort((a, b) => a.time - b.time)
                .filter((v, i, arr) => i === 0 || v.time !== arr[i - 1].time);

            const maPoints = periodRaw
                .map((c, idx, arr) => {
                    if (idx < 4 || !c.date) return null;
                    const slice = arr.slice(idx - 4, idx + 1);
                    const avg = slice.reduce((sum, item) => sum + item.closingPrice, 0) / 5;
                    return {
                        time: Math.floor(new Date(c.date).getTime() / 1000),
                        value: Number(avg.toFixed(2)),
                    };
                })
                .filter(Boolean);

            candleSeriesRef.current.setData(intraData);
            maSeriesRef.current.setData(maPoints);
            periodicCandleSeriesRef.current.setData(periodicCandles);
            chartRef.current.timeScale().fitContent();
        } catch (err) {
            console.error("데이터 불러오기 실패:", err);
            setErrorMsg("차트 데이터를 불러오는 데 실패했습니다.");
            candleSeriesRef.current.setData([]);
            maSeriesRef.current.setData([]);
            periodicCandleSeriesRef.current.setData([]);
        }

        setLoading(false);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        fetchChartData();
    };

    return (
        <div style={styles.container}>
            <div style={{textAlign: "center", marginBottom: 20}}>
                <img src="/logo.png" alt="앱 로고" style={{width: 120, height: "auto"}}/>
            </div>
            <h2 style={styles.title}>한국투자증권 종합지수</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    value={stockName}
                    onChange={(e) => setStockName(e.target.value)}
                    placeholder="종목명"
                    required
                    style={styles.input}
                />
                <select
                    value={interval}
                    onChange={(e) => setInterval(Number(e.target.value))}
                    style={styles.select}
                    disabled={period !== "D"}
                    title={period !== "D" ? "1,3,5분 단위는 일봉 선택 시만 사용" : ""}
                >
                    <option value={1}>1분</option>
                    <option value={3}>3분</option>
                    <option value={5}>5분</option>
                </select>
                <input
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    required
                    style={styles.dateInput}
                />
                <select value={period} onChange={(e) => setPeriod(e.target.value)} style={styles.select}>
                    {periodOptions.map((opt) => (
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
            <div ref={chartContainerRef} style={{width: "100%", height: 400}}/>
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
        flexWrap: "wrap",
    },
    input: {
        flex: 1,
        padding: 12,
        fontSize: 16,
        borderRadius: 10,
        border: "1px solid #ddd",
        outline: "none",
        minWidth: 150,
    },
    dateInput: {
        width: 150,
        padding: 12,
        fontSize: 16,
        borderRadius: 10,
        border: "1px solid #ddd",
        outline: "none",
    },
    select: {
        width: 100,
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
        minWidth: 80,
        marginLeft: "265px",
    },
};

export default CombinedChart;
