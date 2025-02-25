package com.couchbase.demo.mobileclip.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

@Component
@Slf4j
public class MobileClipEmbeddingService {

    //to move to props
    private final String modelPath = "models/mobileclip_b_torchscript.pt";
    private final String modelName = "mobileclip_b";


    private final URL resourceUrl;
    private final ZooModel<NDList, NDList> model;
    private final Predictor<Image, float[]> imageFeatureExtractor;
    private final Predictor<String, float[]> textFeatureExtractor;

    public MobileClipEmbeddingService() throws ModelNotFoundException, MalformedModelException, IOException {
        this.resourceUrl = MobileClipCouchbase.class.getClassLoader().getResource(modelPath);

        log.info(String.format("Model Path: %s ", resourceUrl.getPath()));

        Criteria<NDList, NDList> criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optModelPath(Paths.get(resourceUrl.getPath()))
                .optModelName(modelName)
                .optTranslator(new NoopTranslator())
                .optEngine("PyTorch")
                .optDevice(Device.cpu())
                .build();

        model = criteria.loadModel();

        imageFeatureExtractor = model.newPredictor(new ImageTranslator());
        textFeatureExtractor = model.newPredictor(new ImageTextTranslator());
    }

    public float[] getEmbeddingsFromImage(String imageUrl) throws IOException, TranslateException {

        Image img = ImageFactory.getInstance().fromUrl(imageUrl);
        return imageFeatureExtractor.predict(img);
    }

    public float[] getEmbeddingsFromString(String text) throws TranslateException {

        return textFeatureExtractor.predict(text);
    }
}
