package com.yipe.finance.service;

import com.yipe.finance.entity.Salary;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.repository.SalaryRepository;
import com.yipe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private static final List<TransactionType> EXPENSE_TYPES = List.of(
            TransactionType.DEBIT, TransactionType.CREDIT, TransactionType.VR);
    private static final List<TransactionType> INVEST_TYPES = List.of(
            TransactionType.INVESTMENT, TransactionType.RESERVE);

    private final TransactionRepository transactionRepository;
    private final SalaryRepository salaryRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(TransactionRepository transactionRepository,
                         SalaryRepository salaryRepository,
                         CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.salaryRepository = salaryRepository;
        this.categoryRepository = categoryRepository;
    }

    public BudgetData calculate(int year, int month,
                                 BigDecimal baseIncome,
                                 int pctNeeds, int pctWants, int pctInvest,
                                 List<String> catNeeds, List<String> catWants) {
        List<String> allCategories = categoryRepository.findAll().stream()
                .map(c -> c.getNome()).collect(Collectors.toList());

        BigDecimal realIncome = transactionRepository.sumByTipoInAndMonth(
                List.of(TransactionType.INCOME), year, month);
        BigDecimal expectedIncome = salaryRepository.findAll().stream()
                .map(Salary::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveIncome = (baseIncome != null) ? baseIncome
                : (realIncome.compareTo(BigDecimal.ZERO) > 0 ? realIncome : expectedIncome);

        List<String> needsList = (catNeeds != null) ? catNeeds : defaultNeeds(allCategories);
        List<String> wantsList = (catWants != null) ? catWants : defaultWants(allCategories);

        List<TransactionType> investTypes = List.of(TransactionType.INVESTMENT, TransactionType.RESERVE);
        List<TransactionType> expenseTypes = List.of(TransactionType.DEBIT, TransactionType.CREDIT, TransactionType.VR);

        BigDecimal spentNeeds = sumByCategoriesAndTypes(year, month, needsList, expenseTypes);
        BigDecimal spentWants = sumByCategoriesAndTypes(year, month, wantsList, expenseTypes);
        BigDecimal spentInvest = transactionRepository.sumByTipoInAndMonth(investTypes, year, month);

        BigDecimal ceilingNeeds = effectiveIncome.multiply(BigDecimal.valueOf(pctNeeds))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal ceilingWants = effectiveIncome.multiply(BigDecimal.valueOf(pctWants))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal ceilingInvest = effectiveIncome.multiply(BigDecimal.valueOf(pctInvest))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new BudgetData(year, month, effectiveIncome, realIncome, expectedIncome,
                pctNeeds, pctWants, pctInvest,
                spentNeeds, spentWants, spentInvest,
                ceilingNeeds, ceilingWants, ceilingInvest,
                needsList, wantsList, allCategories);
    }

    private BigDecimal sumByCategoriesAndTypes(int year, int month,
                                                List<String> categories,
                                                List<TransactionType> types) {
        List<TransactionType> excludeTypes = new ArrayList<>(List.of(TransactionType.values()));
        excludeTypes.removeAll(types);

        List<com.yipe.finance.entity.Transaction> txns =
                transactionRepository.findByYearAndMonthExcluding(year, month, excludeTypes);

        return txns.stream()
                .filter(t -> categories.contains(t.getCategoria()))
                .map(com.yipe.finance.entity.Transaction::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Integer> getAvailableYears() {
        List<Integer> years = transactionRepository.findDistinctYears();
        int currentYear = LocalDate.now().getYear();
        if (!years.contains(currentYear)) {
            years = new ArrayList<>(years);
            years.add(0, currentYear);
        }
        return years;
    }

    private List<String> defaultNeeds(List<String> allCats) {
        Set<String> defaults = Set.of("Transporte", "Alimentação");
        return allCats.stream().filter(defaults::contains).collect(Collectors.toList());
    }

    private List<String> defaultWants(List<String> allCats) {
        Set<String> defaults = Set.of("Lazer", "Bobeiras", "Presentes");
        return allCats.stream().filter(defaults::contains).collect(Collectors.toList());
    }

    public static class BudgetData {
        private final int year;
        private final int month;
        private final BigDecimal baseIncome;
        private final BigDecimal realIncome;
        private final BigDecimal expectedIncome;
        private final int pctNeeds;
        private final int pctWants;
        private final int pctInvest;
        private final BigDecimal spentNeeds;
        private final BigDecimal spentWants;
        private final BigDecimal spentInvest;
        private final BigDecimal ceilingNeeds;
        private final BigDecimal ceilingWants;
        private final BigDecimal ceilingInvest;
        private final List<String> catNeeds;
        private final List<String> catWants;
        private final List<String> allCategories;

        public BudgetData(int year, int month, BigDecimal baseIncome,
                          BigDecimal realIncome, BigDecimal expectedIncome,
                          int pctNeeds, int pctWants, int pctInvest,
                          BigDecimal spentNeeds, BigDecimal spentWants, BigDecimal spentInvest,
                          BigDecimal ceilingNeeds, BigDecimal ceilingWants, BigDecimal ceilingInvest,
                          List<String> catNeeds, List<String> catWants, List<String> allCategories) {
            this.year = year;
            this.month = month;
            this.baseIncome = baseIncome;
            this.realIncome = realIncome;
            this.expectedIncome = expectedIncome;
            this.pctNeeds = pctNeeds;
            this.pctWants = pctWants;
            this.pctInvest = pctInvest;
            this.spentNeeds = spentNeeds;
            this.spentWants = spentWants;
            this.spentInvest = spentInvest;
            this.ceilingNeeds = ceilingNeeds;
            this.ceilingWants = ceilingWants;
            this.ceilingInvest = ceilingInvest;
            this.catNeeds = catNeeds;
            this.catWants = catWants;
            this.allCategories = allCategories;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public BigDecimal getBaseIncome() { return baseIncome; }
        public BigDecimal getRealIncome() { return realIncome; }
        public BigDecimal getExpectedIncome() { return expectedIncome; }
        public int getPctNeeds() { return pctNeeds; }
        public int getPctWants() { return pctWants; }
        public int getPctInvest() { return pctInvest; }
        public BigDecimal getSpentNeeds() { return spentNeeds; }
        public BigDecimal getSpentWants() { return spentWants; }
        public BigDecimal getSpentInvest() { return spentInvest; }
        public BigDecimal getCeilingNeeds() { return ceilingNeeds; }
        public BigDecimal getCeilingWants() { return ceilingWants; }
        public BigDecimal getCeilingInvest() { return ceilingInvest; }
        public List<String> getCatNeeds() { return catNeeds; }
        public List<String> getCatWants() { return catWants; }
        public List<String> getAllCategories() { return allCategories; }

        public BigDecimal getPctSpentNeeds() {
            return ceilingNeeds.compareTo(BigDecimal.ZERO) > 0
                    ? spentNeeds.divide(ceilingNeeds, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
        }
        public BigDecimal getPctSpentWants() {
            return ceilingWants.compareTo(BigDecimal.ZERO) > 0
                    ? spentWants.divide(ceilingWants, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
        }
        public BigDecimal getPctSpentInvest() {
            return ceilingInvest.compareTo(BigDecimal.ZERO) > 0
                    ? spentInvest.divide(ceilingInvest, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
        }

        public double getDisplayPctNeeds() {
            return Math.min(getPctSpentNeeds().doubleValue(), 100.0);
        }
        public double getDisplayPctWants() {
            return Math.min(getPctSpentWants().doubleValue(), 100.0);
        }
        public double getDisplayPctInvest() {
            return Math.min(getPctSpentInvest().doubleValue(), 100.0);
        }

        public String getBarColorNeeds() {
            double pct = getDisplayPctNeeds();
            return pct >= 100.0 ? "#ff4b4b" : pct >= 85.0 ? "#ffa421" : "#1f77b4";
        }
        public String getBarColorWants() {
            double pct = getDisplayPctWants();
            return pct >= 100.0 ? "#ff4b4b" : pct >= 85.0 ? "#ffa421" : "#9467bd";
        }
        public String getBarColorInvest() {
            double pct = getDisplayPctInvest();
            return pct >= 100.0 ? "#ff4b4b" : pct >= 85.0 ? "#ffa421" : "#2ca02c";
        }

        public String getMonthName() {
            return MESES_PT[month - 1];
        }
    }

    private static final String[] MESES_PT = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };
}
