package com.talanlabs.sample04.http;

import black.door.hate.HalRepresentation;
import black.door.hate.HalResource;
import com.talanlabs.sample04.core.models.QuestionDTO;

import java.net.URI;

public class QuestionResponse implements HalResource {

    private String id;
    private String author;
    private String title;
    private String content;


    public static QuestionResponse fromDTO(QuestionDTO dto) {
        return new QuestionResponse(
                dto.getUID().toString(),
                dto.getAuthor(),
                dto.getTitle(),
                dto.getContent()
        );
    }

    public QuestionResponse(String id, String author, String title, String content) {
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
                .addProperty("description", content)
                .addLink("self", this);
    }
}
