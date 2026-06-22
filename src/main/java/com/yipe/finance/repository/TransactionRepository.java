package com.yipe.finance.repository;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDataBetweenOrderByDataDesc(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transaction t WHERE t.tipo IN :tipos")
    BigDecimal sumByTipoIn(@Param("tipos") List<TransactionType> tipos);

    @Query("SELECT t FROM Transaction t WHERE "
         + "(:year IS NULL OR YEAR(t.data) = :year) AND "
         + "(:month IS NULL OR MONTH(t.data) = :month) AND "
         + "(:day IS NULL OR DAY(t.data) = :day) AND "
         + "(:tipo IS NULL OR t.tipo = :tipo) AND "
         + "(:categoria IS NULL OR t.categoria = :categoria) "
         + "ORDER BY t.data DESC")
    List<Transaction> findFiltered(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("day") Integer day,
            @Param("tipo") TransactionType tipo,
            @Param("categoria") String categoria);

    @Query("SELECT t FROM Transaction t WHERE t.conta = :conta AND t.tipo = :tipo ORDER BY t.data")
    List<Transaction> findByContaAndTipo(@Param("conta") String conta, @Param("tipo") TransactionType tipo);
}
