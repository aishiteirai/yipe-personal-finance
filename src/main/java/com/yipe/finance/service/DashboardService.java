package com.yipe.finance.service;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionRepository repository;

    public DashboardService(TransactionRepository repository) {
        this.repository = repository;
    }

    public BigDecimal sumByTypes(List<TransactionType> tipos) {
        return repository.sumByTipoIn(tipos);
    }

    public BigDecimal sumByTypesAndMonth(List<TransactionType> tipos, int year, int month) {
        return repository.sumByTipoInAndMonth(tipos, year, month);
    }

    public List<Transaction> getTransactionsForMonth(int year, int month) {
        return repository.findByYearAndMonthExcluding(year, month,
                List.of(TransactionType.ADJUSTMENT_INCOME, TransactionType.ADJUSTMENT_EXPENSE));
    }

    public List<Transaction> getTodayTransactions() {
        return repository.findByData(LocalDate.now());
    }

    public BigDecimal computeSaldoGeral() {
        BigDecimal entradas = sumByTypes(List.of(TransactionType.INCOME, TransactionType.ADJUSTMENT_INCOME));
        BigDecimal saidas = sumByTypes(List.of(TransactionType.DEBIT, TransactionType.VR, TransactionType.ADJUSTMENT_EXPENSE));
        BigDecimal investimentos = sumByTypes(List.of(TransactionType.INVESTMENT, TransactionType.RESERVE));
        return entradas.subtract(saidas).subtract(investimentos);
    }

    public BigDecimal computeGastoCreditoHoje(List<Transaction> hojeTransacoes) {
        return hojeTransacoes.stream()
                .filter(t -> t.getTipo() == TransactionType.CREDIT)
                .map(Transaction::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal computeGastoDebitoHoje(List<Transaction> hojeTransacoes) {
        return hojeTransacoes.stream()
                .filter(t -> t.getTipo() == TransactionType.DEBIT || t.getTipo() == TransactionType.VR)
                .map(Transaction::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Integer, BigDecimal> getDailyExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getTipo() == TransactionType.DEBIT
                        || t.getTipo() == TransactionType.CREDIT
                        || t.getTipo() == TransactionType.VR)
                .collect(Collectors.groupingBy(
                        t -> t.getData().getDayOfMonth(),
                        TreeMap::new,
                        Collectors.mapping(Transaction::getValor,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    public Map<String, BigDecimal> getExpensesByCategory(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getTipo() == TransactionType.DEBIT
                        || t.getTipo() == TransactionType.CREDIT
                        || t.getTipo() == TransactionType.VR)
                .collect(Collectors.groupingBy(
                        Transaction::getCategoria,
                        LinkedHashMap::new,
                        Collectors.mapping(Transaction::getValor,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    public List<Map<String, Object>> getSankeyData(List<Transaction> transactions) {
        Map<String, Map<String, BigDecimal>> flow = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getTipo() == TransactionType.DEBIT
                    || t.getTipo() == TransactionType.CREDIT
                    || t.getTipo() == TransactionType.VR) {
                flow.computeIfAbsent(t.getConta(), k -> new HashMap<>())
                        .merge(t.getCategoria(), t.getValor(), BigDecimal::add);
            }
        }

        Set<String> nodes = new LinkedHashSet<>();
        List<Map<String, Object>> links = new ArrayList<>();
        for (var sourceEntry : flow.entrySet()) {
            nodes.add(sourceEntry.getKey());
            for (var targetEntry : sourceEntry.getValue().entrySet()) {
                nodes.add(targetEntry.getKey());
                Map<String, Object> link = new LinkedHashMap<>();
                link.put("source", sourceEntry.getKey());
                link.put("target", targetEntry.getKey());
                link.put("value", targetEntry.getValue());
                links.add(link);
            }
        }

        List<String> nodeList = new ArrayList<>(nodes);
        for (Map<String, Object> link : links) {
            link.put("sourceIdx", nodeList.indexOf(link.get("source")));
            link.put("targetIdx", nodeList.indexOf(link.get("target")));
        }

        return links;
    }

    public List<Transaction> getYearlyExpenses(int year) {
        return repository.findByYearAndTypes(year,
                List.of(TransactionType.ADJUSTMENT_INCOME, TransactionType.ADJUSTMENT_EXPENSE,
                        TransactionType.INCOME, TransactionType.INVESTMENT, TransactionType.RESERVE),
                List.of(TransactionType.DEBIT, TransactionType.CREDIT, TransactionType.VR));
    }
}
