package com.yipe.finance.controller;

import com.yipe.finance.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    private static final String[] MONTH_NAMES = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    @GetMapping
    public String budgetForm(Model model) {
        model.addAttribute("activePage", "budget");
        model.addAttribute("availableYears", budgetService.getAvailableYears());

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("monthNames", List.of(MONTH_NAMES));

        BudgetDataWrapper data = new BudgetDataWrapper();
        data.setBudgetData(budgetService.calculate(year, month, null, 50, 30, 20, null, null));
        model.addAttribute("data", data);

        return "budget";
    }

    @PostMapping
    public String budgetCalculate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) BigDecimal baseIncome,
            @RequestParam(defaultValue = "50") int pctNeeds,
            @RequestParam(defaultValue = "30") int pctWants,
            @RequestParam(defaultValue = "20") int pctInvest,
            @RequestParam(required = false) List<String> catNeeds,
            @RequestParam(required = false) List<String> catWants,
            Model model,
            HttpServletRequest request) {
        model.addAttribute("activePage", "budget");
        model.addAttribute("availableYears", budgetService.getAvailableYears());
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("monthNames", List.of(MONTH_NAMES));

        BudgetDataWrapper data = new BudgetDataWrapper();
        data.setBudgetData(budgetService.calculate(year, month, baseIncome,
                pctNeeds, pctWants, pctInvest, catNeeds, catWants));
        model.addAttribute("data", data);

        boolean htmx = "true".equals(request.getHeader("HX-Request"));
        return htmx ? "budget :: budgetResults" : "budget";
    }

    public static class BudgetDataWrapper {
        private BudgetService.BudgetData budgetData;

        public BudgetService.BudgetData getBudgetData() { return budgetData; }
        public void setBudgetData(BudgetService.BudgetData budgetData) { this.budgetData = budgetData; }
    }
}
