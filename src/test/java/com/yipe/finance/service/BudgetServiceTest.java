package com.yipe.finance.service;

import com.yipe.finance.entity.Category;
import com.yipe.finance.entity.Salary;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.repository.SalaryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    TransactionRepository transactionRepository;
    @Mock
    SalaryRepository salaryRepository;
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    BudgetService service;

    @Test
    @DisplayName("should calculate budget with 50/30/20 rule")
    void calculate_shouldApplyPercentages() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                new Category("Alimentação"), new Category("Lazer"), new Category("Investimento")));
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INCOME), 2026, 6)).thenReturn(BigDecimal.valueOf(5000));
        when(salaryRepository.findAll()).thenReturn(List.of());
        when(transactionRepository.findByYearAndMonthExcluding(anyInt(), anyInt(), any()))
                .thenReturn(List.of());
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE), 2026, 6))
                .thenReturn(BigDecimal.valueOf(1000));

        var result = service.calculate(2026, 6, null, 50, 30, 20,
                List.of("Alimentação"), List.of("Lazer"));

        assertThat(result.getBaseIncome()).isEqualByComparingTo("5000");
        assertThat(result.getCeilingNeeds()).isEqualByComparingTo("2500");
        assertThat(result.getCeilingWants()).isEqualByComparingTo("1500");
        assertThat(result.getCeilingInvest()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("should use expected income when real income is zero")
    void calculate_shouldFallbackToExpectedIncome() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                new Category("Alimentação")));
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INCOME), 2026, 6)).thenReturn(BigDecimal.ZERO);
        Salary sal = new Salary();
        sal.setValor(BigDecimal.valueOf(6000));
        when(salaryRepository.findAll()).thenReturn(List.of(sal));
        when(transactionRepository.findByYearAndMonthExcluding(anyInt(), anyInt(), any()))
                .thenReturn(List.of());
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE), 2026, 6))
                .thenReturn(BigDecimal.ZERO);

        var result = service.calculate(2026, 6, null, 50, 30, 20,
                List.of("Alimentação"), List.of());

        assertThat(result.getBaseIncome()).isEqualByComparingTo("6000");
    }

    @Test
    @DisplayName("should override income when baseIncome is provided")
    void calculate_shouldUseProvidedBaseIncome() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                new Category("Alimentação")));
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INCOME), 2026, 6)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByYearAndMonthExcluding(anyInt(), anyInt(), any()))
                .thenReturn(List.of());
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE), 2026, 6))
                .thenReturn(BigDecimal.ZERO);

        var result = service.calculate(2026, 6, BigDecimal.valueOf(10000), 50, 30, 20,
                List.of("Alimentação"), List.of());

        assertThat(result.getBaseIncome()).isEqualByComparingTo("10000");
        assertThat(result.getCeilingNeeds()).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("should calculate spent amounts from categories")
    void calculate_shouldSumSpendingByCategory() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                new Category("Alimentação"), new Category("Lazer")));
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INCOME), 2026, 6)).thenReturn(BigDecimal.ZERO);

        Transaction t1 = new Transaction();
        t1.setTipo(TransactionType.DEBIT);
        t1.setCategoria("Alimentação");
        t1.setValor(BigDecimal.valueOf(300));
        Transaction t2 = new Transaction();
        t2.setTipo(TransactionType.DEBIT);
        t2.setCategoria("Lazer");
        t2.setValor(BigDecimal.valueOf(100));

        when(transactionRepository.findByYearAndMonthExcluding(anyInt(), anyInt(), any()))
                .thenReturn(List.of(t1, t2));
        when(transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE), 2026, 6))
                .thenReturn(BigDecimal.valueOf(500));

        var result = service.calculate(2026, 6, BigDecimal.valueOf(5000), 50, 30, 20,
                List.of("Alimentação"), List.of("Lazer"));

        assertThat(result.getSpentNeeds()).isEqualByComparingTo("300");
        assertThat(result.getSpentWants()).isEqualByComparingTo("100");
        assertThat(result.getSpentInvest()).isEqualByComparingTo("500");
    }
}
