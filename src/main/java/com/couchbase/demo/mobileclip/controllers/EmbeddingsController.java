package com.couchbase.demo.mobileclip.controllers;

import ai.djl.translate.TranslateException;
import com.couchbase.demo.mobileclip.models.*;
import com.couchbase.demo.mobileclip.services.ClassificationService;
import com.couchbase.demo.mobileclip.services.MobileClipEmbeddingService;
import com.couchbase.lite.CouchbaseLiteException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("embeddings")
public class EmbeddingsController {

    private final ClassificationService classificationService;
    private final MobileClipEmbeddingService mobileClipEmbeddingService;

    public EmbeddingsController(ClassificationService classificationService, MobileClipEmbeddingService mobileClipEmbeddingService) {
        this.classificationService = classificationService;
        this.mobileClipEmbeddingService = mobileClipEmbeddingService;
    }

    @GetMapping("/image")
    public float[] image(@RequestParam(required = true) String imageUrl)
                                                    throws TranslateException, IOException {

        return mobileClipEmbeddingService.getEmbeddingsFromImage(imageUrl);
    }

    @PostMapping("/classification")
    public ClassificationResponse saveClassification(@RequestBody(required = true) ClassificationRequest request)
                                                    throws IOException, TranslateException, CouchbaseLiteException {

        return classificationService.saveClassification(request);
    }

    @PostMapping("/prediction")
    public PredictionResponse predictClassification(@RequestBody(required = true) PredictionRequest request)
                                                    throws TranslateException, IOException, CouchbaseLiteException {

        return classificationService.predictClassification(request);
    }

    @GetMapping("/availableLabels")
    public AvailableLabelsResponse labels() throws CouchbaseLiteException {

        return classificationService.getAvailableLabels();
    }

    @DeleteMapping("/availableLabels")
    public void deleteLabel(@RequestBody(required = true) DeleteClassificationRequest request)
                                                    throws CouchbaseLiteException {

        classificationService.deleteClassification(request);
    }

}
