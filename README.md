# Couchbase Lite offline image classification vector search with MobileClip

In this demo we are exploring the use of Couchbase Lite for offline vector search.

The high-level architecture is the same for a RAG application. The difference is that we need to architect for offline-first use and constrained hardware.

The two main architectural components are: 

- [**MobileClip**](https://github.com/apple/ml-mobileclip), an efficient image-text models designed for optimal performance on mobile devices created by Apple on top of [Clip](https://github.com/openai/CLIP)
- [**Couchbase Lite**](https://www.couchbase.com/products/lite/) for storing and querying the embeddings 

The application is a Spring / Java based rest service that offer apis for storing new embeddings and comparing images against the existing embeddings to provide a classification.

This is based on the work of the [Deeplearning Java Library](https://djl.ai/) that allowed the porting of mobileclip from pytorch to java.

### How to run

You can start the application by going to the root of the project and run: ```mvn spring-boot:run```. Point then your browser to ```http://localhost:8080```.

There are few images for testing the local database already included in the project under `src/main/resources/samples`

If you want to start from a clean slate, just delete the `data` directory and restart the application.

### I wish I knew before

- don't take pictures with your fingers... guess what? It will recognize fingers everywhere. So, don't.
- the accuracy is astonishing
- the models are pretty heavy (300-500GB..) wait ```git clone``` to finish properly.

### Future works:

- adapting the build for supporting different cpu architecture (at the moment you need to change the dependencies of djl yourself)

  ```  <dependency>
            <groupId>ai.djl.pytorch</groupId>
            <artifactId>pytorch-native-cpu</artifactId>
            <classifier>osx-aarch64</classifier>
        </dependency>
  ```

  you can work it around now by altering the ```classifier``` with your target architecture: [**Full list here**](https://djl.ai/engines/pytorch/pytorch-engine/#load-your-own-pytorch-native-library)
  
- containerize it
- clean up some couchbase lite advanced technicalities not necessary to serve the purpose.
- add [**Couchbase Capella App Services**](https://cloud.couchbase.com/) to sync the embeddings from cloud to edge
- add support for mobile browsers

## Requirements

- Java on your machine 17+
- maven


