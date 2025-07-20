import axios from 'axios';

const API = '/api';

export const getTradeList = async (accountId, token) => {
    return axios.post(`${API}/accounts/${accountId}/trades`, null, {
        headers: { Authorization: `Bearer ${token}` },
    });
};
