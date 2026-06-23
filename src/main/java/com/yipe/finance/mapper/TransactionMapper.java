package com.yipe.finance.mapper;

import com.yipe.finance.dto.TransactionDTO;
import com.yipe.finance.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcela", ignore = true)
    @Mapping(target = "valor", ignore = true)
    Transaction toEntity(TransactionDTO dto);

    TransactionDTO toDto(Transaction entity);
}
