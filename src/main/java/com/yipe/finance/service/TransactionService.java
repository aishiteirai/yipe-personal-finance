package com.yipe.finance.service;

import com.yipe.finance.dto.TransactionDTO;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void create(TransactionDTO dto) {
        if (dto.isRecorrente() && dto.getQtdMeses() > 1) {
            createRecurring(dto);
        } else {
            Transaction t = new Transaction();
            applyDto(t, dto);
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
            Transaction t = new Transaction();
            applyDto(t, dto);
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

    private void applyDto(Transaction t, TransactionDTO dto) {
        t.setData(dto.getData());
        t.setTipo(dto.getTipo());
        t.setDescricao(dto.getDescricao());
        t.setConta(dto.getConta());
        t.setCategoria(dto.getCategoria());
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }
}
