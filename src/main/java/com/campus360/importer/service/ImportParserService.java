package com.campus360.importer.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ImportParserService {
    
    public List<Map<String, String>> parseCsv(String fileContent) {
        // In a real scenario, we would use something like OpenCSV or Commons CSV
        // For now, this is a placeholder stub matching the planned service
        return List.of();
    }
}
