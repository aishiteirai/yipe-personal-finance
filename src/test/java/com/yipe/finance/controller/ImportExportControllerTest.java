package com.yipe.finance.controller;

import com.yipe.finance.repository.TransactionRepository;
import com.yipe.finance.service.ImportExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportExportController.class)
@WithMockUser
class ImportExportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionRepository transactionRepository;

    @MockitoBean
    ImportExportService importExportService;

    @Test
    @DisplayName("GET /import-export should return import-export view")
    void page_shouldReturnView() throws Exception {
        when(transactionRepository.count()).thenReturn(90L);

        mockMvc.perform(get("/import-export"))
                .andExpect(status().isOk())
                .andExpect(view().name("import-export"))
                .andExpect(model().attributeExists("activePage"))
                .andExpect(model().attribute("totalTransactions", 90L))
                .andExpect(model().attribute("hasPreview", false));
    }

    @Test
    @DisplayName("GET /import-export/export should return CSV file")
    void export_shouldReturnCsv() throws Exception {
        String csv = "data,descricao,valor,tipo,categoria,conta,parcela\n";
        when(importExportService.exportCsv()).thenReturn(csv);

        mockMvc.perform(get("/import-export/export"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("backup_")))
                .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }

    @Test
    @DisplayName("POST /import-export/import/preview with valid file should show preview")
    void preview_withValidFile_shouldShowPreview() throws Exception {
        String csvContent = "data,descricao,valor,tipo,categoria,conta,parcela\n"
                + "2026-01-01,Test,100.00,DEBIT,Food,NuConta,Única\n"
                + "2026-01-02,Test2,200.00,CREDIT,Transporte,NuConta,Única";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes());

        var parsed = List.of(
                new String[]{"2026-01-01", "Test", "100.00", "DEBIT", "Food", "NuConta", "Única"},
                new String[]{"2026-01-02", "Test2", "200.00", "CREDIT", "Transporte", "NuConta", "Única"}
        );
        when(importExportService.parseRows(any())).thenReturn(parsed);
        when(importExportService.parsePreview(parsed)).thenReturn(parsed);
        when(transactionRepository.count()).thenReturn(90L);

        mockMvc.perform(multipart("/import-export/import/preview")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("import-export"))
                .andExpect(model().attribute("hasPreview", true))
                .andExpect(model().attributeExists("previewRows"))
                .andExpect(model().attribute("totalImport", 2));
    }

    @Test
    @DisplayName("POST /import-export/import/confirm with session data should import")
    void confirm_withSessionData_shouldImport() throws Exception {
        List<String[]> rows = Collections.singletonList(
                new String[]{"2026-01-01", "Test", "100.00", "DEBIT", "Food", "NuConta", "Única"}
        );
        when(importExportService.confirmImport(rows))
                .thenReturn(new ImportExportService.ImportResult(1, 0));
        when(transactionRepository.count()).thenReturn(91L);

        mockMvc.perform(post("/import-export/import/confirm")
                        .sessionAttr("importRows", rows)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("import-export"))
                .andExpect(model().attribute("hasPreview", false))
                .andExpect(model().attributeExists("success"));
    }
}
