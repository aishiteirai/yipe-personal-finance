package com.yipe.finance.repository;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    TransactionRepository repository;

    private Transaction debitTxn;
    private Transaction creditTxn;
    private Transaction incomeTxn;
    private Transaction vRxn;
    private Transaction installmentTxn;

    @BeforeEach
    void setUp() {
        debitTxn = persistTransaction(LocalDate.of(2026, 6, 5), TransactionType.DEBIT,
                BigDecimal.valueOf(100), "Alimentação", "Itaú", "Supermercado", "Única");
        creditTxn = persistTransaction(LocalDate.of(2026, 6, 10), TransactionType.CREDIT,
                BigDecimal.valueOf(200), "Lazer", "Cartão Nubank", "Jantar fora", "Única");
        incomeTxn = persistTransaction(LocalDate.of(2026, 6, 5), TransactionType.INCOME,
                BigDecimal.valueOf(5000), "N/A", "Itaú", "Salário", "Única");
        vRxn = persistTransaction(LocalDate.of(2026, 5, 15), TransactionType.VR,
                BigDecimal.valueOf(50), "Alimentação", "Vale Refeição", "Almoço", "Única");
        installmentTxn = persistTransaction(LocalDate.of(2026, 6, 10), TransactionType.CREDIT,
                BigDecimal.valueOf(300), "Bobeiras", "Cartão Nubank", "Notebook Dell", "1/12");
        persistTransaction(LocalDate.of(2026, 7, 10), TransactionType.CREDIT,
                BigDecimal.valueOf(300), "Bobeiras", "Cartão Nubank", "Notebook Dell", "2/12");
        em.flush();
    }

    @Nested
    @DisplayName("findByDataBetweenOrderByDataDesc")
    class FindByDataBetween {

        @Test
        @DisplayName("should find transactions within date range")
        void shouldFindWithinRange() {
            var result = repository.findByDataBetweenOrderByDataDesc(
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

            assertThat(result).hasSize(4);
            assertThat(result).extracting(Transaction::getDescricao)
                    .contains("Supermercado", "Jantar fora", "Salário", "Notebook Dell");
        }

        @Test
        @DisplayName("should return empty when no transactions in range")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findByDataBetweenOrderByDataDesc(
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return results ordered descending")
        void shouldReturnOrderedDesc() {
            var result = repository.findByDataBetweenOrderByDataDesc(
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31));

            assertThat(result).isSortedAccordingTo((a, b) -> b.getData().compareTo(a.getData()));
        }
    }

    @Nested
    @DisplayName("sumByTipoIn")
    class SumByTipoIn {

        @Test
        @DisplayName("should sum amounts for given types")
        void shouldSumByTypes() {
            var result = repository.sumByTipoIn(List.of(TransactionType.DEBIT, TransactionType.CREDIT));

            assertThat(result).isEqualByComparingTo("900");
        }

        @Test
        @DisplayName("should return zero when no matching types")
        void shouldReturnZero_whenNoMatch() {
            var result = repository.sumByTipoIn(List.of(TransactionType.INVESTMENT, TransactionType.RESERVE));

            assertThat(result).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("sumByTipoInAndMonth")
    class SumByTipoInAndMonth {

        @Test
        @DisplayName("should sum amounts for given types and month")
        void shouldSumByTypesAndMonth() {
            var result = repository.sumByTipoInAndMonth(
                    List.of(TransactionType.DEBIT, TransactionType.CREDIT), 2026, 6);

            assertThat(result).isEqualByComparingTo("600");
        }

        @Test
        @DisplayName("should return zero when no transactions in period")
        void shouldReturnZero_whenNoMatch() {
            var result = repository.sumByTipoInAndMonth(
                    List.of(TransactionType.DEBIT), 2025, 1);

            assertThat(result).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("findByYearAndMonthExcluding")
    class FindByYearAndMonthExcluding {

        @Test
        @DisplayName("should find transactions excluding given types")
        void shouldFindExcludingTypes() {
            var result = repository.findByYearAndMonthExcluding(
                    2026, 6, List.of(TransactionType.INCOME));

            assertThat(result).hasSize(3);
            assertThat(result).extracting(Transaction::getTipo)
                    .doesNotContain(TransactionType.INCOME);
        }

        @Test
        @DisplayName("should return all when exclude list is empty")
        void shouldReturnAll_whenEmptyExclude() {
            var result = repository.findByYearAndMonthExcluding(2026, 6, List.of());

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("should return empty when all types excluded")
        void shouldReturnEmpty_whenAllExcluded() {
            var result = repository.findByYearAndMonthExcluding(
                    2026, 6, List.of(TransactionType.DEBIT, TransactionType.CREDIT,
                            TransactionType.INCOME, TransactionType.VR));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFiltered")
    class FindFiltered {

        @Test
        @DisplayName("should filter by year and month")
        void shouldFilterByYearMonth() {
            var result = repository.findFiltered(2026, 6, null, null, null);

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("should filter by tipo")
        void shouldFilterByTipo() {
            var result = repository.findFiltered(null, null, null, TransactionType.DEBIT, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDescricao()).isEqualTo("Supermercado");
        }

        @Test
        @DisplayName("should filter by category")
        void shouldFilterByCategory() {
            var result = repository.findFiltered(null, null, null, null, "Alimentação");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Transaction::getDescricao)
                    .contains("Supermercado", "Almoço");
        }

        @Test
        @DisplayName("should filter by day")
        void shouldFilterByDay() {
            var result = repository.findFiltered(null, null, 5, null, null);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Transaction::getDescricao)
                    .contains("Supermercado", "Salário");
        }

        @Test
        @DisplayName("should filter by all params")
        void shouldFilterByAll() {
            var result = repository.findFiltered(2026, 6, 5, TransactionType.DEBIT, "Alimentação");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDescricao()).isEqualTo("Supermercado");
        }

        @Test
        @DisplayName("should return all when no filters provided")
        void shouldReturnAll_whenNoFilters() {
            var result = repository.findFiltered(null, null, null, null, null);

            assertThat(result).hasSize(6);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findFiltered(2025, 1, null, null, null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTipoIn")
    class FindByTipoIn {

        @Test
        @DisplayName("should find transactions by types")
        void shouldFindByTypes() {
            var result = repository.findByTipoIn(List.of(TransactionType.DEBIT, TransactionType.VR));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Transaction::getTipo)
                    .containsOnly(TransactionType.DEBIT, TransactionType.VR);
        }

        @Test
        @DisplayName("should return empty when no types match")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findByTipoIn(List.of(TransactionType.RESERVE));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByData")
    class FindByData {

        @Test
        @DisplayName("should find transactions by exact date")
        void shouldFindByDate() {
            var result = repository.findByData(LocalDate.of(2026, 6, 5));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Transaction::getDescricao)
                    .contains("Supermercado", "Salário");
        }

        @Test
        @DisplayName("should return empty when date has no transactions")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findByData(LocalDate.of(2025, 12, 25));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByContaAndTipo")
    class FindByContaAndTipo {

        @Test
        @DisplayName("should find by account and tipo")
        void shouldFindByAccountAndTipo() {
            var result = repository.findByContaAndTipo("Cartão Nubank", TransactionType.CREDIT);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(Transaction::getDescricao)
                    .contains("Jantar fora", "Notebook Dell");
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findByContaAndTipo("Itaú", TransactionType.CREDIT);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByYearAndTypes")
    class FindByYearAndTypes {

        @Test
        @DisplayName("should find by year excluding and including types")
        void shouldFindByYearAndTypes() {
            var result = repository.findByYearAndTypes(2026,
                    List.of(TransactionType.INCOME),
                    List.of(TransactionType.DEBIT, TransactionType.CREDIT, TransactionType.VR));

            assertThat(result).hasSize(5);
            assertThat(result).extracting(Transaction::getTipo)
                    .doesNotContain(TransactionType.INCOME);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findByYearAndTypes(2025,
                    List.of(), List.of(TransactionType.DEBIT));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDistinctYears")
    class FindDistinctYears {

        @Test
        @DisplayName("should return distinct years ordered desc")
        void shouldFindDistinctYears() {
            var result = repository.findDistinctYears();

            assertThat(result).containsExactly(2026);
        }

        @Test
        @DisplayName("should return empty when no transactions")
        void shouldReturnEmpty_whenNoData() {
            repository.deleteAll();
            em.flush();

            var result = repository.findDistinctYears();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByContaAndTipoAndPeriod")
    class FindByContaAndTipoAndPeriod {

        @Test
        @DisplayName("should find by account, tipo, year and month")
        void shouldFindByAccountTipoAndPeriod() {
            var result = repository.findByContaAndTipoAndPeriod(
                    "Cartão Nubank", TransactionType.CREDIT, 2026, 6);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findByContaAndTipoAndPeriod(
                    "Itaú", TransactionType.CREDIT, 2026, 6);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findInstallments")
    class FindInstallments {

        @Test
        @DisplayName("should find installment transactions")
        void shouldFindInstallments() {
            var result = repository.findInstallments();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Transaction::getParcela)
                    .allMatch(p -> p.contains("/"));
        }

        @Test
        @DisplayName("should return empty when no installments")
        void shouldReturnEmpty_whenNoInstallments() {
            repository.deleteAll();
            em.flush();
            persistTransaction(LocalDate.of(2026, 6, 1), TransactionType.DEBIT,
                    BigDecimal.TEN, "Lazer", "Itaú", "Singular", "Única");
            em.flush();

            var result = repository.findInstallments();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findInstallmentGroup")
    class FindInstallmentGroup {

        @Test
        @DisplayName("should find transactions matching description, conta and valor")
        void shouldFindByDescriptionContaAndValor() {
            var result = repository.findInstallmentGroup(
                    "Notebook Dell", "Cartão Nubank", BigDecimal.valueOf(300));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Transaction::getParcela)
                    .contains("1/12", "2/12");
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            var result = repository.findInstallmentGroup(
                    "Notebook Dell", "Itaú", BigDecimal.valueOf(300));

            assertThat(result).isEmpty();
        }
    }

    private Transaction persistTransaction(LocalDate data, TransactionType tipo, BigDecimal valor,
                                            String categoria, String conta, String descricao, String parcela) {
        Transaction t = new Transaction();
        t.setData(data);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setCategoria(categoria);
        t.setConta(conta);
        t.setDescricao(descricao);
        t.setParcela(parcela);
        return em.persistAndFlush(t);
    }
}
