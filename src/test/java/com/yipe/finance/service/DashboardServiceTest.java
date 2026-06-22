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
}
