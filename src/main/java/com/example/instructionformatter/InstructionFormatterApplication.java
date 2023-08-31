package com.example.instructionformatter;

import com.example.instructionformatter.model.InstructionDto;
import com.example.instructionformatter.repository.JdbcNativeRepository;
import com.example.instructionformatter.service.DocumentService;
import com.example.instructionformatter.service.GoogleBucketService;
import lombok.RequiredArgsConstructor;
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

//        Document document = documentService.changeTags("https://storage.googleapis.com/static-storage/products/instructions/v3/ua/26816-ua.html");
//        List<InstructionDto> instructionDtoList = jdbcNativeRepository.findInstructions();
//        System.out.println(instructionDtoList.stream().filter(dto -> dto.getProductId().equals("26816")).findFirst());
//        System.out.println(document.html());

//        List<InstructionDto> instructionDtoList = jdbcNativeRepository.findInstructions();
//        List<String> ru = instructionDtoList.stream().map(InstructionDto::getLinkRu).toList();
//        List<String> ua = instructionDtoList.stream().map(InstructionDto::getLinkUa).toList();
//        ExecutorService ruService = Executors.newFixedThreadPool(16);
//        ruService.execute(() -> ru.forEach(inst -> {
//            try {
//                Document document = Jsoup.connect(inst).ignoreContentType(true).get();
//                if (documentService.isCorrupted(document)) {
//                    documentService.saveInstruction(document, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\old\\ru\\" + inst.substring(inst.lastIndexOf("/") + 1));
//                }
//            } catch (Exception ignored) {}
//        }));
//        ruService.shutdown();
//        ExecutorService uaService = Executors.newFixedThreadPool(16);
//        uaService.execute(() -> {
//            ua.forEach(inst -> {
//                try {
//                    Document document = Jsoup.connect(inst).ignoreContentType(true).get();
//                    if (documentService.isCorrupted(document)) {
//                        documentService.saveInstruction(document, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\old\\ua\\" + inst.substring(inst.lastIndexOf("/") + 1));
//                    }
//                } catch (Exception ignored) {}
//            });
//        });
//        uaService.shutdown();

//        String link = "https://storage.googleapis.com/static-storage/products/instructions/v3/ua/26816-ua.html";
       // documentService.saveInstruction(document, DocumentService.UA_PATH + link.substring(link.lastIndexOf("/") + 1));

        documentService.updateAndSaveDocuments();

        //bucketService.upload();
    }
}
