package com.example.instructionformatter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructionDto {
    private String name;
    private Document document;
}
