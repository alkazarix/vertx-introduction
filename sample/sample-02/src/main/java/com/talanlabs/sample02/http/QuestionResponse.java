package com.talanlabs.sample02.http;

import black.door.hate.HalRepresentation;
import black.door.hate.HalResource;
import com.talanlabs.sample02.core.QuestionDTO;

import java.net.URI;

public class QuestionResponse implements HalResource {

    private  String uid;
    private  String author;
    private  String title;
    private String content;


    public static QuestionResponse fromDTO(QuestionDTO dto) {
        return new QuestionResponse(
                dto.getUID().toString(),
                dto.getAuthor(),
                dto.getTitle(),
                dto.getContent()
        );
    }

    public QuestionResponse(String uid, String author, String title, String content) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.content = content;

    }

    @Override
    public URI location() {
        return URI.create("/questions/" + uid);
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
