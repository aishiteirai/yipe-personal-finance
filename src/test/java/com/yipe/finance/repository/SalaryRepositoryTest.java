package com.yipe.finance.repository;

import com.yipe.finance.entity.Salary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SalaryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    SalaryRepository repository;

    private Salary salary1;
    private Salary salary2;

    @BeforeEach
    void setUp() {
        salary1 = new Salary();
        salary1.setNome("Salário Mensal");
        salary1.setDia(5);
        salary1.setValor(BigDecimal.valueOf(5000));
        salary1.setConta("Itaú");
        salary2 = new Salary();
        salary2.setNome("Bônus Anual");
        salary2.setDia(20);
        salary2.setValor(BigDecimal.valueOf(2000));
        salary2.setConta("Nubank");
        em.persistAndFlush(salary1);
        em.persistAndFlush(salary2);
    }

    @Test
    @DisplayName("should save and find salary by id")
    void shouldSaveAndFindById() {
        Optional<Salary> result = repository.findById(salary1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("Salário Mensal");
        assertThat(result.get().getValor()).isEqualByComparingTo("5000");
        assertThat(result.get().getDia()).isEqualTo(5);
        assertThat(result.get().getConta()).isEqualTo("Itaú");
    }

    @Test
    @DisplayName("should return empty when salary not found")
    void shouldReturnEmpty_whenNotFound() {
        Optional<Salary> result = repository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find all salaries")
    void shouldFindAll() {
        List<Salary> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Salary::getNome)
                .contains("Salário Mensal", "Bônus Anual");
    }

    @Test
    @DisplayName("should count salaries")
    void shouldCount() {
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("should delete salary by id")
    void shouldDeleteById() {
        repository.deleteById(salary1.getId());
        em.flush();

        assertThat(repository.findById(salary1.getId())).isEmpty();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should delete all salaries")
    void shouldDeleteAll() {
        repository.deleteAll();
        em.flush();

        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("should update salary fields")
    void shouldUpdate() {
        Salary salary = repository.findById(salary1.getId()).orElseThrow();
        salary.setValor(BigDecimal.valueOf(5500));
        salary.setDia(10);
        em.flush();

        Salary updated = repository.findById(salary1.getId()).orElseThrow();
        assertThat(updated.getValor()).isEqualByComparingTo("5500");
        assertThat(updated.getDia()).isEqualTo(10);
    }

    @Test
    @DisplayName("should save multiple new salaries")
    void shouldSaveAll() {
        Salary freelance = new Salary();
        freelance.setNome("Freelance");
        freelance.setDia(15);
        freelance.setValor(BigDecimal.valueOf(1000));
        freelance.setConta("Itaú");
        Salary invest = new Salary();
        invest.setNome("Investimentos");
        invest.setDia(1);
        invest.setValor(BigDecimal.valueOf(300));
        invest.setConta("Nubank");
        repository.saveAll(List.of(freelance, invest));
        em.flush();

        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("should check existence")
    void shouldCheckExists() {
        assertThat(repository.existsById(salary1.getId())).isTrue();
        assertThat(repository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("should return empty list when no salaries")
    void shouldReturnEmpty_whenNoData() {
        repository.deleteAll();
        em.flush();

        assertThat(repository.findAll()).isEmpty();
    }
}
