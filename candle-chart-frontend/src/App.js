import React from 'react';
import {Link, Route, Routes, useLocation, useNavigate, useParams} from 'react-router-dom';

import CandleChart from './components/CandleChart';
import NewsList from './components/NewsList';
import PeriodicCandleChart from './components/PeriodicCandleChart';
import RealtimePrice from './components/RealtimePrice';

import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import ProfilePage from './components/ProfilePage';

import BoardListPage from './components/BoardListPage';
import BoardWritePage from './components/BoardWritePage';

import AccountCreate from './components/AccountCreate';
import AccountSelect from './components/AccountSelect';
import PasswordInput from './components/PasswordInput';
import Home from './components/Home';
import AccountInfo from './components/AccountInfo';
import StockDetail from './components/StockDetail';
import BuyConfirm from './components/BuyConfirm';
import SellConfirm from './components/SellConfirm';
import TradeHistory from './components/TradeHistory';
import OrderHistory from './components/OrderHistory';

function HomePage() {
    const navigate = useNavigate();

    return (
        <div style={{
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            fontFamily: 'sans-serif',
            backgroundColor: '#fafafa',
        }}>
            <img src="/logo.png" alt="앱 로고" style={{width: 150, marginBottom: 30}}/>
            <h1 style={{marginBottom: 40}}>MockStock</h1>
            <button
                onClick={() => navigate('/login')}
                style={{
                    padding: '15px 40px',
                    fontSize: 18,
                    cursor: 'pointer',
                    borderRadius: 8,
                    border: 'none',
                    backgroundColor: '#4461F2',
                    color: '#fff',
                    fontWeight: 'bold',
                }}
            >
                시작하기
            </button>
        </div>
    );
}

function ChartPage() {
    return (
        <div style={{padding: 20}}>
            <h2 style={{marginBottom: 10}}>[ 한국투자증권 분봉차트 ]</h2>
            <CandleChart stockCode="000150" interval={1}/>
        </div>
    );
}

function PeriodicCandleChartWrapper({stockCode}) {
    const {type} = useParams();
    return (
        <div style={{padding: 20}}>
            <PeriodicCandleChart stockCode={stockCode} candleType={type.toUpperCase()}/>
        </div>
    );
}

function ConditionalBottomTabNav() {
    const location = useLocation();
    if (location.pathname === '/') return null;
    return <BottomTabNav/>;
}

function BottomTabNav() {
    const location = useLocation();
    const isLoggedIn = !!localStorage.getItem('token');

    const tabs = [
        {to: '/chart', label: '분봉'},
        {to: '/periodic/d', label: '기간별차트'},
        {to: '/news', label: '뉴스'},
        {to: '/realtime', label: '실시간'},
        {to: '/board', label: '게시판'},


        {to: '/account/create', label: '계좌'},
        {to: '/mockstalk/home', label: '종목'},
        {to: '/trade/history', label: '체결'},

        isLoggedIn
            ? {to: '/profile', label: '마이페이지'}
            : {to: '/login', label: '로그인'},
    ];

    return (
        <nav style={{
            display: 'flex',
            justifyContent: 'space-around',
            borderTop: '1px solid #ccc',
            padding: '10px 0',
            backgroundColor: '#fff',
        }}>
            {tabs.map(tab => (
                <Link
                    key={tab.to}
                    to={tab.to}
                    style={{
                        color: location.pathname === tab.to ? '#f97316' : '#555',
                        textDecoration: 'none',
                        fontWeight: location.pathname === tab.to ? 'bold' : 'normal',
                        fontSize: 14,
                    }}
                >
                    {tab.label}
                </Link>
            ))}
        </nav>
    );
}

function App() {
    const stockId = 1;

    return (
        <div style={{display: 'flex', flexDirection: 'column', height: '100vh'}}>
            <div style={{flex: 1, overflowY: 'auto'}}>
                <Routes>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/chart" element={<ChartPage/>}/>
                    <Route path="/periodic/:type" element={<PeriodicCandleChartWrapper stockCode="000150"/>}/>
                    <Route path="/realtime" element={<RealtimePrice stockCode="000150"/>}/>
                    <Route path="/news" element={<NewsList/>}/>

                    {/* 게시판 */}
                    <Route path="/board" element={<BoardListPage stockId={stockId}/>}/>
                    <Route path="/board/write" element={<BoardWritePage stockId={stockId}/>}/>

                    {/* 로그인/회원가입/마이페이지 */}
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route path="/profile" element={<ProfilePage/>}/>

                    {/* ✅ Mockstalk 페이지 */}
                    <Route path="/account/create" element={<AccountCreate/>}/>
                    <Route path="/account/select" element={<AccountSelect/>}/>
                    <Route path="/account/password" element={<PasswordInput/>}/>
                    <Route path="/mockstalk/home" element={<Home/>}/>
                    <Route path="/account/info" element={<AccountInfo/>}/>
                    <Route path="/stock/:stockCode" element={<StockDetail/>}/>
                    <Route path="/stock/buy" element={<BuyConfirm/>}/>
                    <Route path="/stock/sell" element={<SellConfirm/>}/>
                    <Route path="/trade/history" element={<TradeHistory/>}/>
                    <Route path="/order/history" element={<OrderHistory/>}/>

                    <Route path="*" element={<div style={{padding: 20}}>페이지를 찾을 수 없습니다.</div>}/>
                </Routes>
            </div>
            <ConditionalBottomTabNav/>
        </div>
    );
}

export default App;
