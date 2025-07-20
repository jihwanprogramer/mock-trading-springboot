import React, {useEffect, useState} from "react";
import apiClient from "./api";

export default function TradeHistory() {
    const [trades, setTrades] = useState([]);

    const fetchTrades = async () => {
        try {
            const res = await apiClient.get("/api/v1/trade/history");
            setTrades(res.data); // [{stock, type, price, quantity, status}]
        } catch (err) {
            console.error("체결 내역 조회 실패:", err);
            alert("체결 내역을 불러오지 못했습니다.");
        }
    };

    useEffect(() => {
        fetchTrades();
    }, []);

    return (
        <div className="p-4 max-w-md mx-auto">
            <h2 className="text-lg font-bold mb-4">체결 내역</h2>
            <ul className="space-y-2">
                {trades.map((t, i) => (
                    <li key={i} className="border p-3 rounded-md">
                        {t.stock} | {t.type === "BUY" ? "매수" : "매도"} | {t.quantity}개 |
                        ₩{t.price.toLocaleString()} | {t.status}
                    </li>
                ))}
            </ul>
        </div>
    );
}