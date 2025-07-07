import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import OrderPage from './pages/OrderPage';
import Orders from './pages/Orders';
import TradeHistory from './pages/TradeHistory';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/order" element={<OrderPage />} />
                <Route path="/orders" element={<Orders />} />
                <Route path="/trade-history" element={<TradeHistory />} />
            </Routes>
        </Router>
    );
}

export default App;
