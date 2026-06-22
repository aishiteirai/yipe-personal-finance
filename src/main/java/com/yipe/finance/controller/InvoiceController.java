package com.yipe.finance.controller;

import com.yipe.finance.service.InvoiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public String invoices(@RequestParam(required = false) String cartao,
                           @RequestParam(required = false) String mesAno,
                           Model model) {
        model.addAttribute("activePage", "invoices");

        var cards = invoiceService.getCardsWithTransactions();
        model.addAttribute("cartoes", cards);
        model.addAttribute("noData", cards.isEmpty());

        if (cards.isEmpty()) {
            return "invoices";
        }

        String selectedCard = (cartao != null && !cartao.isBlank()) ? cartao : cards.get(0).getNome();
        model.addAttribute("selectedCard", selectedCard);

        var periods = invoiceService.getInvoicePeriods(selectedCard);
        model.addAttribute("periods", periods);

        String selectedPeriod = (mesAno != null && !mesAno.isBlank()) ? mesAno
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (!periods.contains(selectedPeriod) && !periods.isEmpty()) {
            selectedPeriod = periods.get(0);
        }
        model.addAttribute("selectedPeriod", selectedPeriod);

        var invoice = invoiceService.getInvoice(selectedCard, selectedPeriod);
        model.addAttribute("invoice", invoice);

        return "invoices";
    }
}
