package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

    Optional<Checkin> findFirstByUsuarioAndPostoAndCreatedAtBetweenOrderByCreatedAtDesc(
        Usuario usuario,
        Posto posto,
        LocalDateTime inicio,
        LocalDateTime fim
    );

    List<Checkin> findByUsuarioAndCreatedAtBetween(Usuario usuario, LocalDateTime inicio, LocalDateTime fim);

}
