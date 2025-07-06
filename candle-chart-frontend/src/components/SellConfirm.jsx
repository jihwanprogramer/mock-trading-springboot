import React, {useState} from "react";
import apiClient from "./api";

export default function SellConfirm() {
    const [stockCode] = useState("AAPL");
    const [quantity] = useState(2);

    const handleSell = async () => {
        try {
            await apiClient.post("/api/v1/trade/sell", {
                stockCode,
                quantity,
            });
            alert("매도 완료");
        } catch (err) {
            console.error("매도 실패:", err);
            alert("매도에 실패했습니다.");
        }
    };

    return (
        <div className="p-4 max-w-md mx-auto">
            <h2 className="text-lg font-semibold mb-4">매도 확인</h2>
            <div className="mb-2">종목: {stockCode}</div>
            <div className="mb-2">수량: {quantity}개</div>
            <div className="mb-4">예상 금액: ₩{(quantity * 142650).toLocaleString()}</div>
            <button onClick={handleSell} className="bg-red-600 text-white w-full p-3 rounded-md">
                매도하기
            </button>
        </div>
    );
}
