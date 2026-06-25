package com.yipe.finance.repository;

import com.yipe.finance.entity.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    CardRepository repository;

    @BeforeEach
    void setUp() {
        em.persistAndFlush(new Card("Cartão Nubank", "Nubank", 25, 5));
        em.persistAndFlush(new Card("Cartão Itaú", "Itaú", 15, 10));
    }

    @Test
    @DisplayName("should save and find card by id")
    void shouldSaveAndFindById() {
        Optional<Card> result = repository.findById("Cartão Nubank");

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("Cartão Nubank");
        assertThat(result.get().getBanco()).isEqualTo("Nubank");
        assertThat(result.get().getDiaFechamento()).isEqualTo(25);
        assertThat(result.get().getDiaVencimento()).isEqualTo(5);
    }

    @Test
    @DisplayName("should return empty when card not found")
    void shouldReturnEmpty_whenNotFound() {
        Optional<Card> result = repository.findById("Cartão Inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find all cards")
    void shouldFindAll() {
        List<Card> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Card::getNome)
                .contains("Cartão Nubank", "Cartão Itaú");
    }

    @Test
    @DisplayName("should count cards")
    void shouldCount() {
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("should delete card")
    void shouldDelete() {
        repository.deleteById("Cartão Nubank");
        em.flush();

        assertThat(repository.findById("Cartão Nubank")).isEmpty();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should update card fields")
    void shouldUpdate() {
        Card card = repository.findById("Cartão Nubank").orElseThrow();
        card.setDiaFechamento(20);
        card.setDiaVencimento(10);
        em.flush();

        Card updated = repository.findById("Cartão Nubank").orElseThrow();
        assertThat(updated.getDiaFechamento()).isEqualTo(20);
        assertThat(updated.getDiaVencimento()).isEqualTo(10);
    }

    @Test
    @DisplayName("should save multiple cards")
    void shouldSaveAll() {
        repository.saveAll(List.of(
                new Card("Cartão Inter", "Inter", 10, 5),
                new Card("Cartão C6", "C6", 20, 15)
        ));
        em.flush();

        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("should return empty list when no cards")
    void shouldReturnEmpty_whenNoData() {
        repository.deleteAll();
        em.flush();

        assertThat(repository.findAll()).isEmpty();
    }
}
