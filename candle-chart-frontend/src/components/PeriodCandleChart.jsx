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

function PeriodicCandleChart({stockCode, candleType}) {
    const [candles, setCandles] = useState([]);

    useEffect(() => {
        axios.get(`/period/${stockCode}/candle/${candleType}`)
            .then(res => setCandles(res.data.data))
            .catch(err => console.error(err));
    }, [stockCode, candleType]);

    const data = {
        datasets: [{
            label: `${candleType} Candle`,
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
        plugins: {tooltip: {enabled: true}},
        scales: {
            x: {type: "time", time: {unit: "day"}},
            y: {beginAtZero: false}
        }
    };

    return (
        <div>
            <h2>{stockCode} - {candleType}봉</h2>
            <Chart type="candlestick" data={data} options={options}/>
        </div>
    );
}

export default PeriodicCandleChart;
