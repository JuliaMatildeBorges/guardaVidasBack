package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Checkout;

@Repository
public interface CheckoutRespository extends JpaRepository<Checkout, Long>{

    List<Checkout> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

}
