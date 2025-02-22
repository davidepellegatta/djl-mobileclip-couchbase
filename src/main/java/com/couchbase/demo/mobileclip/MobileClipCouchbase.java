package com.couchbase.demo.mobileclip;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

public class MobileClipCouchbase {

    private static final Logger logger
            = LoggerFactory.getLogger(MobileClipCouchbase.class);

    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {

        String modelPath = "models/mobileclip_s1_torchscript.pt"; // Adjust the path
        URL resourceUrl = MobileClipCouchbase.class.getClassLoader().getResource(modelPath);

        String modelName = "mobileclip_s1";

        ZooModel<NDList, NDList> clip;
        Predictor<Image, float[]> imageFeatureExtractor;
        Predictor<String, float[]> textFeatureExtractor;

        logger.info(String.format("Model Path: %s ", resourceUrl.getPath()));

        Criteria<NDList, NDList> criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optModelPath(Paths.get(resourceUrl.getPath()))
                .optModelName(modelName)
                .optTranslator(new NoopTranslator())
                .optEngine("PyTorch")
                .optDevice(Device.cpu())
                .build();

        ZooModel<NDList, NDList> model = criteria.loadModel();

        imageFeatureExtractor = model.newPredictor(new ImageTranslator());
        textFeatureExtractor = model.newPredictor(new ImageTextTranslator());

        Image img = ImageFactory.getInstance().fromUrl("https://cdn.britannica.com/79/232779-050-6B0411D7/German-Shepherd-dog-Alsatian.jpg");

        float[] embeddings = imageFeatureExtractor.predict(img);

        //not working yet
        //float[] textEmbeddings = textFeatureExtractor.predict("photo of a dog");

        logger.info(String.valueOf(embeddings.length));
        logger.info(Arrays.toString(embeddings));


        //logger.info(String.valueOf(textEmbeddings.length));
        //logger.info(Arrays.toString(textEmbeddings));
    }

}
