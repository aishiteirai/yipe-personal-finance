package com.yipe.finance.repository;

import com.yipe.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByTipo(String tipo);
}
