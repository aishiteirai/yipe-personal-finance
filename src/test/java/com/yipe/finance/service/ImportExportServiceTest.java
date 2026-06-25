package com.yipe.finance.service;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportExportServiceTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    ImportExportService service;

    @Captor
    ArgumentCaptor<List<Transaction>> listCaptor;

    @Test
    @DisplayName("should generate CSV with header and all transactions")
    void exportCsv_shouldGenerateCsv() {
        Transaction t = new Transaction();
        t.setData(LocalDate.of(2026, 6, 15));
        t.setDescricao("Teste");
        t.setValor(BigDecimal.valueOf(100.50));
        t.setTipo(TransactionType.DEBIT);
        t.setCategoria("Alimentação");
        t.setConta("Nubank");
        t.setParcela("Única");
        when(repository.findAll()).thenReturn(List.of(t));

        String csv = service.exportCsv();

        assertThat(csv).startsWith("data,descricao,valor,tipo,categoria,conta,parcela\n");
        assertThat(csv).contains("2026-06-15,Teste,100.5,DEBIT,Alimentação,Nubank,Única");
    }

    @Test
    @DisplayName("should handle null categoria and parcela in CSV export")
    void exportCsv_shouldHandleNullFields() {
        Transaction t = new Transaction();
        t.setData(LocalDate.of(2026, 6, 15));
        t.setDescricao("Teste");
        t.setValor(BigDecimal.valueOf(50));
        t.setTipo(TransactionType.INCOME);
        t.setCategoria(null);
        t.setConta("Itaú");
        t.setParcela(null);
        when(repository.findAll()).thenReturn(List.of(t));

        String csv = service.exportCsv();

        assertThat(csv).contains("2026-06-15,Teste,50,INCOME,,Itaú,Única");
    }

    @Test
    @DisplayName("should return only header when no transactions in CSV export")
    void exportCsv_shouldReturnOnlyHeader_whenEmpty() {
        when(repository.findAll()).thenReturn(List.of());
        String csv = service.exportCsv();
        assertThat(csv).isEqualTo("data,descricao,valor,tipo,categoria,conta,parcela\n");
    }

    @Test
    @DisplayName("should escape fields with commas in CSV export")
    void exportCsv_shouldEscapeCommas() {
        Transaction t = new Transaction();
        t.setData(LocalDate.of(2026, 6, 15));
        t.setDescricao("Compra, mercado");
        t.setValor(BigDecimal.valueOf(100));
        t.setTipo(TransactionType.DEBIT);
        t.setCategoria("Alimentação");
        t.setConta("Nubank");
        t.setParcela("Única");
        when(repository.findAll()).thenReturn(List.of(t));

        String csv = service.exportCsv();

        assertThat(csv).contains("\"Compra, mercado\"");
    }

    @Test
    @DisplayName("should parse CSV rows from buffered reader")
    void parseRows_shouldParseCsv() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(
                "data,descricao,valor,tipo,categoria,conta,parcela\n" +
                "2026-06-15,Teste,100.50,DEBIT,Alimentação,Nubank,Única\n" +
                "2026-06-16,Outro,200,CREDIT,,Itaú,1/3\n"
        ));

        List<String[]> rows = service.parseRows(reader);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("2026-06-15");
        assertThat(rows.get(0)[1]).isEqualTo("Teste");
        assertThat(rows.get(0)[2]).isEqualTo("100.50");
        assertThat(rows.get(1)[3]).isEqualTo("CREDIT");
        assertThat(rows.get(1)[4]).isEmpty();
        assertThat(rows.get(1)[6]).isEqualTo("1/3");
    }

    @Test
    @DisplayName("should handle CSV with BOM character in header")
    void parseRows_shouldHandleBom() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(
                "\uFEFFdata,descricao,valor,tipo,categoria,conta,parcela\n" +
                "2026-06-15,Teste,100.50,DEBIT,Alimentação,Nubank,Única\n"
        ));

        List<String[]> rows = service.parseRows(reader);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)[1]).isEqualTo("Teste");
    }

    @Test
    @DisplayName("should skip blank lines in CSV")
    void parseRows_shouldSkipBlankLines() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(
                "data,descricao,valor,tipo,categoria,conta,parcela\n" +
                "\n" +
                "2026-06-15,Teste,100.50,DEBIT,Alimentação,Nubank,Única\n" +
                "\n"
        ));

        List<String[]> rows = service.parseRows(reader);
        assertThat(rows).hasSize(1);
    }

    @Test
    @DisplayName("should skip lines with fewer than 6 columns")
    void parseRows_shouldSkipMalformedLines() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(
                "data,descricao,valor,tipo,categoria,conta,parcela\n" +
                "too,few\n" +
                "2026-06-15,Teste,100.50,DEBIT,Alimentação,Nubank,Única\n"
        ));

        List<String[]> rows = service.parseRows(reader);
        assertThat(rows).hasSize(1);
    }

    @Test
    @DisplayName("should parse quoted fields containing commas")
    void parseRows_shouldHandleQuotedFields() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(
                "data,descricao,valor,tipo,categoria,conta,parcela\n" +
                "2026-06-15,\"Compra, mercado\",100.50,DEBIT,Alimentação,Nubank,Única\n"
        ));

        List<String[]> rows = service.parseRows(reader);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)[1]).isEqualTo("Compra, mercado");
    }

    @Test
    @DisplayName("should return empty list when reader has no content")
    void parseRows_shouldReturnEmpty_whenNoContent() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(""));
        List<String[]> rows = service.parseRows(reader);
        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("should return first 10 rows for preview")
    void parsePreview_shouldReturnFirst10Rows() {
        List<String[]> allRows = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            allRows.add(new String[]{"a", "b", "c", "d", "e", "f"});
        }

        List<String[]> preview = service.parsePreview(allRows);
        assertThat(preview).hasSize(10);
    }

    @Test
    @DisplayName("should return all rows when fewer than 10 for preview")
    void parsePreview_shouldReturnAll_whenFewerThan10() {
        List<String[]> rows = List.of(
                new String[]{"a", "b", "c", "d", "e", "f"},
                new String[]{"1", "2", "3", "4", "5", "6"}
        );

        List<String[]> preview = service.parsePreview(rows);
        assertThat(preview).hasSize(2);
    }

    @Test
    @DisplayName("should return empty preview for empty rows")
    void parsePreview_shouldReturnEmpty_whenEmpty() {
        assertThat(service.parsePreview(List.of())).isEmpty();
    }

    @Test
    @DisplayName("should import valid rows and return counts")
    void confirmImport_shouldImportValidRows() {
        List<String[]> rows = List.of(
                new String[]{"2026-06-15", "Teste", "100.50", "DEBIT", "Alimentação", "Nubank", "Única"},
                new String[]{"2026-06-16", "Outro", "200", "CREDIT", "", "Itaú", "1/3"}
        );

        ImportExportService.ImportResult result = service.confirmImport(rows);

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.errors()).isEqualTo(0);
        verify(repository).saveAll(listCaptor.capture());
        List<Transaction> batch = listCaptor.getValue();
        assertThat(batch).hasSize(2);
        assertThat(batch.get(0).getDescricao()).isEqualTo("Teste");
        assertThat(batch.get(0).getValor()).isEqualByComparingTo("100.50");
        assertThat(batch.get(1).getParcela()).isEqualTo("1/3");
    }

    @Test
    @DisplayName("should count errors for malformed rows during import")
    void confirmImport_shouldCountErrors() {
        List<String[]> rows = List.of(
                new String[]{"2026-06-15", "Teste", "100.50", "DEBIT", "Alimentação", "Nubank", "Única"},
                new String[]{"invalid-date", "Ruim", "abc", "INVALID_TYPE", "", "Conta", ""}
        );

        ImportExportService.ImportResult result = service.confirmImport(rows);

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.errors()).isEqualTo(1);
    }

    @Test
    @DisplayName("should handle empty row list in confirmImport")
    void confirmImport_shouldHandleEmptyRows() {
        ImportExportService.ImportResult result = service.confirmImport(List.of());
        assertThat(result.imported()).isEqualTo(0);
        assertThat(result.errors()).isEqualTo(0);
        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("should handle rows with varying column counts")
    void confirmImport_shouldHandleVaryingColumns() {
        List<String[]> rows = List.of(
                new String[]{"2026-06-15", "Teste", "100.50", "DEBIT", "Alimentação", "Nubank"},
                new String[]{"2026-06-16", "Teste2", "50", "DEBIT", "", "Itaú"}
        );

        ImportExportService.ImportResult result = service.confirmImport(rows);

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.errors()).isEqualTo(0);
    }
}
