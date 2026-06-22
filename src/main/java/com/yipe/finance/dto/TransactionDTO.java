package com.yipe.finance.dto;

import com.yipe.finance.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDTO {

    @NotNull
    private TransactionType tipo;

    @NotNull
    private LocalDate data;

    @NotNull
    @Positive
    private BigDecimal valor;

    private String categoria;

    @NotBlank
    private String conta;

    @NotBlank
    private String descricao;

    private boolean recorrente;

    private String tipoRecorrencia;

    private Integer qtdMeses = 1;

    public TransactionType getTipo() { return tipo; }
    public void setTipo(TransactionType tipo) { this.tipo = tipo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getConta() { return conta; }
    public void setConta(String conta) { this.conta = conta; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isRecorrente() { return recorrente; }
    public void setRecorrente(boolean recorrente) { this.recorrente = recorrente; }

    public String getTipoRecorrencia() { return tipoRecorrencia; }
    public void setTipoRecorrencia(String tipoRecorrencia) { this.tipoRecorrencia = tipoRecorrencia; }

    public Integer getQtdMeses() { return qtdMeses; }
    public void setQtdMeses(Integer qtdMeses) { this.qtdMeses = qtdMeses; }
}
