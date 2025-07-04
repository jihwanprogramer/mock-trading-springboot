import React, {useEffect, useState} from "react";
import axios from "axios";
import {Chart as ChartJS, LinearScale, TimeScale, Title, Tooltip} from "chart.js";
import {Chart} from "react-chartjs-2";
import {CandlestickController, CandlestickElement} from "chartjs-chart-financial";
import 'chartjs-adapter-date-fns';

ChartJS.register(
    CandlestickController, CandlestickElement,
    LinearScale, TimeScale, Title, Tooltip
);

const candleOptions = [
    {label: "일봉", value: "day"},
    {label: "주봉", value: "week"},
    {label: "월봉", value: "month"},
    {label: "년봉", value: "year"},
];

const PeriodicCandleChart = () => {
    const [stockCode, setStockCode] = useState("000150");
    const [candleType, setCandleType] = useState("day");
    const [candles, setCandles] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchCandles = async () => {
            setLoading(true);
            try {
                const res = await axios.get(`/api/period/${stockCode}/candle/${candleType}`);
                setCandles(res.data.data || []);
            } catch (err) {
                console.error("차트 데이터 요청 실패:", err);
                setCandles([]);
            }
            setLoading(false);
        };

        fetchCandles();
    }, [stockCode, candleType]);

    const handleSubmit = (e) => {
        e.preventDefault();
        // stockCode, candleType 상태 변경 시 useEffect에서 자동 호출됨
    };

    const data = {
        datasets: [{
            label: `${candleType}봉`,
            data: candles.map(c => ({
                x: c.date,
                o: c.openingPrice,
                h: c.highPrice,
                l: c.lowPrice,
                c: c.closingPrice,
            })),
            color: {
                up: "#00B15D",
                down: "#E44343",
                unchanged: "#999999",
            },
        }]
    };

    const options = {
        responsive: true,
        plugins: {
            tooltip: {enabled: true},
            title: {
                display: true,
                text: `${stockCode} - ${candleType}봉 차트`,
                font: {size: 18, weight: 'bold'}
            }
        },
        scales: {
            x: {
                type: "time",
                time: {
                    unit: candleType === "day" ? "day"
                        : candleType === "week" ? "week"
                            : candleType === "month" ? "month"
                                : "year",
                    tooltipFormat: "yyyy-MM-dd"
                },
                grid: {display: false},
                ticks: {font: {size: 12}}
            },
            y: {
                beginAtZero: false,
                grid: {color: "rgba(200,200,200,0.2)", borderDash: [3, 3]},
                ticks: {
                    font: {size: 12},
                    callback: val => val.toLocaleString(),
                },
                title: {
                    display: true,
                    text: "Price",
                    font: {size: 14, weight: "bold"},
                }
            }
        }
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>기간별 봉 차트 조회</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="text"
                    value={stockCode}
                    onChange={e => setStockCode(e.target.value)}
                    placeholder="종목코드 입력"
                    style={styles.input}
                    required
                />
                <select
                    value={candleType}
                    onChange={e => setCandleType(e.target.value)}
                    style={styles.select}
                >
                    {candleOptions.map(opt => (
                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                </select>
                <button type="submit" style={styles.button}>조회</button>
            </form>

            {loading && <p>로딩 중...</p>}

            {!loading && candles.length === 0 && <p>데이터가 없습니다.</p>}

            {!loading && candles.length > 0 && (
                <Chart type="candlestick" data={data} options={options}/>
            )}
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
    }
};

export default PeriodicCandleChart;
