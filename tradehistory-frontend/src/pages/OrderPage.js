import React from 'react';
import OrderForm from '../components/orders/OrderForm';

const OrderPage = () => {
    const accountId = 1; // 실제 로그인된 계정 ID
    const token = localStorage.getItem('accessToken');

    return (
        <div>
            <h2>주문 페이지</h2>
            <OrderForm accountId={accountId} token={token} />
        </div>
    );
};

export default OrderPage;
