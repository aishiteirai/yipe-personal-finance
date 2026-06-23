package com.yipe.finance.controller;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.AccountRepository;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/statement")
public class StatementController {

    private final TransactionService service;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CategoryRepository categoryRepository;

    public StatementController(TransactionService service,
                               AccountRepository accountRepository,
                               CardRepository cardRepository,
                               CategoryRepository categoryRepository) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String statement(@RequestParam(required = false) Integer ano,
                            @RequestParam(required = false) Integer mes,
                            @RequestParam(required = false) Integer dia,
                            @RequestParam(required = false) TransactionType tipo,
                            @RequestParam(required = false) String categoria,
                            @RequestParam(required = false) Long editId,
                            Model model) {
        model.addAttribute("activePage", "statement");

        List<Transaction> transactions = service.findFiltered(ano, mes, dia, tipo, categoria);
        model.addAttribute("transactions", transactions);
        model.addAttribute("filtroAno", ano);
        model.addAttribute("filtroMes", mes);
        model.addAttribute("filtroDia", dia);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroCategoria", categoria);

        model.addAttribute("availableYears", service.findDistinctYears());

        model.addAttribute("allTipos", TransactionType.values());
        model.addAttribute("allContas", accountRepository.findAll());
        model.addAttribute("allCategorias", categoryRepository.findAll());

        model.addAttribute("installmentGroups", service.findInstallmentGroups());

        if (editId != null) {
            service.findById(editId).ifPresent(t -> model.addAttribute("editTransaction", t));
        } else if (!transactions.isEmpty()) {
            model.addAttribute("editTransaction", transactions.get(0));
        }

        return "statement";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam TransactionType tipo,
                         @RequestParam LocalDate data,
                         @RequestParam BigDecimal valor,
                         @RequestParam String descricao,
                         @RequestParam String conta,
                         @RequestParam String categoria,
                         RedirectAttributes redirect) {
        service.update(id, tipo, data, valor, descricao, conta, categoria);
        redirect.addFlashAttribute("success", "Lançamento atualizado!");
        return "redirect:/statement";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("success", "Lançamento excluído!");
        return "redirect:/statement";
    }

    @PostMapping("/bulk-update")
    public String bulkUpdate(@RequestParam(required = false) List<Long> ids,
                             @RequestParam(defaultValue = "") String conta,
                             @RequestParam(defaultValue = "") String categoria,
                             RedirectAttributes redirect) {
        if (ids != null && !ids.isEmpty()) {
            service.bulkUpdate(ids,
                    conta.isBlank() || "Não Alterar".equals(conta) ? null : conta,
                    categoria.isBlank() || "Não Alterar".equals(categoria) ? null : categoria);
            redirect.addFlashAttribute("success", ids.size() + " lançamento(s) atualizado(s) em massa!");
        }
        return "redirect:/statement";
    }

    @PostMapping("/restructure")
    public String restructure(@RequestParam String descricao,
                              @RequestParam String conta,
                              @RequestParam BigDecimal valor,
                              @RequestParam int novoDia,
                              @RequestParam int novaQuantidade,
                              @RequestParam BigDecimal novoValor,
                              @RequestParam String novaConta,
                              RedirectAttributes redirect) {
        service.restructure(descricao, conta, valor, novoDia, novaQuantidade, novoValor, novaConta);
        redirect.addFlashAttribute("success", "Parcelas reestruturadas com sucesso!");
        return "redirect:/statement";
    }
}
