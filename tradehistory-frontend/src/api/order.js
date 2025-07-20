import axios from 'axios';

const API = '/api';

export const placeMarketOrder = async (accountId, type, data, token) => {
    const url = `${API}/accounts/${accountId}/orders/market_${type}`;
    return axios.post(url, data, {
        headers: { Authorization: `Bearer ${token}` },
    });
};

export const placeLimitOrder = async (accountId, type, data, token) => {
    const url = `${API}/accounts/${accountId}/orders/limit_${type}`;
    return axios.post(url, data, {
        headers: { Authorization: `Bearer ${token}` },
    });
};

export const getOrderList = async (accountId, token) => {
    return axios.get(`${API}/accounts/${accountId}/orders`, {
        headers: { Authorization: `Bearer ${token}` },
    });
};

export const cancelOrder = async (accountId, orderId, token) => {
    return axios.delete(`${API}/accounts/${accountId}/orders/cancel/${orderId}`, {
        headers: { Authorization: `Bearer ${token}` },
    });
};
