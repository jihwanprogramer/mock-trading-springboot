package com.example.mockstalk.domain.user.repository;

import com.example.mockstalk.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existByEmail(String email);

    Optional<User> findByEmail(String email);
}
