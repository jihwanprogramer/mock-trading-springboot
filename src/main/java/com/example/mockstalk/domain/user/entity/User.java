package com.example.mockstalk.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.mockstalk.common.baseEntity.BaseEntity;
import com.example.mockstalk.domain.account.entity.Account;
import com.example.mockstalk.domain.board.entity.Board;
import com.example.mockstalk.domain.comment.entity.Comment;
import com.example.mockstalk.domain.interest_stock.entity.InterestStock;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(length = 50, nullable = true)
	private String nickname;

	@Column(unique = true)
	private String walletAddress;

	@Enumerated(EnumType.STRING)
	private UserRole userRole;

	@OneToMany(mappedBy = "user")
	private List<Account> accounts = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Comment> comments = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Board> boards = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InterestStock> interestStocks = new ArrayList<>();

	// 회원가입
	public User(String email, String password, String nickname, String walletAddress, UserRole userRole) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.walletAddress = walletAddress;
		this.userRole = userRole;
	}

	@Builder
	public User(Long id, String email, String password, String nickname, String walletAddress, UserRole userRole) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.walletAddress = walletAddress;
		this.userRole = userRole;
	}

	//수정
	public void updateUser(String nickname, String password) {
		if (nickname != null && !nickname.isEmpty()) {
			this.nickname = nickname;
		}
		if (password != null && !password.isEmpty()) {
			this.password = password;
		}
	}

}
