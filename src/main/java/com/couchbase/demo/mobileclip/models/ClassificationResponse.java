package com.couchbase.demo.mobileclip.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassificationResponse {
    private String id;
    private float[] embeddings;
    private String className;
}
