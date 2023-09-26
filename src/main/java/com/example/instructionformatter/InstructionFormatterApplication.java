package com.example.instructionformatter;

import com.example.instructionformatter.model.InstructionDto;
import com.example.instructionformatter.repository.JdbcNativeRepository;
import com.example.instructionformatter.service.DocumentService;
import com.example.instructionformatter.service.GoogleBucketService;
import com.example.instructionformatter.service.v2.V2DocumentService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


@SpringBootApplication
@RequiredArgsConstructor
public class InstructionFormatterApplication implements ApplicationRunner {

    private final JdbcNativeRepository jdbcNativeRepository;
    private final V2DocumentService v2DocumentService;
    private final DocumentService documentService;
    private final GoogleBucketService bucketService;

    public static void main(String[] args) {
        SpringApplication.run(InstructionFormatterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(16);
        List<String> names = v2DocumentService.parseNames("C:\\Users\\Mark\\Desktop\\Work\\InstructionFormatter\\refs");
        List<InstructionDto> documents = v2DocumentService.parseDocuments("C:\\Users\\Mark\\Desktop\\Work\\InstructionFormatter\\refs", executorService1);
        executorService1.shutdown();
        if (executorService1.awaitTermination(1, TimeUnit.HOURS)) {
            List<InstructionDto> sorted = new ArrayList<>();
            for (String name : names) {
                sorted.add(documents.stream().filter(document -> document.getName().equals(name)).findFirst().orElse(null));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(16);
            executorService.execute(() -> sorted.forEach(v2DocumentService::proceedDocument));
            executorService.shutdown();
            if (executorService.awaitTermination(1, TimeUnit.HOURS)) {
                for (InstructionDto instructionDto : sorted) {
                    try {
                        documentService.saveDocument(instructionDto.getDocument(), "C:\\Users\\Mark\\Desktop\\Work\\InstructionFormatter\\v2top100\\" + instructionDto.getName());
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
