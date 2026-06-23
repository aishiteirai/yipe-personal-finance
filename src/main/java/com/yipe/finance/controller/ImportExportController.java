package com.yipe.finance.controller;

import com.yipe.finance.repository.TransactionRepository;
import com.yipe.finance.service.ImportExportService;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/import-export")
public class ImportExportController {

    private final TransactionRepository transactionRepository;
    private final ImportExportService importExportService;

    public ImportExportController(TransactionRepository transactionRepository,
                                  ImportExportService importExportService) {
        this.transactionRepository = transactionRepository;
        this.importExportService = importExportService;
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
        String csv = importExportService.exportCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
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
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            List<String[]> rows = importExportService.parseRows(reader);
            reader.close();

            if (rows.isEmpty()) {
                model.addAttribute("error", "Nenhuma linha válida encontrada.");
                return "import-export";
            }

            session.setAttribute("importRows", rows);
            model.addAttribute("previewRows", importExportService.parsePreview(rows));
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

        ImportExportService.ImportResult result = importExportService.confirmImport(rows);

        session.removeAttribute("importRows");

        model.addAttribute("activePage", "import-export");
        model.addAttribute("totalTransactions", transactionRepository.count());
        model.addAttribute("hasPreview", false);
        model.addAttribute("success", result.imported() + " transações importadas com sucesso!");
        if (result.errors() > 0) {
            model.addAttribute("error", result.errors() + " linhas com erro foram ignoradas.");
        }
        return "import-export";
    }
}
