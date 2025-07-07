import React, { useEffect, useState } from 'react';
import { getOrderList, cancelOrder } from '../../api/order';

const OrderList = ({ accountId, token }) => {
    const [orders, setOrders] = useState([]);

    const fetchOrders = async () => {
        try {
            const res = await getOrderList(accountId, token);
            setOrders(res.data.data.content || []);
        } catch (err) {
            alert('주문 조회 실패');
        }
    };

    const handleCancel = async (orderId) => {
        try {
            await cancelOrder(accountId, orderId, token);
            fetchOrders();
        } catch {
            alert('취소 실패');
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    return (
        <div>
            {orders.map((order) => (
                <div key={order.orderId}>
                    {order.stockName} / {order.quantity} / {order.orderStatus}
                    <button onClick={() => handleCancel(order.orderId)}>취소</button>
                </div>
            ))}
        </div>
    );
};

export default OrderList;
