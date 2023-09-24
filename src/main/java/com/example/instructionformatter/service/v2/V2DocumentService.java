package com.example.instructionformatter.service.v2;

import com.example.instructionformatter.model.InstructionDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.instructionformatter.service.DocumentService.HEADERS_UA;

@Component
public class V2DocumentService {

    public List<InstructionDto> parseDocuments(String path) throws IOException {
        File root = new File(path);
        List<File> documents = List.of(Objects.requireNonNull(root.listFiles()));
        List<InstructionDto> instructionDtoList = new ArrayList<>();
        for (File document : documents) {
            InstructionDto instructionDto = new InstructionDto();
            instructionDto.setDocument(Jsoup.parse(document));
            instructionDto.setName(document.getName());
            instructionDtoList.add(instructionDto);
        }
        return instructionDtoList;
    }

    public Document proceedDocument(String link) throws IOException {
        Document document = Jsoup.connect(link).ignoreContentType(true).get();
        document.getAllElements().forEach(Element::clearAttributes);
        document = clearGarbage(document);
        document.getElementsByTag("h1").tagName("h2");
        document.getElementsByTag("h2").tagName("p");
        selectHeaders(document);
        Element meta = new Element("meta").attr("charset", "utf-8");
        Objects.requireNonNull(document.getElementsByTag("head").first()).appendChild(meta);
        return document;
    }



    public void selectHeaders(Document document) {
        addHeaders(HEADERS_UA);
        List<String> headersCopy = HEADERS_UA;
        List<String> usedHeaders = new ArrayList<>();
        for (String header : headersCopy) {
            boolean flag = fullSentenceUpdate(document, header);
            if (flag) usedHeaders.add(header);
        }
        headersCopy.removeAll(usedHeaders);
        for (String header : headersCopy) {
            boolean flag = withoutPunctuationUpdate(document, header);
            if (flag) usedHeaders.add(header);
        }
        headersCopy.removeAll(usedHeaders);
    }

    private Document clearGarbage(Document document) {
        clearDivs(document);
        String html = document.outerHtml().replace("<span>", "")
                .replace("</span>", "")
                .replace("<o:p>", "")
                .replace("</o:p>", "")
                .replace("&nbsp;", "")
                .replace("<st1:personname>", "")
                .replace("</st1:personname>", "")
                .replace("<b>", "")
                .replace("</b>", "")
                .replace("<i>", "")
                .replace("</i>", "");
        document = Jsoup.parse(html);
        Elements attributeLess = document.getElementsByTag("li");
        attributeLess.forEach(element -> {
            try {
                if (!Objects.requireNonNull(element.parent()).tagName().equals("ul")) {
                    element.unwrap();
                }
            } catch (NullPointerException ignored) {
                element.unwrap();
            }
        });
        Elements emptyElements = document.select(":matches(^[\\s\\n]*$)");
        emptyElements.forEach(Element::remove);

        String[] allowedTags = {"p", "div", "table", "tr", "td", "ul", "ol", "h1", "h2", "h3", "h4", "h5", "h6"};

        String pattern = String.format("</?(?!%s)\\w+.*?>", String.join("|", allowedTags));
        String cleanedHtml = document.outerHtml().replaceAll(pattern, "");

        return Jsoup.parse(cleanedHtml);
    }

    private void clearDivs(Document document) {
        Element root = getRoot(document);
        Element div = new Element("div");
        root.before(div);
        div.appendChild(root);

        div.getElementsByTag("div").unwrap();
    }

    private Element getRoot(Document document) {
        if (!document.getElementsByTag("div").isEmpty()) {
            return document.select("div").first();
        } else if (!document.getElementsByTag("body").isEmpty()) {
            return document.select("body").first();
        } else {
            return document;
        }
    }

    private boolean fullSentenceUpdate(Document document, String header) {
        Elements elements = document.select(String.format("p:contains(%s.)", header));
        elements.addAll(document.select(String.format("p:contains(%s:)", header)));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        elements.forEach(element -> {
            String pText = element.text();
            if (!element.tagName().equals("h2") && pText.contains(header)) {
                Element h2Element = new Element("h2").text(header);
                element.before(h2Element);
                element.text(pText.replace(header + ".", "")
                        .replace(header + ". ", "")
                        .replace(header + ":", "")
                        .replace(header + ": ", ""));
                atomicBoolean.set(true);
            }
        });
        return atomicBoolean.get();
    }

    private boolean withoutPunctuationUpdate(Document document, String header) {
        Elements elements = document.getElementsContainingOwnText(header);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (Element element : elements) {
            if (element.text().equals(header) && (element.tagName().equals("p") || element.tagName().equals("h1")
                || element.tagName().equals("h3") || element.tagName().equals("h4")
                || element.tagName().equals("h5") || element.tagName().equals("h6"))) {
                element.tagName("h2");
                atomicBoolean.set(true);
            }
        }
        return atomicBoolean.get();
    }

    private void addHeaders(List<String> headers) {
        String newHeaders = "Активні компоненти\n" +
                "Призначення\n" +
                "Діючі властивості компонентів\n" +
                "Особливість застосування\n" +
                "Застосування\n" +
                "Властивості\n" +
                "Попередження та спеціальні застереження\n" +
                "Умови та термін зберігання\n" +
                "Рекомендації по вживанню\n" +
                "Рекомендації щодо вживання\n" +
                "Термін зберігання\n" +
                "Рекомендації до споживання\n" +
                "Допоміжні речовини\n" +
                "Взаємодія з іншими засобами та інші види взаємодії\n" +
                "Попередження\n" +
                "Форма випуску\n" +
                "Умови та строки зберігання\n" +
                "Рекомендації щодо призначення\n" +
                "Умови зберiгання\n" +
                "Рекомендації щодо застосування\n" +
                "Вплив на здатність керування автотранспортом\n" +
                "Призначення засобу\n" +
                "Рекомендації по застосуванню\n" +
                "Рекомендації щодо використання\n" +
                "Рекомендація до споживання\n" +
                "Спосiб застосування\n" +
                "Взаємодія з іншими лікарськими засобами та інші види взаємодії\n" +
                "Побічна дія\n" +
                "Опис діючих речовин\n" +
                "Калорійність (енергетична цінність)\n" +
                "Алергічні реакції\n" +
                "Активні речовини\n" +
                "Рекомендації щодо споживання\n" +
                "Застереження\n" +
                "Фармакологiчнi властивості\n" +
                "Спосіб застосування та доза\n" +
                "Взаємодія з іншими лікарськими засобами\n" +
                "Властивості основних речовин\n" +
                "Показаня до застосування\n" +
                "Властивості активних речовин\n" +
                "Призначення для використання\n" +
                "Спосіб застосування і дозування\n" +
                "Активні інгредієнти\n" +
                "Властивості активних компонентів\n" +
                "Рекомендації для застосування:\n" +
                "Спосіб застосування і дози\n" +
                "Попередження і застереження\n" +
                "Рекомендації до вживання\n" +
                "Властивості основної речовини\n" +
                "Активні компоненти добавки\n" +
                "Активна речовина\n" +
                "Дозування";
        List<String> result = List.of(newHeaders.split("\n"));
        headers.addAll(result);
    }
    
}
