package com.example.instructionformatter;

import com.example.instructionformatter.model.InstructionDto;
import com.example.instructionformatter.repository.JdbcNativeRepository;
import com.example.instructionformatter.service.DocumentService;
import com.example.instructionformatter.service.GoogleBucketService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@SpringBootApplication
@RequiredArgsConstructor
public class InstructionFormatterApplication implements ApplicationRunner {

    private final JdbcNativeRepository jdbcNativeRepository;
    private final DocumentService documentService;
    private final GoogleBucketService bucketService;

    public static void main(String[] args) {
        SpringApplication.run(InstructionFormatterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String link = "https://storage.googleapis.com/static-storage/products/instructions/v3_test/1042234.html";
        Document refactored = documentService.refactorDocument(link, "ua");
        Document unwrap = documentService.unwrapH2(link);
       // documentService.saveDocument(refactored, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\16.35.html");
        documentService.saveDocument(refactored, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\16.51.html");
    }
}
