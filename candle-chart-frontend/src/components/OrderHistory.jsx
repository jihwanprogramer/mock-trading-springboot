import React, {useEffect, useState} from "react";
import apiClient from "./api";

export default function OrderHistory() {
    const [orders, setOrders] = useState([]);

    const fetchOrders = async () => {
        try {
            const res = await apiClient.get("/api/v1/trade/orders");
            setOrders(res.data);
        } catch (err) {
            console.error("주문 내역 실패:", err);
            alert("주문 내역을 불러오지 못했습니다.");
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    return (
        <div className="p-4 max-w-md mx-auto">
            <h2 className="text-lg font-bold mb-4">주문 내역</h2>
            <ul className="space-y-2">
                {orders.map((order, i) => (
                    <li key={i} className="border p-3 rounded-md">
                        {order.stock} | {order.type} | {order.quantity}개 |
                        ₩{order.price.toLocaleString()} | {order.status}
                    </li>
                ))}
            </ul>
        </div>
    );
}