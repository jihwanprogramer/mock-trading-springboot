import React, {useEffect, useState} from "react";
import axios from "axios";
import {Chart as ChartJS, LinearScale, TimeScale, Title, Tooltip} from "chart.js";
import {Chart} from "react-chartjs-2";
import {CandlestickController, CandlestickElement} from "chartjs-chart-financial";
import 'chartjs-adapter-date-fns';

ChartJS.register(TimeScale, LinearScale, Tooltip, Title, CandlestickController, CandlestickElement);

const CandleChart = ({stockCode = "000150", interval = 1}) => {
    const [chartData, setChartData] = useState(null);
    const [stockName, setStockName] = useState("");

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

    useEffect(() => {
        axios.get(`/api/stocks/${stockCode}/candles`, {
            params: {date: "20250609", interval}
        })
            .then(res => {
                if (!res.data || res.data.length === 0) {
                    setChartData(null);
                    setStockName("");
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
            })
            .catch(err => {
                console.error("차트 데이터 요청 실패:", err);
                setStockName("");
            });
    }, [stockCode, interval]);

    if (!chartData) return <div>Loading chart...</div>;

    return (
        <Chart
            type="candlestick"
            data={chartData}
            options={options}
        />
    );
};

export default CandleChart;
