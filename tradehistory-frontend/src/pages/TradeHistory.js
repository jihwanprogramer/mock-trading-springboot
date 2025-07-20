import React from 'react';
import TradeList from '../components/trades/TradeList';

const TradeHistory = () => {
    const accountId = 1;
    const token = localStorage.getItem('accessToken');

    return (
        <div>
            <h2>체결 내역</h2>
            <TradeList accountId={accountId} token={token} />
        </div>
    );
};

export default TradeHistory;
