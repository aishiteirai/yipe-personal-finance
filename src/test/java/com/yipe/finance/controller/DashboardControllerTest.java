package com.yipe.finance.controller;

import com.yipe.finance.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@WithMockUser
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DashboardService dashboardService;

    private void mockChartData() {
        when(dashboardService.sumByTypesAndMonth(anyList(), anyInt(), anyInt())).thenReturn(BigDecimal.ZERO);
        when(dashboardService.getTransactionsForMonth(anyInt(), anyInt())).thenReturn(List.of());
        when(dashboardService.getExpensesByCategory(anyList())).thenReturn(Map.of());
        when(dashboardService.getDailyExpenses(anyList())).thenReturn(Map.of());
        when(dashboardService.getSankeyData(anyList())).thenReturn(List.of());
        when(dashboardService.getYearlyExpenses(anyInt())).thenReturn(List.of());
    }

    @Test
    @DisplayName("GET /dashboard should return dashboard view")
    void dashboard_shouldReturnView() throws Exception {
        when(dashboardService.sumByTypes(anyList())).thenReturn(BigDecimal.ZERO);
        when(dashboardService.sumByTypesAndMonth(anyList(), anyInt(), anyInt())).thenReturn(BigDecimal.ZERO);
        when(dashboardService.getTransactionsForMonth(anyInt(), anyInt())).thenReturn(List.of());
        when(dashboardService.getTodayTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("activePage"));
    }

    @Test
    @DisplayName("GET / should redirect to dashboard")
    void home_shouldRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("GET /dashboard/charts should return chart fragment")
    void chartsFragment_shouldReturnFragment() throws Exception {
        mockChartData();

        mockMvc.perform(get("/dashboard/charts"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard :: chartsArea"))
                .andExpect(model().attributeExists("activePage", "dias", "valoresDiarios",
                        "categorias", "valoresCategoria", "sankeyLinks", "sankeyNodes",
                        "yearlyExpenses", "entradasMes", "despesasMes", "investMes",
                        "anoSelecionado", "mesSelecionado", "mesesPt"));
    }

    @Test
    @DisplayName("GET /dashboard/charts should filter by ano/mes params")
    void chartsFragment_shouldAcceptParams() throws Exception {
        mockChartData();

        mockMvc.perform(get("/dashboard/charts")
                        .param("ano", "2025")
                        .param("mes", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("anoSelecionado", 2025))
                .andExpect(model().attribute("mesSelecionado", 3));
    }
}
