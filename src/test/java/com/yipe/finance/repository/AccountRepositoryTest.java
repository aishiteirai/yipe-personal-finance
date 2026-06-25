package com.yipe.finance.repository;

import com.yipe.finance.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    AccountRepository repository;

    private Account bankAccount;
    private Account vrAccount;

    @BeforeEach
    void setUp() {
        bankAccount = em.persistAndFlush(new Account("Itaú", "Banco"));
        vrAccount = em.persistAndFlush(new Account("Vale Refeição", "VR"));
    }

    @Nested
    @DisplayName("findByTipo")
    class FindByTipo {

        @Test
        @DisplayName("should find accounts by tipo")
        void shouldFindByTipo() {
            List<Account> result = repository.findByTipo("Banco");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNome()).isEqualTo("Itaú");
        }

        @Test
        @DisplayName("should return empty when tipo has no accounts")
        void shouldReturnEmpty_whenNoMatch() {
            List<Account> result = repository.findByTipo("Investimento");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD operations")
    class Crud {

        @Test
        @DisplayName("should save and find account by id")
        void shouldSaveAndFindById() {
            var saved = repository.findById("Itaú");

            assertThat(saved).isPresent();
            assertThat(saved.get().getTipo()).isEqualTo("Banco");
        }

        @Test
        @DisplayName("should return empty when account not found")
        void shouldReturnEmpty_whenNotFound() {
            var result = repository.findById("Banco Inexistente");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should delete account")
        void shouldDelete() {
            repository.deleteById("Itaú");
            em.flush();

            assertThat(repository.findById("Itaú")).isEmpty();
        }

        @Test
        @DisplayName("should update account tipo")
        void shouldUpdate() {
            Account account = repository.findById("Itaú").orElseThrow();
            account.setTipo("Banco Digital");
            em.flush();

            Account updated = repository.findById("Itaú").orElseThrow();
            assertThat(updated.getTipo()).isEqualTo("Banco Digital");
        }

        @Test
        @DisplayName("should find all accounts")
        void shouldFindAll() {
            List<Account> all = repository.findAll();

            assertThat(all).hasSize(2);
            assertThat(all).extracting(Account::getNome)
                    .contains("Itaú", "Vale Refeição");
        }

        @Test
        @DisplayName("should count accounts")
        void shouldCount() {
            assertThat(repository.count()).isEqualTo(2);
        }
    }
}
