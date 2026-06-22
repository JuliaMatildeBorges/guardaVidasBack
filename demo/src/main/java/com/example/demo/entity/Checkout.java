package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
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

    @ManyToOne(optional = true)
    @JoinColumn(name = "foto_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Arquivo foto;

    @ManyToMany
    private List<Arquivo> fotos = new ArrayList<>();

    @ManyToOne
    private Usuario usuario;

    private Integer prevencoesManha;

    private Integer prevencoesTarde;

    private Integer lesoesAguaVivaManha;

    private Integer lesoesAguaVivaTarde;

}
