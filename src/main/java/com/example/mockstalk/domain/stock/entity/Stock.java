package com.example.mockstalk.domain.stock.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.board.entity.Board;
import com.example.mockstalk.domain.candlesticks.entity.CandleSticks;
import com.example.mockstalk.domain.holdings.entity.Holdings;
import com.example.mockstalk.domain.interest_stocks.entity.InterestStock;
import com.example.mockstalk.domain.news.entity.News;
import com.example.mockstalk.domain.order.entity.Order;
import com.example.mockstalk.domain.price.entity.Price;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "stock")
@NoArgsConstructor
@AllArgsConstructor
public class Stock extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private String stockName;

    @Column(nullable = true)
    private String stockCode;

    @Column(nullable = true)
    private LocalDateTime listedDate;

    @Column(nullable = true)
    private LocalDateTime delistedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockStatus stockStatus;

    @OneToMany(mappedBy = "stock")
    private List<News> newsList = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    private List<Price> prices = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    private List<InterestStock> interestStocks = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    private List<Holdings> holdings = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    private List<Order> orders = new ArrayList<>();




}
