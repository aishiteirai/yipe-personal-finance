package com.yipe.finance.controller;

import com.yipe.finance.dto.TransactionDTO;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.AccountRepository;
import com.yipe.finance.entity.Transaction;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CategoryRepository categoryRepository;

    public TransactionController(TransactionService service,
                                 AccountRepository accountRepository,
                                 CardRepository cardRepository,
                                 CategoryRepository categoryRepository) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("activePage", "transactions");
        model.addAttribute("dto", new TransactionDTO());
        model.addAttribute("tipos", TransactionType.values());
        model.addAttribute("contas", accountRepository.findAll());
        model.addAttribute("cartoes", cardRepository.findAll());
        model.addAttribute("categorias", categoryRepository.findAll());
        return "transactions/form";
    }

    @PostMapping
    public String create(@Valid TransactionDTO dto, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "transactions");
            model.addAttribute("tipos", TransactionType.values());
            model.addAttribute("contas", accountRepository.findAll());
            model.addAttribute("cartoes", cardRepository.findAll());
            model.addAttribute("categorias", categoryRepository.findAll());
            return "transactions/form";
        }
        service.create(dto);
        redirect.addFlashAttribute("success", "Lançamento registrado com sucesso!");
        return "redirect:/transactions";
    }
}
