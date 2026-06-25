package com.yipe.finance.service;

import com.yipe.finance.dto.TransactionDTO;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.mapper.TransactionMapper;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @Mock
    TransactionRepository repository;

    @Mock
    TransactionMapper mapper;

    @InjectMocks
    TransactionService service;

    @Captor
    ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    ArgumentCaptor<List<Transaction>> listCaptor;

    @BeforeEach
    void setUp() {
        when(mapper.toEntity(any(TransactionDTO.class))).thenAnswer(
                (Answer<Transaction>) invocation -> {
                    TransactionDTO dto = invocation.getArgument(0);
                    Transaction t = new Transaction();
                    t.setData(dto.getData());
                    t.setTipo(dto.getTipo());
                    t.setDescricao(dto.getDescricao());
                    t.setConta(dto.getConta());
                    t.setCategoria(dto.getCategoria());
                    return t;
                }
        );
    }

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

    // ---- Untested methods below ----

    @Test
    @DisplayName("should restructure installments with new count, value, day and account")
    void restructure_shouldCreateNewInstallments() {
        Transaction t1 = new Transaction();
        t1.setId(1L); t1.setData(LocalDate.of(2026, 1, 10));
        t1.setTipo(TransactionType.CREDIT); t1.setValor(BigDecimal.valueOf(300));
        t1.setCategoria("Eletrônicos"); t1.setConta("Nubank");
        t1.setDescricao("Compra"); t1.setParcela("1/3");
        Transaction t2 = new Transaction();
        t2.setId(2L); t2.setData(LocalDate.of(2026, 2, 10));
        t2.setTipo(TransactionType.CREDIT); t2.setValor(BigDecimal.valueOf(300));
        t2.setCategoria("Eletrônicos"); t2.setConta("Nubank");
        t2.setDescricao("Compra"); t2.setParcela("2/3");
        Transaction t3 = new Transaction();
        t3.setId(3L); t3.setData(LocalDate.of(2026, 3, 10));
        t3.setTipo(TransactionType.CREDIT); t3.setValor(BigDecimal.valueOf(300));
        t3.setCategoria("Eletrônicos"); t3.setConta("Nubank");
        t3.setDescricao("Compra"); t3.setParcela("3/3");

        when(repository.findInstallmentGroup("Compra", "Nubank", BigDecimal.valueOf(300)))
                .thenReturn(List.of(t1, t2, t3));

        service.restructure("Compra", "Nubank", BigDecimal.valueOf(300),
                15, 6, BigDecimal.valueOf(150), "Itaú");

        verify(repository).deleteAll(List.of(t1, t2, t3));
        verify(repository).saveAll(listCaptor.capture());
        List<Transaction> novas = listCaptor.getValue();
        assertThat(novas).hasSize(6);
        assertThat(novas.get(0).getData()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(novas.get(0).getValor()).isEqualByComparingTo("150");
        assertThat(novas.get(0).getConta()).isEqualTo("Itaú");
        assertThat(novas.get(0).getParcela()).isEqualTo("1/6");
        assertThat(novas.get(0).getTipo()).isEqualTo(TransactionType.CREDIT);
        assertThat(novas.get(0).getCategoria()).isEqualTo("Eletrônicos");
        assertThat(novas.get(0).getDescricao()).isEqualTo("Compra");
        assertThat(novas.get(5).getData()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(novas.get(5).getParcela()).isEqualTo("6/6");
    }

    @Test
    @DisplayName("should do nothing when old installment group is empty")
    void restructure_shouldDoNothing_whenEmptyGroup() {
        when(repository.findInstallmentGroup("Compra", "Nubank", BigDecimal.valueOf(300)))
                .thenReturn(List.of());

        service.restructure("Compra", "Nubank", BigDecimal.valueOf(300),
                15, 6, BigDecimal.valueOf(150), "Itaú");

        verify(repository, never()).deleteAll(any());
        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("should clamp day when target month has fewer days (e.g. Feb 31 -> 28)")
    void restructure_shouldClampDay_whenMonthTooShort() {
        Transaction t1 = new Transaction();
        t1.setId(1L); t1.setData(LocalDate.of(2026, 1, 31));
        t1.setTipo(TransactionType.CREDIT); t1.setValor(BigDecimal.valueOf(300));
        t1.setCategoria("Eletrônicos"); t1.setConta("Nubank");
        t1.setDescricao("Compra"); t1.setParcela("1/3");
        Transaction t2 = new Transaction();
        t2.setId(2L); t2.setData(LocalDate.of(2026, 2, 28));
        t2.setTipo(TransactionType.CREDIT); t2.setValor(BigDecimal.valueOf(300));
        t2.setCategoria("Eletrônicos"); t2.setConta("Nubank");
        t2.setDescricao("Compra"); t2.setParcela("2/3");
        Transaction t3 = new Transaction();
        t3.setId(3L); t3.setData(LocalDate.of(2026, 3, 31));
        t3.setTipo(TransactionType.CREDIT); t3.setValor(BigDecimal.valueOf(300));
        t3.setCategoria("Eletrônicos"); t3.setConta("Nubank");
        t3.setDescricao("Compra"); t3.setParcela("3/3");

        when(repository.findInstallmentGroup("Compra", "Nubank", BigDecimal.valueOf(300)))
                .thenReturn(List.of(t1, t2, t3));

        service.restructure("Compra", "Nubank", BigDecimal.valueOf(300),
                31, 3, BigDecimal.valueOf(300), "Nubank");

        verify(repository).saveAll(listCaptor.capture());
        List<Transaction> novas = listCaptor.getValue();
        assertThat(novas).hasSize(3);
        assertThat(novas.get(0).getData()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(novas.get(1).getData()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(novas.get(2).getData()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    @DisplayName("should return all transactions")
    void findAll_shouldReturnAll() {
        when(repository.findAll()).thenReturn(List.of(new Transaction(), new Transaction()));
        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("should return distinct years from repository")
    void findDistinctYears_shouldReturnYears() {
        when(repository.findDistinctYears()).thenReturn(List.of(2026, 2025));
        assertThat(service.findDistinctYears()).containsExactly(2026, 2025);
    }

    @Test
    @DisplayName("should group installments by descricao + conta + valor")
    void findInstallmentGroups_shouldGroup() {
        Transaction t1 = new Transaction();
        t1.setDescricao("Compra"); t1.setConta("Nubank"); t1.setValor(BigDecimal.valueOf(100));
        t1.setParcela("1/3");
        Transaction t2 = new Transaction();
        t2.setDescricao("Compra"); t2.setConta("Nubank"); t2.setValor(BigDecimal.valueOf(100));
        t2.setParcela("2/3");
        Transaction t3 = new Transaction();
        t3.setDescricao("Outra"); t3.setConta("Itaú"); t3.setValor(BigDecimal.valueOf(200));
        t3.setParcela("1/2");

        when(repository.findInstallments()).thenReturn(List.of(t1, t2, t3));

        Map<String, List<Transaction>> groups = service.findInstallmentGroups();

        assertThat(groups).hasSize(2);
        assertThat(groups.get("Compra | Nubank | R$ 100")).hasSize(2);
        assertThat(groups.get("Outra | Itaú | R$ 200")).hasSize(1);
    }

    @Test
    @DisplayName("should skip updating account when null or blank in bulkUpdate")
    void bulkUpdate_shouldSkipNullAccount() {
        Transaction t1 = new Transaction(); t1.setId(1L); t1.setConta("Nubank"); t1.setCategoria("Alimentação");
        Transaction t2 = new Transaction(); t2.setId(2L); t2.setConta("Itaú"); t2.setCategoria("Lazer");
        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(t1, t2));

        service.bulkUpdate(List.of(1L, 2L), "", "NovaCategoria");

        verify(repository).saveAll(List.of(t1, t2));
        assertThat(t1.getConta()).isEqualTo("Nubank");
        assertThat(t1.getCategoria()).isEqualTo("NovaCategoria");
        assertThat(t2.getConta()).isEqualTo("Itaú");
    }

    @Test
    @DisplayName("should skip updating categoria when null or blank in bulkUpdate")
    void bulkUpdate_shouldSkipNullCategoria() {
        Transaction t1 = new Transaction(); t1.setId(1L); t1.setConta("Nubank"); t1.setCategoria("Alimentação");
        when(repository.findAllById(List.of(1L))).thenReturn(List.of(t1));

        service.bulkUpdate(List.of(1L), "NovaConta", null);

        verify(repository).saveAll(List.of(t1));
        assertThat(t1.getConta()).isEqualTo("NovaConta");
        assertThat(t1.getCategoria()).isEqualTo("Alimentação");
    }

    @Test
    @DisplayName("should find filtered with all null parameters")
    void findFiltered_shouldHandleAllNull() {
        service.findFiltered(null, null, null, null, null);
        verify(repository).findFiltered(null, null, null, null, null);
    }
}
