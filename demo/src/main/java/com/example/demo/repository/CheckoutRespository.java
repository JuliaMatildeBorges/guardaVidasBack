package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;

@Repository
public interface CheckoutRespository extends JpaRepository<Checkout, Long>{

    List<Checkout> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

    Optional<Checkout> findFirstByUsuarioAndPostoAndCreatedAtBetweenOrderByCreatedAtDesc(
        Usuario usuario,
        Posto posto,
        LocalDateTime inicio,
        LocalDateTime fim
    );

    List<Checkout> findByUsuarioAndCreatedAtBetween(Usuario usuario, LocalDateTime inicio, LocalDateTime fim);

}
