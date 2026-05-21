package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "checkout")
@EqualsAndHashCode(callSuper = false)
public class Checkout extends BaseEntity{

    @ManyToOne
    private Posto posto;

    @ManyToOne
    private Arquivo foto;


}