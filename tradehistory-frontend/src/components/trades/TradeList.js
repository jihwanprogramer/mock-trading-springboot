import React, { useEffect, useState } from 'react';
import { getTradeList } from '../../api/trade';

const TradeList = ({ accountId, token }) => {
    const [trades, setTrades] = useState([]);

    useEffect(() => {
        const fetch = async () => {
            try {
                const res = await getTradeList(accountId, token);
                setTrades(res.data.data.content || []);
            } catch {
                alert('체결 내역 조회 실패');
            }
        };
        fetch();
    }, []);

    return (
        <div>
            {trades.map((trade) => (
                <div key={trade.tradeId}>
                    {trade.stockName} / {trade.price} / {trade.quantity} / {trade.tradeTime}
                </div>
            ))}
        </div>
    );
};

export default TradeList;
