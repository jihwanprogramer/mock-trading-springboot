package com.example.mockstalk.domain.user.entity;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.account.entity.Accounts;
import com.example.mockstalk.domain.board.entity.Board;
import com.example.mockstalk.domain.comment.entity.Comment;
import com.example.mockstalk.domain.interest_stock.entity.InterestStock;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100,nullable = false, unique = true)
    private String email;

    @Column(length = 50, nullable = true)
    private String password;

    @Column(length = 50, nullable = true)
    private String nickname;

    @Column(unique = true)
    private String walletAddress;


    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @OneToMany(mappedBy = "user")
    private List<Accounts> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<InterestStock> interestStocks = new ArrayList<>();


    public User( String email, String password, String nickname, String walletAddress,UserRole userRole) {
       this.email = email;
       this.password = password;
       this.nickname = nickname;
       this.walletAddress = walletAddress;
       this.userRole = userRole;
    }
}
