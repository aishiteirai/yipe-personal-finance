package com.yipe.finance.controller;

import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Integer ano,
                            @RequestParam(required = false) Integer mes,
                            Model model) {

        model.addAttribute("activePage", "dashboard");

        // --- Overall totals ---
        BigDecimal entradasTotais = dashboardService.sumByTypes(
                List.of(TransactionType.INCOME, TransactionType.ADJUSTMENT_INCOME));
        BigDecimal saidasTotais = dashboardService.sumByTypes(
                List.of(TransactionType.DEBIT, TransactionType.VR, TransactionType.ADJUSTMENT_EXPENSE));
        BigDecimal investimentosTotais = dashboardService.sumByTypes(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE));
        BigDecimal saldoGeral = entradasTotais.subtract(saidasTotais).subtract(investimentosTotais);

        model.addAttribute("saldoGeral", saldoGeral);
        model.addAttribute("totalInvestido", investimentosTotais);

        // --- Today's snapshot ---
        var hojeTransacoes = dashboardService.getTodayTransactions();
        BigDecimal gastoCredHoje = hojeTransacoes.stream()
                .filter(t -> t.getTipo() == TransactionType.CREDIT)
                .map(t -> t.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gastoDebHoje = hojeTransacoes.stream()
                .filter(t -> t.getTipo() == TransactionType.DEBIT || t.getTipo() == TransactionType.VR)
                .map(t -> t.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("gastoCredHoje", gastoCredHoje);
        model.addAttribute("gastoDebHoje", gastoDebHoje);
        model.addAttribute("saldoHoje", saldoGeral);

        // --- Monthly data ---
        int anoAtual = LocalDate.now().getYear();
        int mesAtual = LocalDate.now().getMonthValue();
        int anoSelecionado = ano != null ? ano : anoAtual;
        int mesSelecionado = mes != null ? mes : mesAtual;

        model.addAttribute("anoAtual", anoAtual);
        model.addAttribute("anoSelecionado", anoSelecionado);
        model.addAttribute("mesSelecionado", mesSelecionado);

        BigDecimal entradasMes = dashboardService.sumByTypesAndMonth(
                List.of(TransactionType.INCOME), anoSelecionado, mesSelecionado);
        BigDecimal despesasMes = dashboardService.sumByTypesAndMonth(
                List.of(TransactionType.DEBIT, TransactionType.CREDIT, TransactionType.VR),
                anoSelecionado, mesSelecionado);
        BigDecimal investMes = dashboardService.sumByTypesAndMonth(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE),
                anoSelecionado, mesSelecionado);

        model.addAttribute("entradasMes", entradasMes);
        model.addAttribute("despesasMes", despesasMes);
        model.addAttribute("investMes", investMes);

        // --- Chart: Daily evolution ---
        var monthTransacoes = dashboardService.getTransactionsForMonth(anoSelecionado, mesSelecionado);
        var gastosPorDia = dashboardService.getDailyExpenses(monthTransacoes);
        model.addAttribute("dias", gastosPorDia.keySet());
        model.addAttribute("valoresDiarios", gastosPorDia.values());

        // --- Chart: Expenses by category (bar chart) ---
        var gastosPorCategoria = dashboardService.getExpensesByCategory(monthTransacoes);
        model.addAttribute("categorias", gastosPorCategoria.keySet());
        model.addAttribute("valoresCategoria", gastosPorCategoria.values());

        // --- Chart: Sankey data ---
        var sankeyLinks = dashboardService.getSankeyData(monthTransacoes);
        model.addAttribute("sankeyLinks", sankeyLinks);

        // Collect unique nodes for sankey
        Set<String> allNodes = new LinkedHashSet<>();
        for (var link : sankeyLinks) {
            allNodes.add((String) link.get("source"));
            allNodes.add((String) link.get("target"));
        }
        model.addAttribute("sankeyNodes", allNodes);

        // --- Chart: Yearly stacked bar ---
        var yearlyExpenses = dashboardService.getYearlyExpenses(anoSelecionado);
        model.addAttribute("yearlyExpenses", yearlyExpenses);

        // Available years for selector
        List<Integer> years = List.of(anoAtual, anoAtual - 1, anoAtual - 2);
        model.addAttribute("availableYears", years);

        List<String> mesesPt = List.of("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");
        model.addAttribute("mesesPt", mesesPt);

        return "dashboard";
    }
}
