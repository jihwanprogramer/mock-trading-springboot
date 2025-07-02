import React, {useCallback, useEffect, useState} from "react";
import axios from "axios";
import {Chart as ChartJS, LinearScale, TimeScale, Title, Tooltip} from "chart.js";
import {Chart} from "react-chartjs-2";
import {CandlestickController, CandlestickElement} from "chartjs-chart-financial";
import 'chartjs-adapter-date-fns';

ChartJS.register(TimeScale, LinearScale, Tooltip, Title, CandlestickController, CandlestickElement);

const CandleChart = () => {
    const [stockCode, setStockCode] = useState("000150");
    const [interval, setInterval] = useState(1);
    const [chartData, setChartData] = useState(null);
    const [stockName, setStockName] = useState("");
    const [loading, setLoading] = useState(false);

    const options = {
        responsive: true,
        interaction: {
            mode: 'nearest',
            intersect: false,
        },
        plugins: {
            tooltip: {
                enabled: true,
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: ctx => {
                        const o = ctx.raw.o.toLocaleString();
                        const h = ctx.raw.h.toLocaleString();
                        const l = ctx.raw.l.toLocaleString();
                        const c = ctx.raw.c.toLocaleString();
                        return `O: ${o}  H: ${h}  L: ${l}  C: ${c}`;
                    }
                }
            },
            legend: {
                display: false,
            },
            title: {
                display: true,
                text: stockName ? `${stockName} (${stockCode}) ${interval}분봉` : `${stockCode} ${interval}분봉`,
                font: {size: 16, weight: 'bold'},
            }
        },
        scales: {
            x: {
                type: 'time',
                time: {
                    unit: 'minute',
                    tooltipFormat: 'yyyy-MM-dd HH:mm',
                    displayFormats: {
                        minute: 'HH:mm',
                        hour: 'HH:mm',
                    },
                },
                grid: {
                    display: false,
                },
                ticks: {
                    maxRotation: 0,
                    autoSkip: true,
                    maxTicksLimit: 10,
                    font: {size: 12},
                },
            },
            y: {
                beginAtZero: false,
                grid: {
                    color: 'rgba(200,200,200,0.2)',
                    borderDash: [3, 3],
                },
                ticks: {
                    font: {size: 12},
                    callback: val => val.toLocaleString(),
                },
                title: {
                    display: true,
                    text: 'Price',
                    font: {size: 14, weight: 'bold'},
                },
            },
        },
    };

    const fetchChartData = useCallback(async () => {
        setLoading(true);
        try {
            const res = await axios.get(`/intra/stocks/${stockCode}/candles`, {
                params: {date: "20250609", interval}
            });

            if (!res.data || res.data.length === 0) {
                setChartData(null);
                setStockName("");
                setLoading(false);
                return;
            }

            setStockName(res.data[0].stockName || "");

            const data = res.data.map(candle => ({
                x: new Date(candle.timeStamp),
                o: candle.openingPrice,
                h: candle.highPrice,
                l: candle.lowPrice,
                c: candle.closingPrice,
            }));

            setChartData({
                datasets: [{
                    label: `${stockCode} ${interval}분봉`,
                    data,
                    borderColor: "rgba(75, 192, 192, 1)"
                }]
            });
        } catch (err) {
            console.error("차트 데이터 요청 실패:", err);
            setChartData(null);
            setStockName("");
        }
        setLoading(false);
    }, [stockCode, interval]);

    useEffect(() => {
        fetchChartData();
    }, [fetchChartData]);

    const handleSubmit = (e) => {
        e.preventDefault();
        fetchChartData();
    };

    return (
        <div style={{maxWidth: 700, margin: "20px auto", fontFamily: "sans-serif"}}>
            <div style={{textAlign: 'center', marginBottom: 20}}>
                <img
                    src="/logo.png"
                    alt="앱 로고"
                    style={{width: 120, height: 'auto'}}
                />
            </div>
            <form onSubmit={handleSubmit} style={{display: "flex", gap: 10, marginBottom: 20}}>
                <input
                    type="text"
                    value={stockCode}
                    onChange={e => setStockCode(e.target.value)}
                    placeholder="종목코드 입력"
                    style={{flex: 1, padding: 8, fontSize: 16, borderRadius: 5, border: "1px solid #ccc"}}
                />
                <select
                    value={interval}
                    onChange={e => setInterval(Number(e.target.value))}
                    style={{padding: 8, fontSize: 16, borderRadius: 5, border: "1px solid #ccc"}}
                >
                    <option value={1}>1분</option>
                    <option value={3}>3분</option>
                    <option value={5}>5분</option>
                </select>
                <button
                    type="submit"
                    style={{
                        padding: "8px 16px",
                        backgroundColor: "#4461F2",
                        color: "#fff",
                        borderRadius: 5,
                        border: "none",
                        cursor: "pointer"
                    }}
                >
                    조회
                </button>
            </form>

            {loading && <p>로딩 중...</p>}

            {!loading && !chartData && <p>데이터가 없습니다.</p>}

            {!loading && chartData && (
                <Chart type="candlestick" data={chartData} options={options}/>
            )}
        </div>
    );
};

export default CandleChart;
