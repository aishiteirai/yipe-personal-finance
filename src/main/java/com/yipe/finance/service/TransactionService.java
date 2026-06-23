package com.yipe.finance.service;

import com.yipe.finance.dto.TransactionDTO;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.mapper.TransactionMapper;
import com.yipe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    public TransactionService(TransactionRepository repository, TransactionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public void create(TransactionDTO dto) {
        if (dto.isRecorrente() && dto.getQtdMeses() > 1) {
            createRecurring(dto);
        } else {
            Transaction t = mapper.toEntity(dto);
            t.setParcela("Única");
            t.setValor(dto.getValor());
            repository.save(t);
        }
    }

    private void createRecurring(TransactionDTO dto) {
        int months = dto.getQtdMeses();
        BigDecimal installmentValue = dto.getValor();
        boolean isParcelado = "parcelado".equals(dto.getTipoRecorrencia());

        if (isParcelado) {
            installmentValue = dto.getValor().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_EVEN);
        }

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            Transaction t = mapper.toEntity(dto);
            t.setData(dto.getData().plusMonths(i));
            t.setValor(installmentValue);

            if (isParcelado) {
                t.setParcela((i + 1) + "/" + months);
            } else {
                t.setParcela("Recorrente");
            }

            transactions.add(t);
        }
        repository.saveAll(transactions);
    }

    public List<Transaction> findFiltered(Integer year, Integer month, Integer day,
                                          TransactionType tipo, String categoria) {
        return repository.findFiltered(year, month, day, tipo, categoria);
    }

    public Optional<Transaction> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void update(Long id, TransactionType tipo, LocalDate data, BigDecimal valor,
                       String descricao, String conta, String categoria) {
        Transaction t = repository.findById(id).orElseThrow();
        t.setTipo(tipo);
        t.setData(data);
        t.setValor(valor);
        t.setDescricao(descricao);
        t.setConta(conta);
        t.setCategoria(categoria);
        repository.save(t);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void bulkUpdate(List<Long> ids, String conta, String categoria) {
        List<Transaction> transactions = repository.findAllById(ids);
        for (Transaction t : transactions) {
            if (conta != null && !conta.isBlank()) t.setConta(conta);
            if (categoria != null && !categoria.isBlank()) t.setCategoria(categoria);
        }
        repository.saveAll(transactions);
    }

    public Map<String, List<Transaction>> findInstallmentGroups() {
        List<Transaction> installments = repository.findInstallments();
        return installments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDescricao() + " | " + t.getConta() + " | R$ " + t.getValor(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Transactional
    public void restructure(String descricao, String conta, BigDecimal valor,
                            int novoDia, int novaQuantidade, BigDecimal novoValor, String novaConta) {
        List<Transaction> oldGroup = repository.findInstallmentGroup(descricao, conta, valor);
        if (oldGroup.isEmpty()) return;

        Transaction first = oldGroup.get(0);
        repository.deleteAll(oldGroup);

        int mesInicio = first.getData().getMonthValue();
        int anoInicio = first.getData().getYear();

        List<Transaction> novas = new ArrayList<>();
        for (int i = 0; i < novaQuantidade; i++) {
            int targetMonth = mesInicio + i;
            int targetYear = anoInicio + (targetMonth - 1) / 12;
            targetMonth = ((targetMonth - 1) % 12) + 1;

            int safeDay = Math.min(novoDia,
                    java.time.YearMonth.of(targetYear, targetMonth).lengthOfMonth());

            Transaction t = new Transaction();
            t.setData(LocalDate.of(targetYear, targetMonth, safeDay));
            t.setTipo(first.getTipo());
            t.setValor(novoValor);
            t.setCategoria(first.getCategoria());
            t.setConta(novaConta);
            t.setDescricao(first.getDescricao());
            t.setParcela((i + 1) + "/" + novaQuantidade);
            novas.add(t);
        }
        repository.saveAll(novas);
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public List<Integer> findDistinctYears() {
        return repository.findDistinctYears();
    }
}
