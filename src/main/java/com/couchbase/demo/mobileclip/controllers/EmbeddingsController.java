package com.couchbase.demo.mobileclip.controllers;

import ai.djl.translate.TranslateException;
import com.couchbase.demo.mobileclip.model.MobileClipEmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("embeddings")
public class EmbeddingsController {

    private final MobileClipEmbeddingService embeddingService;

    public EmbeddingsController(MobileClipEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping("/image")
    public float[] image(@RequestParam(required = true) String imageUrl) throws TranslateException, IOException {

        return embeddingService.getEmbeddingsFromImage(imageUrl);
    }
}
