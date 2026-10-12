package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByUserId(Long userId);

    Optional<Customer> findByUserId(Long userId);

    @Query("SELECT c FROM Customer c JOIN FETCH c.user")
    List<Customer> findAllWithUser();
}
