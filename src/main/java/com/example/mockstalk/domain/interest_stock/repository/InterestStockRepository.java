package com.example.mockstalk.domain.interest_stock.repository;

import com.example.mockstalk.domain.interest_stock.entity.InterestStock;
import com.example.mockstalk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestStockRepository extends JpaRepository<InterestStock,Long> {
    List<InterestStock> findAllByUser(User user);
}
