package com.yipe.finance.repository;

import com.yipe.finance.entity.Category;
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
class CategoryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    CategoryRepository repository;

    @BeforeEach
    void setUp() {
        em.persistAndFlush(new Category("Alimentação"));
        em.persistAndFlush(new Category("Transporte"));
        em.persistAndFlush(new Category("Lazer"));
    }

    @Test
    @DisplayName("should save and find category by id")
    void shouldSaveAndFindById() {
        Optional<Category> result = repository.findById("Alimentação");

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("Alimentação");
    }

    @Test
    @DisplayName("should return empty when category not found")
    void shouldReturnEmpty_whenNotFound() {
        Optional<Category> result = repository.findById("Inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find all categories")
    void shouldFindAll() {
        List<Category> all = repository.findAll();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(Category::getNome)
                .contains("Alimentação", "Transporte", "Lazer");
    }

    @Test
    @DisplayName("should count categories")
    void shouldCount() {
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("should delete category")
    void shouldDelete() {
        repository.deleteById("Alimentação");
        em.flush();

        assertThat(repository.findById("Alimentação")).isEmpty();
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("should check existence")
    void shouldCheckExists() {
        assertThat(repository.existsById("Transporte")).isTrue();
        assertThat(repository.existsById("Inexistente")).isFalse();
    }

    @Test
    @DisplayName("should save all and persist")
    void shouldSaveAll() {
        repository.saveAll(List.of(new Category("Moradia"), new Category("Saúde")));
        em.flush();

        assertThat(repository.count()).isEqualTo(5);
        assertThat(repository.findById("Moradia")).isPresent();
        assertThat(repository.findById("Saúde")).isPresent();
    }

    @Test
    @DisplayName("should return empty list when no categories")
    void shouldReturnEmpty_whenNoData() {
        repository.deleteAll();
        em.flush();

        assertThat(repository.findAll()).isEmpty();
    }
}
