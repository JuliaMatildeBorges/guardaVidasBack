package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Checkin;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

}
