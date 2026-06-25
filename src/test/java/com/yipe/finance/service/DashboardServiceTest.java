package com.yipe.finance.service;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    DashboardService service;

    @Test
    @DisplayName("should sum by types")
    void sumByTypes_shouldReturnTotal() {
        when(repository.sumByTipoIn(List.of(TransactionType.INCOME)))
                .thenReturn(BigDecimal.valueOf(5000));

        BigDecimal result = service.sumByTypes(List.of(TransactionType.INCOME));

        assertThat(result).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("should sum by types and month")
    void sumByTypesAndMonth_shouldReturnTotal() {
        when(repository.sumByTipoInAndMonth(List.of(TransactionType.DEBIT), 2026, 6))
                .thenReturn(BigDecimal.valueOf(1500));

        BigDecimal result = service.sumByTypesAndMonth(List.of(TransactionType.DEBIT), 2026, 6);

        assertThat(result).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("should group daily expenses")
    void getDailyExpenses_shouldGroupByDay() {
        Transaction t1 = makeTxn(TransactionType.DEBIT, BigDecimal.valueOf(100), LocalDate.of(2026, 6, 5));
        Transaction t2 = makeTxn(TransactionType.DEBIT, BigDecimal.valueOf(50), LocalDate.of(2026, 6, 5));
        Transaction t3 = makeTxn(TransactionType.CREDIT, BigDecimal.valueOf(200), LocalDate.of(2026, 6, 7));
        Transaction t4 = makeTxn(TransactionType.INCOME, BigDecimal.valueOf(5000), LocalDate.of(2026, 6, 5));

        Map<Integer, BigDecimal> daily = service.getDailyExpenses(List.of(t1, t2, t3, t4));

        assertThat(daily).containsOnlyKeys(5, 7);
        assertThat(daily.get(5)).isEqualByComparingTo("150");
        assertThat(daily.get(7)).isEqualByComparingTo("200");
    }

    @Test
    @DisplayName("should group expenses by category")
    void getExpensesByCategory_shouldGroup() {
        Transaction t1 = makeTxn(TransactionType.DEBIT, BigDecimal.valueOf(100), "Alimentação");
        Transaction t2 = makeTxn(TransactionType.CREDIT, BigDecimal.valueOf(50), "Alimentação");
        Transaction t3 = makeTxn(TransactionType.VR, BigDecimal.valueOf(200), "Transporte");
        Transaction t4 = makeTxn(TransactionType.INCOME, BigDecimal.valueOf(5000), "Salário");

        Map<String, BigDecimal> byCat = service.getExpensesByCategory(List.of(t1, t2, t3, t4));

        assertThat(byCat).containsOnlyKeys("Alimentação", "Transporte");
        assertThat(byCat.get("Alimentação")).isEqualByComparingTo("150");
        assertThat(byCat.get("Transporte")).isEqualByComparingTo("200");
    }

    private Transaction makeTxn(TransactionType tipo, BigDecimal valor, LocalDate data) {
        Transaction t = new Transaction();
        t.setTipo(tipo);
        t.setValor(valor);
        t.setData(data);
        return t;
    }

    private Transaction makeTxn(TransactionType tipo, BigDecimal valor, String categoria) {
        Transaction t = new Transaction();
        t.setTipo(tipo);
        t.setValor(valor);
        t.setCategoria(categoria);
        return t;
    }

    private Transaction makeSankeyTxn(String conta, String categoria, TransactionType tipo, BigDecimal valor) {
        Transaction t = new Transaction();
        t.setConta(conta);
        t.setCategoria(categoria);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setData(LocalDate.now());
        return t;
    }

    // ---- Untested methods below ----

    @Test
    @DisplayName("should compute saldo geral as entradas - saidas - investimentos")
    void computeSaldoGeral_shouldReturnBalance() {
        when(repository.sumByTipoIn(List.of(TransactionType.INCOME, TransactionType.ADJUSTMENT_INCOME)))
                .thenReturn(BigDecimal.valueOf(10000));
        when(repository.sumByTipoIn(List.of(TransactionType.DEBIT, TransactionType.VR, TransactionType.ADJUSTMENT_EXPENSE)))
                .thenReturn(BigDecimal.valueOf(4000));
        when(repository.sumByTipoIn(List.of(TransactionType.INVESTMENT, TransactionType.RESERVE)))
                .thenReturn(BigDecimal.valueOf(1000));

        BigDecimal result = service.computeSaldoGeral();

        assertThat(result).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("should return zero for computeSaldoGeral when no transactions")
    void computeSaldoGeral_shouldReturnZero_whenNoTransactions() {
        when(repository.sumByTipoIn(List.of(TransactionType.INCOME, TransactionType.ADJUSTMENT_INCOME)))
                .thenReturn(BigDecimal.ZERO);
        when(repository.sumByTipoIn(List.of(TransactionType.DEBIT, TransactionType.VR, TransactionType.ADJUSTMENT_EXPENSE)))
                .thenReturn(BigDecimal.ZERO);
        when(repository.sumByTipoIn(List.of(TransactionType.INVESTMENT, TransactionType.RESERVE)))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal result = service.computeSaldoGeral();

        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("should compute gasto credito hoje summing CREDIT transactions")
    void computeGastoCreditoHoje_shouldSumCredit() {
        Transaction t1 = makeTxn(TransactionType.CREDIT, BigDecimal.valueOf(200), LocalDate.now());
        Transaction t2 = makeTxn(TransactionType.DEBIT, BigDecimal.valueOf(100), LocalDate.now());
        Transaction t3 = makeTxn(TransactionType.CREDIT, BigDecimal.valueOf(50), LocalDate.now());

        BigDecimal result = service.computeGastoCreditoHoje(List.of(t1, t2, t3));

        assertThat(result).isEqualByComparingTo("250");
    }

    @Test
    @DisplayName("should return zero for computeGastoCreditoHoje with empty list")
    void computeGastoCreditoHoje_shouldReturnZero_whenEmpty() {
        BigDecimal result = service.computeGastoCreditoHoje(List.of());
        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("should compute gasto debito hoje summing DEBIT and VR transactions")
    void computeGastoDebitoHoje_shouldSumDebitAndVr() {
        Transaction t1 = makeTxn(TransactionType.DEBIT, BigDecimal.valueOf(100), LocalDate.now());
        Transaction t2 = makeTxn(TransactionType.VR, BigDecimal.valueOf(50), LocalDate.now());
        Transaction t3 = makeTxn(TransactionType.CREDIT, BigDecimal.valueOf(200), LocalDate.now());

        BigDecimal result = service.computeGastoDebitoHoje(List.of(t1, t2, t3));

        assertThat(result).isEqualByComparingTo("150");
    }

    @Test
    @DisplayName("should return zero for computeGastoDebitoHoje with empty list")
    void computeGastoDebitoHoje_shouldReturnZero_whenEmpty() {
        BigDecimal result = service.computeGastoDebitoHoje(List.of());
        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("should build sankey flow data with source/target/value/indices")
    void getSankeyData_shouldBuildFlow() {
        Transaction t1 = makeSankeyTxn("Nubank", "Alimentação", TransactionType.DEBIT, BigDecimal.valueOf(100));
        Transaction t2 = makeSankeyTxn("Nubank", "Alimentação", TransactionType.DEBIT, BigDecimal.valueOf(50));
        Transaction t3 = makeSankeyTxn("Itaú", "Transporte", TransactionType.CREDIT, BigDecimal.valueOf(200));
        Transaction t4 = makeSankeyTxn("Nubank", "Lazer", TransactionType.VR, BigDecimal.valueOf(75));

        List<Map<String, Object>> sankey = service.getSankeyData(List.of(t1, t2, t3, t4));

        assertThat(sankey).hasSize(3);
        Map<String, Object> nubankAlimentacao = sankey.stream()
                .filter(m -> "Nubank".equals(m.get("source")) && "Alimentação".equals(m.get("target")))
                .findFirst().orElseThrow();
        assertThat(((BigDecimal) nubankAlimentacao.get("value"))).isEqualByComparingTo("150");
        assertThat(nubankAlimentacao.get("source")).isEqualTo("Nubank");
        assertThat(nubankAlimentacao.get("target")).isEqualTo("Alimentação");

        Map<String, Object> itauTransporte = sankey.stream()
                .filter(m -> "Itaú".equals(m.get("source")) && "Transporte".equals(m.get("target")))
                .findFirst().orElseThrow();
        assertThat(((BigDecimal) itauTransporte.get("value"))).isEqualByComparingTo("200");

        Map<String, Object> nubankLazer = sankey.stream()
                .filter(m -> "Nubank".equals(m.get("source")) && "Lazer".equals(m.get("target")))
                .findFirst().orElseThrow();
        assertThat(((BigDecimal) nubankLazer.get("value"))).isEqualByComparingTo("75");

        for (Map<String, Object> link : sankey) {
            assertThat(link.get("sourceIdx")).isInstanceOf(Integer.class);
            assertThat(link.get("targetIdx")).isInstanceOf(Integer.class);
        }
    }

    @Test
    @DisplayName("should return empty sankey data for empty transactions")
    void getSankeyData_shouldReturnEmpty_whenEmpty() {
        List<Map<String, Object>> sankey = service.getSankeyData(List.of());
        assertThat(sankey).isEmpty();
    }

    @Test
    @DisplayName("should filter out non-expense types from sankey data")
    void getSankeyData_shouldFilterNonExpenses() {
        Transaction t1 = makeSankeyTxn("Nubank", "Alimentação", TransactionType.INCOME, BigDecimal.valueOf(5000));
        Transaction t2 = makeSankeyTxn("Nubank", "Alimentação", TransactionType.DEBIT, BigDecimal.valueOf(100));

        List<Map<String, Object>> sankey = service.getSankeyData(List.of(t1, t2));

        assertThat(sankey).hasSize(1);
        assertThat(sankey.get(0).get("source")).isEqualTo("Nubank");
        assertThat(sankey.get(0).get("target")).isEqualTo("Alimentação");
    }

    @Test
    @DisplayName("should get yearly expenses from repository")
    void getYearlyExpenses_shouldCallRepository() {
        Transaction t1 = makeTxn(TransactionType.DEBIT, BigDecimal.valueOf(100), LocalDate.of(2026, 6, 1));
        when(repository.findByYearAndTypes(2026,
                List.of(TransactionType.ADJUSTMENT_INCOME, TransactionType.ADJUSTMENT_EXPENSE,
                        TransactionType.INCOME, TransactionType.INVESTMENT, TransactionType.RESERVE),
                List.of(TransactionType.DEBIT, TransactionType.CREDIT, TransactionType.VR)))
                .thenReturn(List.of(t1));

        List<Transaction> result = service.getYearlyExpenses(2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValor()).isEqualByComparingTo("100");
    }
}
