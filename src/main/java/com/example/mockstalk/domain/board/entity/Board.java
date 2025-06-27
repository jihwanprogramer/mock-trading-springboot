package com.example.mockstalk.domain.board.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.board.dto.BoardUpdateRequestDto;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(name = "board")
@NoArgsConstructor
@AllArgsConstructor
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    public Board(String title, String content, Stock stock) {
        this.title = title;
        this.content = content;
        this.stock = stock;
    }

    public void updatedAt(BoardUpdateRequestDto boardUpdateRequestDto) {
        if (boardUpdateRequestDto.getTitle() != null) {
            this.title = boardUpdateRequestDto.getTitle();
        }
        if (boardUpdateRequestDto.getContent() != null) {
            this.content = boardUpdateRequestDto.getContent();
        }
    }
}
