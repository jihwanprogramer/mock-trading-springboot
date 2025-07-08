import React, { useState } from 'react';
import axios from 'axios';

const OrderComponent = ({ accountId }) => {
    const [type, setType] = useState('MARKET_BUY');
    const [price, setPrice] = useState('');
    const [quantity, setQuantity] = useState('');
    const [response, setResponse] = useState(null);

    const accessToken = localStorage.getItem('accessToken');

    const handleSubmit = async () => {
        try {
            const headers = {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            };

            const url =
                type === 'MARKET_BUY'
                    ? `/api/accounts/${accountId}/orders/market_buy`
                    : type === 'MARKET_SELL'
                        ? `/api/accounts/${accountId}/orders/market_sell`
                        : type === 'LIMIT_BUY'
                            ? `/api/accounts/${accountId}/orders/limit_buy`
                            : `/api/accounts/${accountId}/orders/limit_sell`;

            const requestBody = type.includes('LIMIT')
                ? { stockId: 1, price: Number(price), quantity: Number(quantity) }
                : { stockId: 1, quantity: Number(quantity) };

            const res = await axios.post(url, requestBody, { headers });
            setResponse(res.data);
        } catch (err) {
            console.error(err);
            alert('주문 실패');
        }
    };

    return (
        <div className="p-4 max-w-xl mx-auto">
            <h2 className="text-xl font-bold mb-2">주문하기</h2>
            <select value={type} onChange={(e) => setType(e.target.value)} className="border rounded p-2 mb-2 w-full">
                <option value="MARKET_BUY">시장가 매수</option>
                <option value="MARKET_SELL">시장가 매도</option>
                <option value="LIMIT_BUY">지정가 매수</option>
                <option value="LIMIT_SELL">지정가 매도</option>
            </select>

            {type.includes('LIMIT') && (
                <input
                    type="number"
                    placeholder="가격"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    className="border rounded p-2 mb-2 w-full"
                />
            )}

            <input
                type="number"
                placeholder="수량"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                className="border rounded p-2 mb-4 w-full"
            />

            <button onClick={handleSubmit} className="bg-blue-600 text-white px-4 py-2 rounded">
                주문하기
            </button>

            {response && (
                <div className="mt-4 p-4 bg-gray-100 rounded">
                    <pre>{JSON.stringify(response, null, 2)}</pre>
                </div>
            )}
        </div>
    );
};

export default OrderComponent;
