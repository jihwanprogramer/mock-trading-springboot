import React from 'react';
import OrderList from '../components/orders/OrderList';

const Orders = () => {
    const accountId = 1;
    const token = localStorage.getItem('accessToken');

    return (
        <div>
            <h2>주문 내역</h2>
            <OrderList accountId={accountId} token={token} />
        </div>
    );
};

export default Orders;
