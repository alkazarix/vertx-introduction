package com.talanlabs.sample01.http;

import black.door.hate.HalRepresentation;
import black.door.hate.HalResource;
import com.talanlabs.sample01.core.QuestionDTO;

import java.net.URI;

public class QuestionResult implements HalResource {

    private  int id;
    private  String author;
    private  String title;
    private String content;


    public static QuestionResult fromDTO(QuestionDTO dto) {
        return new QuestionResult(
                dto.getId(),
                dto.getAuthor(),
                dto.getTitle(),
                dto.getContent()
        );
    }

    public QuestionResult(int id, String author, String title, String content) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.content = content;

    }

    @Override
    public URI location() {
        return URI.create("/questions/" + id);
    }


    @Override
    public HalRepresentation.HalRepresentationBuilder representationBuilder() {
        return HalRepresentation.builder()
                .addProperty("author", author)
                .addProperty("title", title)
                .addProperty("content", content)
                .addLink("self", this);
    }
}
