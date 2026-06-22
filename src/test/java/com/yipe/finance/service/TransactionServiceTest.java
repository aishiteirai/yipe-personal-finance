package com.yipe.finance.service;

import com.yipe.finance.dto.TransactionDTO;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    TransactionService service;

    @Captor
    ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    ArgumentCaptor<List<Transaction>> listCaptor;

    @Test
    @DisplayName("should save single transaction when not recurring")
    void create_shouldSaveSingle_whenNotRecurring() {
        TransactionDTO dto = new TransactionDTO();
        dto.setData(LocalDate.of(2026, 6, 15));
        dto.setDescricao("Teste");
        dto.setValor(BigDecimal.valueOf(100));
        dto.setTipo(TransactionType.DEBIT);
        dto.setCategoria("Alimentação");
        dto.setConta("Nubank");
        dto.setRecorrente(false);
        dto.setQtdMeses(1);

        service.create(dto);

        verify(repository).save(transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getDescricao()).isEqualTo("Teste");
        assertThat(saved.getValor()).isEqualByComparingTo("100.00");
        assertThat(saved.getParcela()).isEqualTo("Única");
        assertThat(saved.getTipo()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    @DisplayName("should create installment transactions when recurring with months")
    void create_shouldCreateInstallments_whenRecurring() {
        TransactionDTO dto = new TransactionDTO();
        dto.setData(LocalDate.of(2026, 1, 10));
        dto.setDescricao("Parcelado");
        dto.setValor(BigDecimal.valueOf(3000));
        dto.setTipo(TransactionType.CREDIT);
        dto.setCategoria("Eletrônicos");
        dto.setConta("Nubank");
        dto.setRecorrente(true);
        dto.setQtdMeses(3);
        dto.setTipoRecorrencia("parcelado");

        service.create(dto);

        verify(repository).saveAll(listCaptor.capture());
        List<Transaction> transactions = listCaptor.getValue();
        assertThat(transactions).hasSize(3);
        assertThat(transactions.get(0).getValor()).isEqualByComparingTo("1000.00");
        assertThat(transactions.get(0).getParcela()).isEqualTo("1/3");
        assertThat(transactions.get(1).getParcela()).isEqualTo("2/3");
        assertThat(transactions.get(2).getParcela()).isEqualTo("3/3");
        assertThat(transactions.get(1).getData()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(transactions.get(2).getData()).isEqualTo(LocalDate.of(2026, 3, 10));
    }

    @Test
    @DisplayName("should create recurring (non-installment) transactions")
    void create_shouldCreateRecurring_whenMes() {
        TransactionDTO dto = new TransactionDTO();
        dto.setData(LocalDate.of(2026, 1, 5));
        dto.setDescricao("Mensal");
        dto.setValor(BigDecimal.valueOf(100));
        dto.setTipo(TransactionType.DEBIT);
        dto.setCategoria("Assinatura");
        dto.setConta("Nubank");
        dto.setRecorrente(true);
        dto.setQtdMeses(12);
        dto.setTipoRecorrencia("mensal");

        service.create(dto);

        verify(repository).saveAll(listCaptor.capture());
        List<Transaction> transactions = listCaptor.getValue();
        assertThat(transactions).hasSize(12);
        assertThat(transactions.get(0).getParcela()).isEqualTo("Recorrente");
        assertThat(transactions.get(0).getValor()).isEqualByComparingTo("100.00");
        assertThat(transactions.get(11).getData()).isEqualTo(LocalDate.of(2026, 12, 5));
    }

    @Test
    @DisplayName("should update transaction fields")
    void update_shouldModifyFields() {
        Transaction existing = new Transaction();
        existing.setId(1L);
        existing.setDescricao("Original");
        existing.setValor(BigDecimal.valueOf(50));
        existing.setTipo(TransactionType.DEBIT);
        existing.setConta("Nubank");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.update(1L, TransactionType.CREDIT, LocalDate.of(2026, 7, 1),
                BigDecimal.valueOf(200), "Atualizado", "Itaú", "Lazer");

        verify(repository).save(existing);
        assertThat(existing.getDescricao()).isEqualTo("Atualizado");
        assertThat(existing.getValor()).isEqualByComparingTo("200");
        assertThat(existing.getTipo()).isEqualTo(TransactionType.CREDIT);
        assertThat(existing.getConta()).isEqualTo("Itaú");
    }

    @Test
    @DisplayName("should delete transaction by id")
    void delete_shouldRemoveTransaction() {
        service.delete(42L);
        verify(repository).deleteById(42L);
    }

    @Test
    @DisplayName("should bulk update account and category")
    void bulkUpdate_shouldChangeAccountAndCategory() {
        Transaction t1 = new Transaction(); t1.setId(1L);
        Transaction t2 = new Transaction(); t2.setId(2L);
        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(t1, t2));

        service.bulkUpdate(List.of(1L, 2L), "NovaConta", "NovaCategoria");

        verify(repository).saveAll(List.of(t1, t2));
        assertThat(t1.getConta()).isEqualTo("NovaConta");
        assertThat(t1.getCategoria()).isEqualTo("NovaCategoria");
        assertThat(t2.getConta()).isEqualTo("NovaConta");
        assertThat(t2.getCategoria()).isEqualTo("NovaCategoria");
    }

    @Test
    @DisplayName("should find filtered transactions")
    void findFiltered_shouldCallRepository() {
        service.findFiltered(2026, 6, null, TransactionType.DEBIT, "Alimentação");
        verify(repository).findFiltered(2026, 6, null, TransactionType.DEBIT, "Alimentação");
    }

    @Test
    @DisplayName("should find by id")
    void findById_shouldReturnTransaction() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Transaction()));
        assertThat(service.findById(1L)).isPresent();
    }
}
