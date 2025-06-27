import React from 'react';
import CandleChart from './components/CandleChart';

function App() {
    return (
        <div className="App">
            <h1>[ 한국투자증권 분봉차트 ]</h1>
            <CandleChart stockCode="000150" interval={1}/>
        </div>
    );
}

export default App;