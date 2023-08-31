package com.example.instructionformatter.service;

import com.example.instructionformatter.model.InstructionDto;
import com.example.instructionformatter.repository.JdbcNativeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final JdbcNativeRepository jdbcNativeRepository;
    public static final String UA_PATH = "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\instructions\\ua\\";
    public static final String RU_PATH = "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\instructions\\ru\\";

    private static final List<String> HEADERS_UA = List.of("Склад лікарського засобу", "Склад", "Лікарська форма", "Фармакотерапевтична група", "Фармакологічні властивості",
            "Показання для застосування", "Показання", "Протипоказання", "Взаємодія з іншими лікарськими засобами та інші види взаємодій", "Особливості застосування", "Особливості щодо застосування",
            "Застосування у період вагітності або годування груддю", "Здатність впливати на швидкість реакції при керуванні автотранспортом або іншими механізмами",
            "Спосіб застосування та дози", "Передозування", "Побічні ефекти", "Побічні реакції", "Особливі заходи безпеки", "Належні заходи безпеки при застосуванні",
            "Особливі застереження", "Термін придатності", "Умови зберігання", "Упаковка", "Категорія відпуску", "Виробник", "Назва і місцезнаходження виробника");

    private static final List<String> HEADERS_RU = List.of("Состав лекарственного средства", "Состав", "Лекарственная форма", "Фармакотерапевтическая группа",
            "Фармакологические свойства", "Показания к применению", "Показания", "Противопоказания", "Взаимодействие с другими лекарственными средствами и другие виды взаимодействий",
            "Особенности применения", "Особенности по применению", "Применение в период беременности или кормления грудью",
            "Способность влиять на скорость реакции при управлении автотранспортом или другими механизмами", "Способ применения и дозы", "Передозировка",
            "Побочные эффекты", "Побочные реакции", "Особые меры безопасности", "Надлежащие меры безопасности при применении", "Особые предостережения",
            "Срок годности", "Условия хранения", "Упаковка", "Категория отпуска", "Производитель", "Название и местонахождение производителя");

    public void findCorruptedInstructions() {
        List<InstructionDto> dtoList = jdbcNativeRepository.findInstructions();
        System.out.println("All found");
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        executorService.execute(() -> {
            for (InstructionDto instructionDto : dtoList) {
                Document ua = null;
                try {
                    ua = changeTags(instructionDto.getLinkUa());
                } catch (IOException ignored) {}
                System.out.println("ua tags changed");
                Document ru = null;
                try {
                    ru = changeTags(instructionDto.getLinkRu());
                } catch (IOException ignored) {}
                System.out.println("ru tags changed");
                save(instructionDto, ua, ru);
            }
        });
        executorService.shutdown();

    }

    public void updateAndSaveDocuments() {
        List<InstructionDto> dtoList = jdbcNativeRepository.findInstructions();
        System.out.println("All found");
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        executorService.execute(() -> {
            for (InstructionDto instructionDto : dtoList) {
                Document ua = null;
                try {
                    ua = updateDocument(instructionDto.getLinkUa(), "ua");
                } catch (IOException ignored) {}
                System.out.println("ua document updated");
                Document ru = null;
                try {
                    ru = updateDocument(instructionDto.getLinkRu(), "ru");
                } catch (IOException ignored) {}
                System.out.println("ru document updated");

                save(instructionDto, ua, ru);
            }
        });
        executorService.shutdown();
    }

    private void save(InstructionDto instructionDto, Document ua, Document ru) {
        if (ua != null) {
            try {
                saveInstruction(ua, UA_PATH + instructionDto.getLinkUa().substring(instructionDto.getLinkUa().lastIndexOf("/") + 1));
            } catch (IOException ignored) {}
        }
        if (ru != null) {
            try {
                saveInstruction(ru, RU_PATH + instructionDto.getLinkRu().substring(instructionDto.getLinkRu().lastIndexOf("/") + 1));
            } catch (IOException ignored) {}
        }
    }


    private Document updateDocument(String link, String lang) throws IOException {
        if (link == null) return null;
        Document document = Jsoup.connect(link).ignoreContentType(true).get();
        findHeadersAndUpdate(document, lang.equals("ua") ? HEADERS_UA : HEADERS_RU);
        return document;
    }

    private void findHeadersAndUpdate(Document document, List<String> headers) {
        for (String header : headers) {
            Elements elements = document.getElementsContainingOwnText(header);
            elements.forEach(element -> {
                if (element.text().length() - header.length() < 3) {
                    element.tagName("h2");
                    removePunctuation(element);
                } else {
                    element.tagName("p");
                }
            });
        }
    }

    public Document changeTags(String link) throws IOException {
        if (link == null) return null;
        Document document = Jsoup.connect(link).ignoreContentType(true).get();
        boolean isCorrupted = isCorrupted(document);
        if (isCorrupted) {
            return document;
        } else {
            return null;
        }
    }

    private void removePunctuation(Element element) {
        String content = element.text();
        if (!Character.isLetterOrDigit(content.charAt(content.length() - 1))) {
            element.text(content.substring(0, content.length() - 1));
        }
    }

    public boolean isCorrupted(Document document) {
        Elements elements = document.getElementsByTag("h2");
        AtomicInteger count = new AtomicInteger();
        elements.forEach(element -> {
            if (element.text().length() > 100 || element.html().contains("<br>")) {
                count.getAndIncrement();
                element.tagName("p");
            }
        });
        return count.get() > 0;
    }

    public void saveInstruction(Document doc, String path) throws IOException {
        File f = new File(path);
        FileUtils.writeStringToFile(f, doc.outerHtml(), StandardCharsets.UTF_8);
        System.out.println(path);
    }
}
