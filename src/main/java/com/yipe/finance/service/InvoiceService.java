package com.yipe.finance.service;

import com.yipe.finance.entity.Card;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public InvoiceService(TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    public String calculateInvoicePeriod(LocalDate purchaseDate, Card card) {
        int closingDay = card.getDiaFechamento();
        int month = purchaseDate.getMonthValue();
        int year = purchaseDate.getYear();

        if (purchaseDate.getDayOfMonth() >= closingDay) {
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }
        return String.format("%d-%02d", year, month);
    }

    public List<Card> getCardsWithTransactions() {
        List<Card> allCards = cardRepository.findAll();
        List<Transaction> creditTxns = transactionRepository.findByTipoIn(
                List.of(TransactionType.CREDIT));

        if (creditTxns.isEmpty()) return allCards;

        Set<String> cardNames = creditTxns.stream()
                .map(Transaction::getConta)
                .collect(Collectors.toSet());

        return allCards.stream()
                .filter(c -> cardNames.contains(c.getNome()))
                .collect(Collectors.toList());
    }

    public List<String> getInvoicePeriods(String cardName) {
        Card card = cardRepository.findById(cardName).orElse(null);
        if (card == null) return List.of();

        List<Transaction> creditTxns = transactionRepository
                .findByContaAndTipo(cardName, TransactionType.CREDIT);

        Set<String> periods = creditTxns.stream()
                .map(t -> calculateInvoicePeriod(t.getData(), card))
                .collect(Collectors.toCollection(TreeSet::new));

        String currentPeriod = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        periods.add(currentPeriod);

        return new ArrayList<>(periods).reversed();
    }

    public InvoiceData getInvoice(String cardName, String mesAno) {
        Card card = cardRepository.findById(cardName).orElse(null);
        if (card == null) return null;

        List<Transaction> allCredit = transactionRepository
                .findByContaAndTipo(cardName, TransactionType.CREDIT);

        List<Transaction> invoiceTxns = allCredit.stream()
                .filter(t -> calculateInvoicePeriod(t.getData(), card).equals(mesAno))
                .sorted((a, b) -> b.getData().compareTo(a.getData()))
                .collect(Collectors.toList());

        BigDecimal total = invoiceTxns.stream()
                .map(Transaction::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String[] parts = mesAno.split("-");
        int anoF = Integer.parseInt(parts[0]);
        int mesF = Integer.parseInt(parts[1]);

        String nomeMes = MESES_PT[mesF - 1];

        return new InvoiceData(cardName, mesAno, total, card.getDiaVencimento(), nomeMes, anoF, invoiceTxns);
    }

    private static final String[] MESES_PT = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    public static class InvoiceData {
        private final String cartaoNome;
        private final String mesAno;
        private final BigDecimal total;
        private final int diaVencimento;
        private final String nomeMes;
        private final int ano;
        private final List<Transaction> transactions;

        public InvoiceData(String cartaoNome, String mesAno, BigDecimal total,
                           int diaVencimento, String nomeMes, int ano,
                           List<Transaction> transactions) {
            this.cartaoNome = cartaoNome;
            this.mesAno = mesAno;
            this.total = total;
            this.diaVencimento = diaVencimento;
            this.nomeMes = nomeMes;
            this.ano = ano;
            this.transactions = transactions;
        }

        public String getCartaoNome() { return cartaoNome; }
        public String getMesAno() { return mesAno; }
        public BigDecimal getTotal() { return total; }
        public int getDiaVencimento() { return diaVencimento; }
        public String getNomeMes() { return nomeMes; }
        public int getAno() { return ano; }
        public List<Transaction> getTransactions() { return transactions; }
    }
}
