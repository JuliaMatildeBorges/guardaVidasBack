package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pessoa")
@EqualsAndHashCode(callSuper = false)
public class Pessoa extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String nome;


}
