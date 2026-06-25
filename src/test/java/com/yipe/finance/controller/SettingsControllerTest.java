package com.yipe.finance.controller;

import com.yipe.finance.entity.Account;
import com.yipe.finance.entity.Card;
import com.yipe.finance.entity.Category;
import com.yipe.finance.entity.Salary;
import com.yipe.finance.repository.AccountRepository;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.repository.SalaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
@WithMockUser
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CardRepository cardRepository;

    @MockitoBean
    AccountRepository accountRepository;

    @MockitoBean
    CategoryRepository categoryRepository;

    @MockitoBean
    SalaryRepository salaryRepository;

    @Test
    @DisplayName("GET /settings should return settings view")
    void settings_shouldReturnView() throws Exception {
        when(cardRepository.findAll()).thenReturn(List.of());
        when(accountRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(salaryRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attributeExists("cards"))
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("salaries"));
    }

    @Test
    @DisplayName("GET /settings with editType=card should add editItem to model")
    void settings_withEditCard_shouldAddEditItem() throws Exception {
        var card = new Card("Nubank", "Nubank", 5, 10);
        when(cardRepository.findAll()).thenReturn(List.of(card));
        when(accountRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(salaryRepository.findAll()).thenReturn(List.of());
        when(cardRepository.findById("Nubank")).thenReturn(Optional.of(card));

        mockMvc.perform(get("/settings")
                        .param("editType", "card")
                        .param("editKey", "Nubank"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attribute("editSection", "cards"))
                .andExpect(model().attributeExists("editItem"));
    }

    @Test
    @DisplayName("POST /settings/cards/save should redirect")
    void saveCard_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/cards/save")
                        .param("nome", "Nubank")
                        .param("banco", "Nubank")
                        .param("diaFechamento", "5")
                        .param("diaVencimento", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#cards"));
    }

    @Test
    @DisplayName("POST /settings/cards/delete should redirect")
    void deleteCard_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/cards/delete")
                        .param("nome", "Nubank")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#cards"));
    }

    @Test
    @DisplayName("POST /settings/accounts/save should redirect")
    void saveAccount_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/accounts/save")
                        .param("nome", "NuConta")
                        .param("tipo", "BANK")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#accounts"));
    }

    @Test
    @DisplayName("POST /settings/accounts/delete should redirect")
    void deleteAccount_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/accounts/delete")
                        .param("nome", "NuConta")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#accounts"));
    }

    @Test
    @DisplayName("POST /settings/categories/save should redirect")
    void saveCategory_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/categories/save")
                        .param("nome", "Alimentação")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#categories"));
    }

    @Test
    @DisplayName("POST /settings/categories/delete should redirect")
    void deleteCategory_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/categories/delete")
                        .param("nome", "Alimentação")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#categories"));
    }

    @Test
    @DisplayName("POST /settings/salaries/save should redirect")
    void saveSalary_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/salaries/save")
                        .param("nome", "Salário")
                        .param("dia", "5")
                        .param("valor", "5000")
                        .param("conta", "NuConta")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#salaries"));
    }

    @Test
    @DisplayName("POST /settings/salaries/delete should redirect")
    void deleteSalary_shouldRedirect() throws Exception {
        mockMvc.perform(post("/settings/salaries/delete")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#salaries"));
    }

    @Test
    @DisplayName("POST /settings/cards/save with rename should delete old card and save new")
    void saveCard_withRename_shouldReplace() throws Exception {
        mockMvc.perform(post("/settings/cards/save")
                        .param("nome", "NovoNome")
                        .param("banco", "Banco")
                        .param("diaFechamento", "10")
                        .param("diaVencimento", "15")
                        .param("originalNome", "NomeAntigo")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings#cards"));

        verify(cardRepository).deleteById("NomeAntigo");
        verify(cardRepository).save(any(Card.class));
    }
}
