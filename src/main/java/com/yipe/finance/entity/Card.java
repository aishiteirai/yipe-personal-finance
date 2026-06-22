package com.yipe.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cartoes")
public class Card {

    @Id
    @Column(name = "nome", length = 100)
    private String nome;

    @Column(name = "banco", length = 100, nullable = false)
    private String banco;

    @Column(name = "dia_fechamento", nullable = false)
    private Integer diaFechamento;

    @Column(name = "dia_vencimento", nullable = false)
    private Integer diaVencimento;

    public Card() {}

    public Card(String nome, String banco, Integer diaFechamento, Integer diaVencimento) {
        this.nome = nome;
        this.banco = banco;
        this.diaFechamento = diaFechamento;
        this.diaVencimento = diaVencimento;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public Integer getDiaFechamento() { return diaFechamento; }
    public void setDiaFechamento(Integer diaFechamento) { this.diaFechamento = diaFechamento; }

    public Integer getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(Integer diaVencimento) { this.diaVencimento = diaVencimento; }
}
