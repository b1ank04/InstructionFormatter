package com.example.instructionformatter;

import com.example.instructionformatter.model.InstructionDto;
import com.example.instructionformatter.repository.JdbcNativeRepository;
import com.example.instructionformatter.service.GoogleBucketService;
import com.example.instructionformatter.service.v2.V2DocumentService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


@SpringBootApplication
@RequiredArgsConstructor
public class InstructionFormatterApplication implements ApplicationRunner {

    private final JdbcNativeRepository jdbcNativeRepository;
    private final V2DocumentService v2DocumentService;
    //private final DocumentService documentService;
    private final GoogleBucketService bucketService;

    public static void main(String[] args) {
        SpringApplication.run(InstructionFormatterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        AtomicReference<List<InstructionDto>> instructionDtoList = new AtomicReference<>();
        executorService.execute(() -> {
            try {
                instructionDtoList.set(v2DocumentService.parseDocuments("C:\\Users\\Mark\\Desktop\\inst_refs"));
            } catch (IOException e) {}
        });
        executorService.shutdown();
        if (executorService.awaitTermination(1, TimeUnit.HOURS)) {
            Document document = v2DocumentService.proceedDocument("https://storage.googleapis.com/static-storage/products/instructions/v2/100.html");
        }

       // documentService.saveDocument(document, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\100.html");
    }
}
