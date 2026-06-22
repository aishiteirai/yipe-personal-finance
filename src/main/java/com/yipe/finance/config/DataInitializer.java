package com.yipe.finance.config;

import com.yipe.finance.entity.Account;
import com.yipe.finance.entity.Card;
import com.yipe.finance.entity.Category;
import com.yipe.finance.repository.AccountRepository;
import com.yipe.finance.repository.CardRepository;
import com.yipe.finance.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           AccountRepository accountRepository,
                           CardRepository cardRepository) {
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Transporte"));
            categoryRepository.save(new Category("Alimentação"));
            categoryRepository.save(new Category("Lazer"));
            categoryRepository.save(new Category("Bobeiras"));
            categoryRepository.save(new Category("Presentes"));
            categoryRepository.save(new Category("Ajuste"));
            log.info("Default categories seeded.");
        }

        if (accountRepository.count() == 0) {
            accountRepository.save(new Account("Itaú", "Banco"));
            accountRepository.save(new Account("Nubank", "Banco"));
            accountRepository.save(new Account("Vale Refeição", "VR"));
            log.info("Default accounts seeded.");
        }

        if (cardRepository.count() == 0) {
            cardRepository.save(new Card("Cartão Nubank", "Nubank", 25, 5));
            log.info("Default card seeded.");
        }
    }
}
