package com.yipe.finance.controller;

import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.AccountRepository;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatementController.class)
@WithMockUser
class StatementControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionService transactionService;

    @MockitoBean
    AccountRepository accountRepository;

    @MockitoBean
    CardRepository cardRepository;

    @MockitoBean
    CategoryRepository categoryRepository;

    @Test
    @DisplayName("GET /statement should return statement view")
    void statement_shouldReturnView() throws Exception {
        when(transactionService.findFiltered(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(transactionService.findDistinctYears()).thenReturn(List.of(2026));
        when(accountRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(transactionService.findInstallmentGroups()).thenReturn(Map.of());

        mockMvc.perform(get("/statement"))
                .andExpect(status().isOk())
                .andExpect(view().name("statement"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("availableYears"))
                .andExpect(model().attributeExists("allTipos"))
                .andExpect(model().attributeExists("allContas"))
                .andExpect(model().attributeExists("allCategorias"))
                .andExpect(model().attributeExists("installmentGroups"));
    }

    @Test
    @DisplayName("GET /statement with filters should pass filter params to model")
    void statement_withFilters_shouldPassFilters() throws Exception {
        when(transactionService.findFiltered(anyInt(), any(), any(), any(), anyString())).thenReturn(List.of());
        when(transactionService.findDistinctYears()).thenReturn(List.of(2026));
        when(accountRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(transactionService.findInstallmentGroups()).thenReturn(Map.of());

        mockMvc.perform(get("/statement")
                        .param("ano", "2026")
                        .param("mes", "6")
                        .param("tipo", "DEBIT"))
                .andExpect(status().isOk())
                .andExpect(view().name("statement"))
                .andExpect(model().attribute("filtroAno", 2026))
                .andExpect(model().attribute("filtroMes", 6))
                .andExpect(model().attribute("filtroTipo", TransactionType.DEBIT));
    }

    @Test
    @DisplayName("GET /statement with editId should add editTransaction to model")
    void statement_withEditId_shouldSetEditTransaction() throws Exception {
        var txn = new com.yipe.finance.entity.Transaction();
        txn.setId(1L);
        txn.setTipo(TransactionType.DEBIT);
        txn.setData(LocalDate.of(2026, 1, 15));
        txn.setValor(BigDecimal.valueOf(100));
        txn.setDescricao("Test");
        txn.setConta("NuConta");
        txn.setCategoria("Alimentação");
        when(transactionService.findFiltered(any(), any(), any(), any(), any())).thenReturn(List.of(txn));
        when(transactionService.findDistinctYears()).thenReturn(List.of(2026));
        when(accountRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(transactionService.findInstallmentGroups()).thenReturn(Map.of());
        when(transactionService.findById(1L)).thenReturn(Optional.of(txn));

        mockMvc.perform(get("/statement").param("editId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("statement"))
                .andExpect(model().attributeExists("editTransaction"));
    }

    @Test
    @DisplayName("POST /statement/{id}/update should redirect to statement")
    void update_shouldRedirect() throws Exception {
        mockMvc.perform(post("/statement/1/update")
                        .param("tipo", "DEBIT")
                        .param("data", "2026-01-15")
                        .param("valor", "100.00")
                        .param("descricao", "Test")
                        .param("conta", "NuConta")
                        .param("categoria", "Alimentação")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/statement"));
    }

    @Test
    @DisplayName("POST /statement/{id}/delete should redirect to statement")
    void delete_shouldRedirect() throws Exception {
        mockMvc.perform(post("/statement/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/statement"));
    }

    @Test
    @DisplayName("POST /statement/bulk-update should redirect to statement")
    void bulkUpdate_shouldRedirect() throws Exception {
        mockMvc.perform(post("/statement/bulk-update")
                        .param("ids", "1", "2", "3")
                        .param("conta", "NuConta")
                        .param("categoria", "Alimentação")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/statement"));
    }

    @Test
    @DisplayName("POST /statement/restructure should redirect to statement")
    void restructure_shouldRedirect() throws Exception {
        mockMvc.perform(post("/statement/restructure")
                        .param("descricao", "Compra parcelada")
                        .param("conta", "Cartao")
                        .param("valor", "1000.00")
                        .param("novoDia", "15")
                        .param("novaQuantidade", "6")
                        .param("novoValor", "166.67")
                        .param("novaConta", "Cartao2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/statement"));
    }
}
