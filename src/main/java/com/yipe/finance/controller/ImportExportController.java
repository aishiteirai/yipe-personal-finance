package com.yipe.finance.controller;

import com.yipe.finance.entity.Transaction;
import com.yipe.finance.entity.TransactionType;
import com.yipe.finance.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/import-export")
public class ImportExportController {

    private final TransactionRepository transactionRepository;

    public ImportExportController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activePage", "import-export");
        model.addAttribute("totalTransactions", transactionRepository.count());
        model.addAttribute("hasPreview", false);
        return "import-export";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        List<Transaction> all = transactionRepository.findAll();
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

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "backup_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/import/preview")
    public String preview(@RequestParam MultipartFile file, HttpSession session, Model model) {
        model.addAttribute("activePage", "import-export");
        model.addAttribute("totalTransactions", transactionRepository.count());

        if (file.isEmpty()) {
            model.addAttribute("error", "Arquivo vazio.");
            return "import-export";
        }

        try {
            List<String[]> rows = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String header = reader.readLine();
            if (header == null) {
                model.addAttribute("error", "CSV sem cabeçalho.");
                return "import-export";
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = parseCsvLine(line);
                if (cols.length >= 6) {
                    rows.add(cols);
                }
            }
            reader.close();

            if (rows.isEmpty()) {
                model.addAttribute("error", "Nenhuma linha válida encontrada.");
                return "import-export";
            }

            session.setAttribute("importRows", rows);
            model.addAttribute("previewRows", rows.stream().limit(10).collect(Collectors.toList()));
            model.addAttribute("totalImport", rows.size());
            model.addAttribute("hasPreview", true);

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao ler CSV: " + e.getMessage());
        }

        return "import-export";
    }

    @PostMapping("/import/confirm")
    public String confirm(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<String[]> rows = (List<String[]>) session.getAttribute("importRows");

        if (rows == null || rows.isEmpty()) {
            model.addAttribute("activePage", "import-export");
            model.addAttribute("totalTransactions", transactionRepository.count());
            model.addAttribute("error", "Nenhum dado para importar. Faça o upload primeiro.");
            return "import-export";
        }

        int imported = 0;
        int errors = 0;
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
                transactionRepository.save(t);
                imported++;
            } catch (Exception e) {
                errors++;
            }
        }

        session.removeAttribute("importRows");

        model.addAttribute("activePage", "import-export");
        model.addAttribute("totalTransactions", transactionRepository.count());
        model.addAttribute("hasPreview", false);
        model.addAttribute("success", imported + " transações importadas com sucesso!");
        if (errors > 0) {
            model.addAttribute("error", errors + " linhas com erro foram ignoradas.");
        }
        return "import-export";
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
}
