package com.yipe.finance.controller;

import com.yipe.finance.entity.Card;
import com.yipe.finance.service.InvoiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@WithMockUser
class InvoiceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InvoiceService invoiceService;

    @Test
    @DisplayName("GET /invoices with no cards should show empty state")
    void invoices_withNoCards_shouldShowEmptyState() throws Exception {
        when(invoiceService.getCardsWithTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attribute("noData", true));
    }

    @Test
    @DisplayName("GET /invoices with cards should render invoices")
    void invoices_withCards_shouldRenderInvoices() throws Exception {
        var card = new Card("Nubank", "Nubank", 5, 10);
        when(invoiceService.getCardsWithTransactions()).thenReturn(List.of(card));
        when(invoiceService.getInvoicePeriods("Nubank")).thenReturn(List.of("2026-06"));
        when(invoiceService.getInvoice("Nubank", "2026-06"))
                .thenReturn(new InvoiceService.InvoiceData(
                        "Nubank", "2026-06", BigDecimal.valueOf(1500), 10, "Junho", 2026, List.of()));

        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices"))
                .andExpect(model().attribute("noData", false))
                .andExpect(model().attribute("selectedCard", "Nubank"))
                .andExpect(model().attribute("selectedPeriod", "2026-06"))
                .andExpect(model().attributeExists("invoice"));
    }

    @Test
    @DisplayName("GET /invoices with specific card and period should use provided params")
    void invoices_withParams_shouldUseProvidedValues() throws Exception {
        var card = new Card("Nubank", "Nubank", 5, 10);
        var card2 = new Card("Inter", "Inter", 10, 15);
        when(invoiceService.getCardsWithTransactions()).thenReturn(List.of(card, card2));
        when(invoiceService.getInvoicePeriods("Inter")).thenReturn(List.of("2026-07", "2026-06"));
        when(invoiceService.getInvoice("Inter", "2026-07"))
                .thenReturn(new InvoiceService.InvoiceData(
                        "Inter", "2026-07", BigDecimal.valueOf(800), 15, "Julho", 2026, List.of()));

        mockMvc.perform(get("/invoices")
                        .param("cartao", "Inter")
                        .param("mesAno", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices"))
                .andExpect(model().attribute("selectedCard", "Inter"))
                .andExpect(model().attribute("selectedPeriod", "2026-07"))
                .andExpect(model().attributeExists("invoice"));
    }

    @Test
    @DisplayName("GET /invoices with invalid period should fall back to first available")
    void invoices_withInvalidPeriod_shouldFallback() throws Exception {
        var card = new Card("Nubank", "Nubank", 5, 10);
        when(invoiceService.getCardsWithTransactions()).thenReturn(List.of(card));
        when(invoiceService.getInvoicePeriods("Nubank")).thenReturn(List.of("2026-06", "2026-05"));
        when(invoiceService.getInvoice("Nubank", "2026-06"))
                .thenReturn(new InvoiceService.InvoiceData(
                        "Nubank", "2026-06", BigDecimal.valueOf(1500), 10, "Junho", 2026, List.of()));

        mockMvc.perform(get("/invoices")
                        .param("mesAno", "2099-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices"))
                .andExpect(model().attribute("selectedPeriod", "2026-06"))
                .andExpect(model().attributeExists("invoice"));
    }
}
