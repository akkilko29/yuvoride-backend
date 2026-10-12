package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {
    boolean existsByUserId(Long userId);

    Optional<AdminAccount> findByUsernameIgnoreCase(String username);

    Optional<AdminAccount> findByUserId(Long userId);

    @Query("SELECT a FROM AdminAccount a JOIN FETCH a.user")
    List<AdminAccount> findAllWithUser();
}
