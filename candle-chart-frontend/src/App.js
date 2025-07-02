import React from 'react';
import {Link, Navigate, Route, Routes, useParams} from 'react-router-dom';
import CandleChart from './components/CandleChart';
import NewsList from './components/NewsList';
import PeriodicCandleChart from './components/PeriodicCandleChart';

function App() {
    return (
        <div className="App" style={{padding: "20px", maxWidth: "1000px", margin: "0 auto"}}>
            <nav style={{marginBottom: "30px"}}>
                <Link to="/chart" style={{marginRight: "15px"}}>분봉차트</Link>
                <Link to="/periodic/d" style={{marginRight: "15px"}}>일봉</Link>
                <Link to="/periodic/w" style={{marginRight: "15px"}}>주봉</Link>
                <Link to="/periodic/m" style={{marginRight: "15px"}}>월봉</Link>
                <Link to="/periodic/y" style={{marginRight: "15px"}}>년봉</Link>
                <Link to="/news">뉴스</Link>
            </nav>

            <Routes>
                <Route path="/" element={<Navigate to="/chart" replace/>}/>

                <Route path="/chart" element={
                    <>
                        <h1>[ 한국투자증권 분봉차트 ]</h1>
                        <CandleChart stockCode="000150" interval={1}/>
                    </>
                }/>

                <Route path="/periodic/:type" element={
                    <PeriodicCandleChartWrapper stockCode="000150"/>
                }/>

                <Route path="/news" element={<NewsList/>}/>

                <Route path="*" element={<div>페이지를 찾을 수 없습니다.</div>}/>
            </Routes>
        </div>
    );
}

// 캔들타입에 따라 PeriodicCandleChart 렌더링
function PeriodicCandleChartWrapper({stockCode}) {
    const {type} = useParams();
    return (
        <>
            <h1>[ 한국투자증권 {type.toUpperCase()}봉 차트 ]</h1>
            <PeriodicCandleChart stockCode={stockCode} candleType={type.toUpperCase()}/>
        </>
    );
}

export default App;
