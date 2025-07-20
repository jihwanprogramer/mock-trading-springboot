package com.example.mockstalk.domain.interest_stock.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interestStock")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestStock extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    // 관심종목 등록
    public InterestStock(User user, Stock stock) {
        this.user = user;
        this.stock = stock;
    }


}
