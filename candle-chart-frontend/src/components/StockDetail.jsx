import React, {useCallback, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import apiClient from "./api";

export default function StockDetail() {
    const {stockCode} = useParams();
    const [stock, setStock] = useState(null);

    const fetchStockDetail = useCallback(async () => {
        try {
            const res = await apiClient.get(`/api/v1/stocks/${stockCode}`);
            setStock(res.data);
        } catch (err) {
            console.error("종목 상세 조회 실패:", err);
            alert("종목 정보를 불러오지 못했습니다.");
        }
    }, [stockCode]);

    useEffect(() => {
        fetchStockDetail();
    }, [fetchStockDetail]);

    if (!stock) return <div className="p-4">로딩 중...</div>;

    return (
        <div className="p-4 max-w-md mx-auto">
            <h2 className="text-xl font-bold">
                {stock.name} ({stock.code})
            </h2>
            <p className="text-lg text-gray-700 mb-2">₩{stock.price.toLocaleString()}</p>
            <div className="bg-gray-100 h-40 mb-4 rounded-md flex items-center justify-center text-gray-400">
                차트 영역
            </div>
            <div className="flex gap-4">
                <button className="bg-green-500 text-white p-2 rounded-md w-full">매수</button>
                <button className="bg-red-500 text-white p-2 rounded-md w-full">매도</button>
            </div>
        </div>
    );
}
