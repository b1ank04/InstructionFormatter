package com.example.instructionformatter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructionDto {
    private String productId;
    private String linkRu;
    private String linkUa;
    private Document documentRu;
    private Document documentUa;
}
