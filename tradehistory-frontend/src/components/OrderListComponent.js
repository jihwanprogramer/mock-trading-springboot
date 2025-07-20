import React, { useEffect, useState } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export default function OrderListComponent({ accountId }) {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchOrders = async () => {
        try {
            setLoading(true);
            const res = await axios.get(`/api/accounts/${accountId}/orders`);
            setOrders(res.data.data.content);
        } catch (error) {
            console.error("Error fetching orders", error);
            alert("주문 목록을 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const cancelOrder = async (orderId) => {
        try {
            await axios.delete(`/api/accounts/${accountId}/orders/cancel/${orderId}`);
            alert("주문이 취소되었습니다.");
            fetchOrders();
        } catch (error) {
            console.error("Error canceling order", error);
            alert("주문 취소 실패");
        }
    };

    useEffect(() => {
        fetchOrders();
    }, [accountId]);

    return (
        <div className="p-4 space-y-4">
            <h2 className="text-xl font-bold">주문 내역</h2>
            {loading ? (
                <p>로딩 중...</p>
            ) : (
                orders.map((order) => (
                    <Card key={order.id} className="p-4">
                        <CardContent>
                            <p>종목: {order.stockName}</p>
                            <p>타입: {order.orderType}</p>
                            <p>상태: {order.orderStatus}</p>
                            <p>수량: {order.quantity}</p>
                            <p>가격: {order.price}</p>
                            <p>주문일: {order.createdAt}</p>
                            {order.orderStatus !== "SETTLE" && (
                                <Button
                                    className="mt-2"
                                    onClick={() => cancelOrder(order.id)}
                                >
                                    주문 취소
                                </Button>
                            )}
                        </CardContent>
                    </Card>
                ))
            )}
        </div>
    );
}
