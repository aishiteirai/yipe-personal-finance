package com.yipe.finance.service;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportExportService {

    private final TransactionRepository repository;

    public ImportExportService(TransactionRepository repository) {
        this.repository = repository;
    }

    public String exportCsv() {
        List<Transaction> all = repository.findAll();
        StringBuilder csv = new StringBuilder("data,descricao,valor,tipo,categoria,conta,parcela\n");
        for (Transaction t : all) {
            csv.append(escapeCsv(t.getData().toString())).append(",");
            csv.append(escapeCsv(t.getDescricao())).append(",");
            csv.append(escapeCsv(t.getValor().toString())).append(",");
            csv.append(escapeCsv(t.getTipo().name())).append(",");
            csv.append(escapeCsv(t.getCategoria() != null ? t.getCategoria() : "")).append(",");
            csv.append(escapeCsv(t.getConta())).append(",");
            csv.append(escapeCsv(t.getParcela() != null ? t.getParcela() : "Única")).append("\n");
        }
        return csv.toString();
    }

    public List<String[]> parsePreview(List<String[]> rows) {
        return rows.stream().limit(10).toList();
    }

    @Transactional
    public ImportResult confirmImport(List<String[]> rows) {
        int imported = 0;
        int errors = 0;
        List<Transaction> batch = new ArrayList<>();

        for (String[] cols : rows) {
            try {
                Transaction t = new Transaction();
                t.setData(LocalDate.parse(cols[0]));
                t.setDescricao(cols[1]);
                t.setValor(new BigDecimal(cols[2]));
                t.setTipo(TransactionType.valueOf(cols[3]));
                t.setCategoria(cols.length > 4 && !cols[4].isBlank() ? cols[4] : null);
                t.setConta(cols[5]);
                t.setParcela(cols.length > 6 && !cols[6].isBlank() ? cols[6] : "Única");
                batch.add(t);
                imported++;
            } catch (Exception e) {
                errors++;
            }
        }

        if (!batch.isEmpty()) {
            repository.saveAll(batch);
        }

        return new ImportResult(imported, errors);
    }

    public List<String[]> parseRows(java.io.BufferedReader reader) throws Exception {
        List<String[]> rows = new ArrayList<>();
        String header = reader.readLine();
        if (header == null) return rows;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) continue;
            String[] cols = parseCsvLine(line);
            if (cols.length >= 6) {
                rows.add(cols);
            }
        }
        return rows;
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        cols.add(current.toString().trim());
        return cols.toArray(new String[0]);
    }

    public record ImportResult(int imported, int errors) {}
}
