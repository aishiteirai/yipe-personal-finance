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
        BigDecimal saldoGeral = dashboardService.computeSaldoGeral();
        BigDecimal investimentosTotais = dashboardService.sumByTypes(
                List.of(TransactionType.INVESTMENT, TransactionType.RESERVE));

        model.addAttribute("saldoGeral", saldoGeral);
        model.addAttribute("totalInvestido", investimentosTotais);

        // --- Today's snapshot ---
        var hojeTransacoes = dashboardService.getTodayTransactions();
        model.addAttribute("gastoCredHoje", dashboardService.computeGastoCreditoHoje(hojeTransacoes));
        model.addAttribute("gastoDebHoje", dashboardService.computeGastoDebitoHoje(hojeTransacoes));
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
        model.addAttribute("dias", new ArrayList<>(gastosPorDia.keySet()));
        model.addAttribute("valoresDiarios", new ArrayList<>(gastosPorDia.values()));

        // --- Chart: Expenses by category (bar chart) ---
        var gastosPorCategoria = dashboardService.getExpensesByCategory(monthTransacoes);
        model.addAttribute("categorias", new ArrayList<>(gastosPorCategoria.keySet()));
        model.addAttribute("valoresCategoria", new ArrayList<>(gastosPorCategoria.values()));

        // --- Chart: Sankey data ---
        var sankeyLinks = dashboardService.getSankeyData(monthTransacoes);
        model.addAttribute("sankeyLinks", sankeyLinks);

        Set<String> allNodes = new LinkedHashSet<>();
        for (var link : sankeyLinks) {
            allNodes.add((String) link.get("source"));
            allNodes.add((String) link.get("target"));
        }
        model.addAttribute("sankeyNodes", new ArrayList<>(allNodes));

        // --- Chart: Yearly stacked bar (pre-aggregated server-side) ---
        var yearlyChartData = dashboardService.getYearlyChartData(anoSelecionado);
        model.addAttribute("yearlyChartData", yearlyChartData);

    // Available years for selector
    List<Integer> years = List.of(anoAtual, anoAtual - 1, anoAtual - 2);
    model.addAttribute("availableYears", years);

    List<String> mesesPt = List.of("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");
    model.addAttribute("mesesPt", mesesPt);

    return "dashboard";
    }

    @GetMapping("/dashboard/charts")
    public String chartsFragment(@RequestParam(required = false) Integer ano,
                                  @RequestParam(required = false) Integer mes,
                                  Model model) {
        int anoAtual = LocalDate.now().getYear();
        int mesAtual = LocalDate.now().getMonthValue();
        int anoSelecionado = ano != null ? ano : anoAtual;
        int mesSelecionado = mes != null ? mes : mesAtual;

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

        var monthTransacoes = dashboardService.getTransactionsForMonth(anoSelecionado, mesSelecionado);
        var gastosPorDia = dashboardService.getDailyExpenses(monthTransacoes);
        model.addAttribute("dias", new ArrayList<>(gastosPorDia.keySet()));
        model.addAttribute("valoresDiarios", new ArrayList<>(gastosPorDia.values()));

        var gastosPorCategoria = dashboardService.getExpensesByCategory(monthTransacoes);
        model.addAttribute("categorias", new ArrayList<>(gastosPorCategoria.keySet()));
        model.addAttribute("valoresCategoria", new ArrayList<>(gastosPorCategoria.values()));

        var sankeyLinks = dashboardService.getSankeyData(monthTransacoes);
        model.addAttribute("sankeyLinks", sankeyLinks);

        Set<String> allNodes = new LinkedHashSet<>();
        for (var link : sankeyLinks) {
            allNodes.add((String) link.get("source"));
            allNodes.add((String) link.get("target"));
        }
        model.addAttribute("sankeyNodes", new ArrayList<>(allNodes));

        var yearlyChartData = dashboardService.getYearlyChartData(anoSelecionado);
        model.addAttribute("yearlyChartData", yearlyChartData);

        List<String> mesesPt = List.of("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");
        model.addAttribute("mesesPt", mesesPt);
        model.addAttribute("activePage", "dashboard");

        return "dashboard :: chartsArea";
    }
}
