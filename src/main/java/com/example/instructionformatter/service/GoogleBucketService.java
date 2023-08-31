package com.example.instructionformatter.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GoogleBucketService {

    public static final String UA_PATH = "";
    public static final String RU_PATH = "";

    public void upload() {
        File ruDirectory = new File("C:\\Users\\Mark\\Desktop\\InstructionFormatter\\instructions\\ru");
        File uaDirectory = new File("C:\\Users\\Mark\\Desktop\\InstructionFormatter\\instructions\\ua");
        List<File> ruFiles = Arrays.stream(Objects.requireNonNull(ruDirectory.listFiles())).toList();
        List<File> uaFiles = Arrays.stream(Objects.requireNonNull(uaDirectory.listFiles())).toList();
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        executorService.execute(() -> {
            ruFiles.forEach(file -> {
                try {
                    uploadFile(file.getAbsolutePath(), "products/instructions/v3_test/ru");
                } catch (Exception ignored) {
                    System.out.println(ignored);
                }
            });
            uaFiles.forEach(file -> {
                try {
                    uploadFile(file.getAbsolutePath(), "products/instructions/v3_test");
                } catch (Exception ignored) {
                    System.out.println(ignored);
                }
            });
        });
        executorService.shutdown();
    }

    private void uploadFile(String targetPath, String filePath) throws IOException {
        String projectId = "ancmiddleware";
        String bucketName = "static-storage";

        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\Mark\\Desktop\\InstructionFormatter\\src\\main\\resources\\credentials.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(targetPath)));
        System.out.println(filePath + " -uploaded");
    }
}
