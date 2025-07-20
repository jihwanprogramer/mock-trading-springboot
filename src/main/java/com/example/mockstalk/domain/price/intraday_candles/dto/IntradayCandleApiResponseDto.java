package com.example.mockstalk.domain.price.intraday_candles.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.mockstalk.domain.price.intraday_candles.entity.CandleType;
import com.example.mockstalk.domain.price.intraday_candles.entity.IntradayCandle;
import com.example.mockstalk.domain.stock.entity.Stock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntradayCandleApiResponseDto {
	private String stck_bsop_date;   // 기준일자 (yyyyMMdd)
	private String stck_cntg_hour;   // 체결시각 (HHmmss)
	private String stck_prpr;        // 현재가(종가)
	private String stck_oprc;        // 시가
	private String stck_hgpr;        // 고가
	private String stck_lwpr;        // 저가
	private String cntg_vol;         // 체결거래량
	private String acml_tr_pbmn;     // 누적거래대금

	public IntradayCandle toEntity(Stock stock) {
		return IntradayCandle.builder()
			.candleType(CandleType.MIN)
			.stockCode(stock.getStockCode())
			.stockName(stock.getStockName())
			.timeStamp(
				LocalDateTime.parse(stck_bsop_date + stck_cntg_hour, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
			.closingPrice(Long.parseLong(stck_prpr))
			.openingPrice(Long.parseLong(stck_oprc))
			.highPrice(Long.parseLong(stck_hgpr))
			.lowPrice(Long.parseLong(stck_lwpr))
			.tradingVolume(Long.parseLong(cntg_vol))
			.tradingValue(Long.parseLong(acml_tr_pbmn))
			.stock(stock)
			.build();
	}

}
