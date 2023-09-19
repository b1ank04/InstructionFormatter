package com.example.instructionformatter.service;

import com.opencsv.CSVWriter;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.util.List;
import java.util.Set;

@Data
@Component
public class CsvService {

    public void writeAllLines(List<String[]> lines, String path) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeAll(lines);
        }
    }
}
