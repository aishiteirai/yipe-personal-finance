package com.yipe.finance.service;

import com.yipe.finance.entity.Card;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.CardRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    TransactionRepository transactionRepository;
    @Mock
    CardRepository cardRepository;

    @InjectMocks
    InvoiceService service;

    Card card = new Card("Nubank", "Nubank", 25, 5);

    @Test
    @DisplayName("should assign purchase to current month when before closing day")
    void calculateInvoicePeriod_shouldBeSameMonth_whenBeforeClosingDay() {
        String period = service.calculateInvoicePeriod(LocalDate.of(2026, 6, 10), card);
        assertThat(period).isEqualTo("2026-06");
    }

    @Test
    @DisplayName("should assign purchase to next month when on closing day")
    void calculateInvoicePeriod_shouldBeNextMonth_whenOnClosingDay() {
        String period = service.calculateInvoicePeriod(LocalDate.of(2026, 6, 25), card);
        assertThat(period).isEqualTo("2026-07");
    }

    @Test
    @DisplayName("should assign purchase to next month when after closing day")
    void calculateInvoicePeriod_shouldBeNextMonth_whenAfterClosingDay() {
        String period = service.calculateInvoicePeriod(LocalDate.of(2026, 6, 28), card);
        assertThat(period).isEqualTo("2026-07");
    }

    @Test
    @DisplayName("should roll over to January when purchase is in December after closing")
    void calculateInvoicePeriod_shouldRollYear_whenDecember() {
        String period = service.calculateInvoicePeriod(LocalDate.of(2026, 12, 28), card);
        assertThat(period).isEqualTo("2027-01");
    }

    @Test
    @DisplayName("should return cards with credit transactions")
    void getCardsWithTransactions_shouldFilterByCreditUsage() {
        when(cardRepository.findAll()).thenReturn(List.of(
                new Card("Nubank", "Nubank", 25, 5),
                new Card("Itaú", "Itaú", 15, 10)
        ));
        Transaction t = new Transaction();
        t.setTipo(TransactionType.CREDIT);
        t.setConta("Nubank");
        when(transactionRepository.findByTipoIn(List.of(TransactionType.CREDIT)))
                .thenReturn(List.of(t));

        List<Card> result = service.getCardsWithTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Nubank");
    }

    @Test
    @DisplayName("should calculate invoice total from filtered transactions")
    void getInvoice_shouldSumTransactionValues() {
        when(cardRepository.findById("Nubank")).thenReturn(java.util.Optional.of(card));

        Transaction jan10 = new Transaction();
        jan10.setData(LocalDate.of(2026, 6, 10));
        jan10.setValor(BigDecimal.valueOf(100));
        jan10.setTipo(TransactionType.CREDIT);
        jan10.setConta("Nubank");

        Transaction jun28 = new Transaction();
        jun28.setData(LocalDate.of(2026, 6, 28));
        jun28.setValor(BigDecimal.valueOf(200));
        jun28.setTipo(TransactionType.CREDIT);
        jun28.setConta("Nubank");

        when(transactionRepository.findByContaAndTipo("Nubank", TransactionType.CREDIT))
                .thenReturn(List.of(jan10, jun28));

        InvoiceService.InvoiceData invoice = service.getInvoice("Nubank", "2026-06");

        assertThat(invoice).isNotNull();
        assertThat(invoice.getTotal()).isEqualByComparingTo("100");
        assertThat(invoice.getTransactions()).hasSize(1);
        assertThat(invoice.getMesAno()).isEqualTo("2026-06");
    }
}
