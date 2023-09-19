package com.example.instructionformatter.service;

import com.example.instructionformatter.model.InstructionDto;
import com.example.instructionformatter.repository.JdbcNativeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final JdbcNativeRepository jdbcNativeRepository;
    private final CsvService csvService;
    public static final String UA_PATH = "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\instructions\\ua\\";
    public static final String RU_PATH = "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\instructions\\ru\\";

    private static final List<String> HEADERS_UA = new ArrayList<>(List.of("Склад лікарського засобу", "Склад", "Лікарська форма", "Фармакотерапевтична група", "Фармакологічні властивості",
            "Показання для застосування", "Показання", "Протипоказання", "Взаємодія з іншими лікарськими засобами та інші види взаємодій", "Особливості застосування", "Особливості щодо застосування",
            "Застосування у період вагітності або годування груддю", "Здатність впливати на швидкість реакції при керуванні автотранспортом або іншими механізмами",
            "Спосіб застосування та дози", "Передозування", "Побічні ефекти", "Побічні реакції", "Особливі заходи безпеки", "Належні заходи безпеки при застосуванні",
            "Особливі застереження", "Термін придатності", "Умови зберігання", "Упаковка", "Категорія відпуску", "Виробник", "Назва і місцезнаходження виробника"));

    private static final List<String> HEADERS_RU = new ArrayList<>(List.of("Состав лекарственного средства", "Состав", "Лекарственная форма", "Фармакотерапевтическая группа",
            "Фармакологические свойства", "Показания к применению", "Показания", "Противопоказания", "Взаимодействие с другими лекарственными средствами и другие виды взаимодействий",
            "Особенности применения", "Особенности по применению", "Применение в период беременности или кормления грудью",
            "Способность влиять на скорость реакции при управлении автотранспортом или другими механизмами", "Способ применения и дозы", "Передозировка",
            "Побочные эффекты", "Побочные реакции", "Особые меры безопасности", "Надлежащие меры безопасности при применении", "Особые предостережения",
            "Срок годности", "Условия хранения", "Упаковка", "Категория отпуска", "Производитель", "Название и местонахождение производителя"));


    public Document unwrapH2(String link) throws IOException {
        Document document = getDocument(link);
        Elements headers = document.getElementsByTag("h2");
        headers.forEach(Node::unwrap);
        Element root = document.select("div").first();
        List<TextNode> textNodes = root.textNodes();
        for (TextNode textNode : textNodes) {
            // Находим предыдущий элемент с тегом
            Element previousElement = findPreviousElementWithTag(textNode);

            // Если такой элемент существует, добавляем текстовый узел к нему
            if (previousElement != null) {
                previousElement.append(" ").append(textNode.text());
                // Удаляем текстовый узел после добавления его содержимого
                textNode.remove();
            }
        }
        return document;
    }

    private Document getDocument(String link) throws IOException {
        Document document = Jsoup.connect(link).ignoreContentType(true).get();
        String html = document.outerHtml();
        html = html.replace("<p>&nbsp;</p>", "").replace("<p></p>", "");
        return Jsoup.parse(html);
    }

    private static Element findPreviousElementWithTag(TextNode textNode) {
        Node sibling = textNode.previousSibling();
        while (sibling != null) {
            if (sibling instanceof Element element) {
                return element;
            }
            sibling = sibling.previousSibling();
        }
        return null;
    }

    public Document refactorDocument(String link, String lang) throws IOException {
        Document document = unwrapH2(link);
        List<String> copiedHeaders = new ArrayList<>(lang.equals("ua") ? HEADERS_UA : HEADERS_RU);
        List<String> usedHeaders = new ArrayList<>();
        for (String header : copiedHeaders) {
            boolean flag = endSentenceUpdate(document, header);
            if (flag) usedHeaders.add(header);
        }
        copiedHeaders.removeAll(usedHeaders);
        for (String header : copiedHeaders) {
           boolean flag = fullSentenceUpdate(document, header);
           if (flag) usedHeaders.add(header);
        }
        copiedHeaders.removeAll(usedHeaders);
        for (String header : copiedHeaders) {
            boolean flag = withoutDotUpdate(document, header);
            if (flag) usedHeaders.add(header);
        }
        copiedHeaders.removeAll(usedHeaders);
        return document;
    }

    private boolean fullSentenceUpdate(Document document, String header) {
        Elements elements = document.select(String.format("p:contains(%s.)", header));
        elements.addAll(document.select(String.format("p:contains(%s:)", header)));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        elements.forEach(element -> {
            if (!element.tagName().equals("h2")) {
                String pText = element.text();
                // Создаем новый элемент <h2> с текстом "Протипоказання"
                Element h2Element = new Element("h2").text(header);
                element.before(h2Element);
                element.text(pText.replace(header + ".", ""));
                atomicBoolean.set(true);
            }
        });
        return atomicBoolean.get();
    }

    private boolean endSentenceUpdate(Document document, String header) {
        Elements elements = document.select(String.format("p:contains(. %s)", header));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        elements.forEach(element -> {
            if (!element.tagName().equals("h2")) {
                // Извлекаем текст из элемента <p>
                String pText = element.text();
                // Создаем новый элемент <h2> с текстом "Протипоказання"
                Element h2Element = new Element("h2").text(header);
                // Вставляем новый элемент перед элементом <p>
                element.after(h2Element);

                String textAfter = pText.substring(pText.indexOf(". " + header) + header.length() + 2);
                Element after = new Element("p").text(textAfter);
                h2Element.after(after);
                // Удаляем текст "Протипоказання" из элемента <p>
                element.text(pText.replace(". " + header + textAfter, ". ").trim());
                atomicBoolean.set(true);
            }
        });
        return atomicBoolean.get();

    }

    private boolean withoutDotUpdate(Document document, String header) {
        Elements elements = document.select(String.format("p:contains(%s)", header));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        elements.forEach(element -> {
            String text = element.text();
            if (!element.tagName().equals("h2") && (text.indexOf(header) + header.length() == text.length())) {

                // Создаем новый элемент <h2> с текстом "Протипоказання"
                Element h2Element = new Element("h2").text(header);
                // Вставляем новый элемент перед элементом <p>
                element.after(h2Element);
                element.text(text.replace( header, ""));
                atomicBoolean.set(true);
            }
        });
        return atomicBoolean.get();
    }


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


    public void findAllHeaders() throws Exception {
        List<InstructionDto> dtoList = jdbcNativeRepository.findInstructions();
        System.out.println("All found");
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        Set<String> resultUa = new HashSet<>();
        Set<String> resultRu = new HashSet<>();
        executorService.execute(() -> {
            for (InstructionDto instructionDto : dtoList) {
                try {
                    resultUa.addAll(findHeadersInDocument(instructionDto.getLinkUa()));
                    System.out.println(instructionDto.getLinkUa());
                } catch (Exception ignored) {
                }
                try {
                    resultRu.addAll(findHeadersInDocument(instructionDto.getLinkRu()));
                    System.out.println(instructionDto.getLinkRu());
                } catch (Exception ignored) {
                }
            }
        });

        executorService.shutdown();

        try {
            // Wait for the tasks to complete or a timeout of 5 seconds
            if (executorService.awaitTermination(1, TimeUnit.HOURS)) {
                List<String[]> uaLines = resultUa.stream().map(string -> new String[]{string}).toList();

                List<String[]> ruLines = resultRu.stream().map(string -> new String[]{string}).toList();
                csvService.writeAllLines(uaLines, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\ua_headers.csv");

                csvService.writeAllLines(ruLines, "C:\\Users\\Mark\\Desktop\\InstructionFormatter\\ru_headers.csv");
            } else {
                // Handle the case where the timeout was reached
                System.out.println("Timeout reached while waiting for tasks to complete.");
            }
        } catch (InterruptedException e) {
            // Handle InterruptedException
            System.err.println("ExecutorService was interrupted while waiting for termination.");
        }
    }

    private Set<String> findHeadersInDocument(String link) throws IOException {
        Document document = Jsoup.connect(link).ignoreContentType(true).get();
        Set<String> result = new HashSet<>();
        Elements elements = document.getElementsByTag("h2");
        elements.forEach(element -> result.add(element.text()));
        return result;
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
                saveDocument(ua, UA_PATH + instructionDto.getLinkUa().substring(instructionDto.getLinkUa().lastIndexOf("/") + 1));
            } catch (IOException ignored) {}
        }
        if (ru != null) {
            try {
                saveDocument(ru, RU_PATH + instructionDto.getLinkRu().substring(instructionDto.getLinkRu().lastIndexOf("/") + 1));
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

    public void saveDocument(Document doc, String path) throws IOException {
        File f = new File(path);
        FileUtils.writeStringToFile(f, doc.outerHtml(), StandardCharsets.UTF_8);
        System.out.println(path);
    }
}
