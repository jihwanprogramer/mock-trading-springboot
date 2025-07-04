//종목리스트조회 메인 홈 화면
import React, {useEffect, useState} from "react";
import apiClient from "./api";

export default function Home() {
    const [stocks, setStocks] = useState([]);

    const fetchStocks = async () => {
        try {
            const res = await apiClient.get("/api/v1/stocks");
            setStocks(res.data); // [{code, name, price, rate}, ...]
        } catch (err) {
            console.error("종목 불러오기 실패:", err);
            alert("종목 정보를 가져오지 못했습니다.");
        }
    };

    useEffect(() => {
        fetchStocks();
    }, []);

    return (
        <div className="p-4 max-w-md mx-auto">
            <h2 className="text-lg font-semibold mb-2">전체 종목</h2>
            {stocks.map((stock) => (
                <div key={stock.code} className="border p-3 rounded-md mb-2 flex justify-between items-center">
                    <div>
                        <h4 className="font-semibold">{stock.name}</h4>
                        <p className="text-sm text-gray-500">₩{stock.price.toLocaleString()}</p>
                    </div>
                    <span className={stock.rate >= 0 ? "text-green-600" : "text-red-500"}>
            {stock.rate >= 0 ? "▲" : "▼"} {stock.rate}%
          </span>
                </div>
            ))}
        </div>
    );
}