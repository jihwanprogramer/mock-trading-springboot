package com.example.mockstalk.domain.interest_stocks.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interststock")
@NoArgsConstructor
@AllArgsConstructor
public class InterestStock extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
