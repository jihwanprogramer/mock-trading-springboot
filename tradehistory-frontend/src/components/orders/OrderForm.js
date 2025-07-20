import React, { useState } from 'react';
import { placeMarketOrder, placeLimitOrder } from '../../api/order';

const OrderForm = ({ accountId, token }) => {
    const [orderType, setOrderType] = useState('market'); // or 'limit'
    const [buyOrSell, setBuyOrSell] = useState('buy');
    const [stockCode, setStockCode] = useState('');
    const [quantity, setQuantity] = useState('');
    const [price, setPrice] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        const orderData = { stockCode, quantity, price: Number(price) };

        try {
            if (orderType === 'market') {
                await placeMarketOrder(accountId, buyOrSell, orderData, token);
            } else {
                await placeLimitOrder(accountId, buyOrSell, orderData, token);
            }
            alert('주문이 완료되었습니다.');
        } catch (error) {
            alert('주문 실패: ' + error.response?.data?.message);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <select value={orderType} onChange={(e) => setOrderType(e.target.value)}>
                <option value="market">시장가</option>
                <option value="limit">지정가</option>
            </select>
            <select value={buyOrSell} onChange={(e) => setBuyOrSell(e.target.value)}>
                <option value="buy">매수</option>
                <option value="sell">매도</option>
            </select>
            <input placeholder="종목코드" value={stockCode} onChange={(e) => setStockCode(e.target.value)} required />
            <input placeholder="수량" type="number" value={quantity} onChange={(e) => setQuantity(e.target.value)} required />
            {orderType === 'limit' && (
                <input placeholder="가격" type="number" value={price} onChange={(e) => setPrice(e.target.value)} required />
            )}
            <button type="submit">주문</button>
        </form>
    );
};

export default OrderForm;
