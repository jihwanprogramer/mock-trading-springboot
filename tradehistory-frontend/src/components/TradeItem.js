import React from "react";

export default function TradeItem({ trade }) {
    return (
        <div className="border p-3 rounded-md shadow-sm mb-3">
            <p><strong>종목:</strong> {trade.stockName}</p>
            <p><strong>수량:</strong> {trade.quantity}</p>
            <p><strong>가격:</strong> {trade.price}</p>
            <p><strong>타입:</strong> {trade.orderType}</p>
            <p><strong>체결일:</strong> {trade.traderDate}</p>
        </div>
    );
}
