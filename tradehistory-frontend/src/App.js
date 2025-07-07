import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import OrderPage from './pages/OrderPage';
import TradeHistory from './pages/TradeHistory';
import Orders from "./pages/Orders";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* 루트 주소에 들어왔을 때 보여줄 컴포넌트 등록 */}
                <Route path="/" element={<OrderPage />} />
                <Route path="/orders" element={<Orders />} />
                <Route path="/trade-history" element={<TradeHistory />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
