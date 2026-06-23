package com.yipe.finance.controller;

import com.yipe.finance.entity.Account;
import com.yipe.finance.entity.Card;
import com.yipe.finance.entity.Category;
import com.yipe.finance.entity.Salary;
import com.yipe.finance.repository.AccountRepository;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.CategoryRepository;
import com.yipe.finance.repository.SalaryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SalaryRepository salaryRepository;

    public SettingsController(CardRepository cardRepository, AccountRepository accountRepository,
                              CategoryRepository categoryRepository, SalaryRepository salaryRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.salaryRepository = salaryRepository;
    }

    @GetMapping
    public String settings(@RequestParam(required = false) String editType,
                           @RequestParam(required = false) String editKey,
                           @RequestParam(required = false) Long editId,
                           Model model) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("cards", cardRepository.findAll());
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("salaries", salaryRepository.findAll());

        if ("card".equals(editType) && editKey != null) {
            model.addAttribute("editItem", cardRepository.findById(editKey).orElse(null));
            model.addAttribute("editSection", "cards");
        } else if ("account".equals(editType) && editKey != null) {
            model.addAttribute("editItem", accountRepository.findById(editKey).orElse(null));
            model.addAttribute("editSection", "accounts");
        } else if ("category".equals(editType) && editKey != null) {
            model.addAttribute("editItem", categoryRepository.findById(editKey).orElse(null));
            model.addAttribute("editSection", "categories");
        } else if ("salary".equals(editType) && editId != null) {
            model.addAttribute("editItem", salaryRepository.findById(editId).orElse(null));
            model.addAttribute("editSection", "salaries");
        }

        return "settings";
    }

    @Transactional
    @PostMapping("/cards/save")
    public String saveCard(@RequestParam String nome,
                           @RequestParam String banco,
                           @RequestParam Integer diaFechamento,
                           @RequestParam Integer diaVencimento,
                           @RequestParam(defaultValue = "") String originalNome,
                           RedirectAttributes ra) {
        if (!originalNome.isBlank() && !originalNome.equals(nome)) {
            cardRepository.deleteById(originalNome);
        }
        cardRepository.save(new Card(nome, banco, diaFechamento, diaVencimento));
        ra.addFlashAttribute("success", "Cartão salvo com sucesso!");
        return "redirect:/settings#cards";
    }

    @PostMapping("/cards/delete")
    public String deleteCard(@RequestParam String nome, RedirectAttributes ra) {
        cardRepository.deleteById(nome);
        ra.addFlashAttribute("success", "Cartão removido!");
        return "redirect:/settings#cards";
    }

    @Transactional
    @PostMapping("/accounts/save")
    public String saveAccount(@RequestParam String nome,
                              @RequestParam String tipo,
                              @RequestParam(defaultValue = "") String originalNome,
                              RedirectAttributes ra) {
        if (!originalNome.isBlank() && !originalNome.equals(nome)) {
            accountRepository.deleteById(originalNome);
        }
        accountRepository.save(new Account(nome, tipo));
        ra.addFlashAttribute("success", "Conta salva com sucesso!");
        return "redirect:/settings#accounts";
    }

    @PostMapping("/accounts/delete")
    public String deleteAccount(@RequestParam String nome, RedirectAttributes ra) {
        accountRepository.deleteById(nome);
        ra.addFlashAttribute("success", "Conta removida!");
        return "redirect:/settings#accounts";
    }

    @Transactional
    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam String nome,
                               @RequestParam(defaultValue = "") String originalNome,
                               RedirectAttributes ra) {
        if (!originalNome.isBlank() && !originalNome.equals(nome)) {
            categoryRepository.deleteById(originalNome);
        }
        categoryRepository.save(new Category(nome));
        ra.addFlashAttribute("success", "Categoria salva com sucesso!");
        return "redirect:/settings#categories";
    }

    @PostMapping("/categories/delete")
    public String deleteCategory(@RequestParam String nome, RedirectAttributes ra) {
        categoryRepository.deleteById(nome);
        ra.addFlashAttribute("success", "Categoria removida!");
        return "redirect:/settings#categories";
    }

    @PostMapping("/salaries/save")
    public String saveSalary(@RequestParam(required = false) Long id,
                             @RequestParam String nome,
                             @RequestParam Integer dia,
                             @RequestParam BigDecimal valor,
                             @RequestParam String conta,
                             RedirectAttributes ra) {
        Salary s = (id != null) ? salaryRepository.findById(id).orElse(new Salary()) : new Salary();
        s.setNome(nome);
        s.setDia(dia);
        s.setValor(valor);
        s.setConta(conta);
        salaryRepository.save(s);
        ra.addFlashAttribute("success", "Salário salvo com sucesso!");
        return "redirect:/settings#salaries";
    }

    @PostMapping("/salaries/delete")
    public String deleteSalary(@RequestParam Long id, RedirectAttributes ra) {
        salaryRepository.deleteById(id);
        ra.addFlashAttribute("success", "Salário removido!");
        return "redirect:/settings#salaries";
    }
}
