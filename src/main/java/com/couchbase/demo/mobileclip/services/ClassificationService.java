package com.couchbase.demo.mobileclip.services;

import ai.djl.translate.TranslateException;
import com.couchbase.demo.mobileclip.database.DatabaseService;
import com.couchbase.demo.mobileclip.models.*;
import com.couchbase.lite.CouchbaseLiteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ClassificationService {

    private final MobileClipEmbeddingService mobileClipEmbeddingService;
    private final DatabaseService dbService;

    private final String ENCODING = "UTF-8";

    public ClassificationService(MobileClipEmbeddingService mobileClipEmbeddingService, DatabaseService dbService) {
        this.mobileClipEmbeddingService = mobileClipEmbeddingService;
        this.dbService = dbService;
    }

    public PredictionResponse predictClassification(PredictionRequest request) throws IOException, TranslateException, CouchbaseLiteException {

        String base64Image = request.getImage();
        float[] embeddings = mobileClipEmbeddingService.getEmbeddingsFromImage(decodeToImage(base64Image));

        List<PredictionDbModel> classes = dbService.findNearbyEmbeddings(embeddings);

        return new PredictionResponse(classes);
    }

    public ClassificationResponse saveClassification(ClassificationRequest request) throws IOException, TranslateException, CouchbaseLiteException {

        //TODO: add validations
        String base64Image = request.getImage();

        float[] embeddings = mobileClipEmbeddingService.getEmbeddingsFromImage(decodeToImage(base64Image));

        String uuid = UUID.randomUUID().toString();

        dbService.saveEmbedding(uuid, embeddings, request.getClassName());

        return new ClassificationResponse(uuid, embeddings, request.getClassName());

    }

    public AvailableLabelsResponse getAvailableLabels() throws CouchbaseLiteException {

        List<String> labels = dbService.findUniqueClasses();
        Long totalEmbeddings = dbService.countNumberOfLabels();

        return new AvailableLabelsResponse(labels, totalEmbeddings);
    }

    private BufferedImage decodeToImage(String base64Image) throws IOException {
        BufferedImage img = null;
        try {
            byte[] imageBytes = Base64.getDecoder().decode(new String(base64Image).getBytes(ENCODING));
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            img = ImageIO.read(bis);
            bis.close();
        } catch (IOException e) {
            throw new IOException("Error decoding base64 image", e);
        }
        return img;
    }
}
