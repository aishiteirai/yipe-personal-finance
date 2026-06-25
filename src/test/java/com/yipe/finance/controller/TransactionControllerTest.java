package com.yipe.finance.controller;

import com.yipe.finance.dto.TransactionDTO;
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

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@WithMockUser
class TransactionControllerTest {

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
    @DisplayName("GET /transactions should return form view")
    void form_shouldReturnView() throws Exception {
        when(accountRepository.findAll()).thenReturn(List.of());
        when(cardRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions/form"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attributeExists("dto"))
                .andExpect(model().attributeExists("tipos"))
                .andExpect(model().attributeExists("contas"))
                .andExpect(model().attributeExists("cartoes"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    @DisplayName("POST /transactions with valid data should redirect")
    void create_withValidData_shouldRedirect() throws Exception {
        mockMvc.perform(post("/transactions")
                        .param("tipo", "DEBIT")
                        .param("data", "2026-01-15")
                        .param("valor", "150.00")
                        .param("descricao", "Supermercado")
                        .param("conta", "NuConta")
                        .param("categoria", "Alimentacao")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));
    }

    @Test
    @DisplayName("POST /transactions with recurring should create via service")
    void create_withRecurring_shouldCallService() throws Exception {
        mockMvc.perform(post("/transactions")
                        .param("tipo", "DEBIT")
                        .param("data", "2026-01-15")
                        .param("valor", "300.00")
                        .param("descricao", "Assinatura")
                        .param("conta", "NuConta")
                        .param("categoria", "Lazer")
                        .param("recorrente", "true")
                        .param("tipoRecorrencia", "parcelado")
                        .param("qtdMeses", "3")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        verify(transactionService).create(org.mockito.ArgumentMatchers.any(TransactionDTO.class));
    }
}
