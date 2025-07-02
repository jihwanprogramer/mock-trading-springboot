import React from 'react';
import {Link, Navigate, Route, Routes, useLocation, useParams} from 'react-router-dom';

import CandleChart from './components/CandleChart';
import NewsList from './components/NewsList';
import PeriodicCandleChart from './components/PeriodicCandleChart';
import RealtimePrice from './components/RealtimePrice';

import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import EditStep1 from './components/EditStep1';
import EditStep2 from './components/EditStep2';
import ProfilePage from './components/ProfilePage';

function App() {
    return (
        <div style={{display: 'flex', flexDirection: 'column', height: '100vh'}}>
            <div style={{flex: 1, overflowY: 'auto'}}>
                <Routes>
                    <Route path="/" element={<Navigate to="/chart" replace/>}/>
                    <Route path="/chart" element={<ChartPage/>}/>
                    <Route path="/periodic/:type" element={<PeriodicCandleChartWrapper stockCode="000150"/>}/>
                    <Route path="/news" element={<NewsList/>}/>
                    <Route path="/realtime" element={<RealtimePrice stockCode="000150"/>}/>

                    {/* 회원 관련 라우트 */}
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route path="/edit/step1" element={<EditStep1/>}/>
                    <Route path="/edit/step2" element={<EditStep2/>}/>
                    <Route path="/profile" element={<ProfilePage/>}/>

                    <Route path="*" element={<div style={{padding: 20}}>페이지를 찾을 수 없습니다.</div>}/>
                </Routes>
            </div>
            <BottomTabNav/>
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
            <h2>[ 한국투자증권 {type.toUpperCase()}봉 차트 ]</h2>
            <PeriodicCandleChart stockCode={stockCode} candleType={type.toUpperCase()}/>
        </div>
    );
}

function BottomTabNav() {
    const location = useLocation();

    // 로그인 여부 체크
    const isLoggedIn = !!localStorage.getItem('token');

    // 로그인 상태에 따라 탭 목록 변경
    const tabs = [
        {to: '/chart', label: '분봉'},
        {to: '/periodic/d', label: '기간별차트'},
        {to: '/news', label: '뉴스'},
        {to: '/realtime', label: '실시간'},
        // 로그인 상태에 따라 마이페이지 또는 로그인 탭 출력
        isLoggedIn
            ? {to: '/profile', label: '마이페이지'}
            : {to: '/login', label: '로그인'},
    ];

    return (
        <nav
            style={{
                display: 'flex',
                justifyContent: 'space-around',
                borderTop: '1px solid #ccc',
                padding: '10px 0',
                backgroundColor: '#fff',
            }}
        >
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


export default App;
