package com.yipe.finance.controller;

import com.yipe.finance.service.BudgetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@WithMockUser
class BudgetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BudgetService budgetService;

    private BudgetService.BudgetData createBudgetData(int year, int month) {
        return new BudgetService.BudgetData(
                year, month, BigDecimal.valueOf(5000), BigDecimal.valueOf(4800),
                BigDecimal.valueOf(5000), 50, 30, 20,
                BigDecimal.valueOf(1500), BigDecimal.valueOf(1200), BigDecimal.valueOf(800),
                BigDecimal.valueOf(2500), BigDecimal.valueOf(1500), BigDecimal.valueOf(1000),
                List.of("Transporte", "Alimentação"), List.of("Lazer", "Bobeiras"),
                List.of("Transporte", "Alimentação", "Lazer", "Bobeiras", "Moradia", "Saúde")
        );
    }

    @Test
    @DisplayName("GET /budget should return budget view")
    void budgetForm_shouldReturnView() throws Exception {
        when(budgetService.getAvailableYears()).thenReturn(List.of(2026));
        when(budgetService.calculate(anyInt(), anyInt(), isNull(), anyInt(), anyInt(), anyInt(),
                isNull(), isNull())).thenReturn(createBudgetData(2026, 6));

        mockMvc.perform(get("/budget"))
                .andExpect(status().isOk())
                .andExpect(view().name("budget"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attributeExists("availableYears"))
                .andExpect(model().attributeExists("selectedYear"))
                .andExpect(model().attributeExists("selectedMonth"))
                .andExpect(model().attributeExists("monthNames"))
                .andExpect(model().attributeExists("data"));
    }

    @Test
    @DisplayName("POST /budget should calculate and return budget view")
    void budgetCalculate_shouldReturnView() throws Exception {
        when(budgetService.calculate(anyInt(), anyInt(), any(), anyInt(), anyInt(), anyInt(),
                anyList(), anyList())).thenReturn(createBudgetData(2026, 6));

        mockMvc.perform(post("/budget")
                        .param("year", "2026")
                        .param("month", "6")
                        .param("baseIncome", "6000")
                        .param("pctNeeds", "50")
                        .param("pctWants", "30")
                        .param("pctInvest", "20")
                        .param("catNeeds", "Transporte", "Alimentação")
                        .param("catWants", "Lazer")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("budget"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attributeExists("data"));
    }

    @Test
    @DisplayName("POST /budget with defaults should calculate with default percentages")
    void budgetCalculate_withDefaults_shouldReturnView() throws Exception {
        when(budgetService.calculate(anyInt(), anyInt(), isNull(), anyInt(), anyInt(), anyInt(),
                isNull(), isNull())).thenReturn(createBudgetData(2026, 6));

        mockMvc.perform(post("/budget")
                        .param("year", "2026")
                        .param("month", "6")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("budget"))
                .andExpect(model().attributeExists("data"));
    }

    @Test
    @DisplayName("POST /budget with different percentages should use provided values")
    void budgetCalculate_withCustomPercentages_shouldUseProvidedValues() throws Exception {
        when(budgetService.calculate(eq(2026), eq(6), isNull(), eq(70), eq(20), eq(10),
                isNull(), isNull())).thenReturn(createBudgetData(2026, 6));

        mockMvc.perform(post("/budget")
                        .param("year", "2026")
                        .param("month", "6")
                        .param("pctNeeds", "70")
                        .param("pctWants", "20")
                        .param("pctInvest", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("budget"));
    }
}
