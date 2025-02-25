package com.couchbase.demo.mobileclip.models;

import lombok.Data;

@Data
public class ClassificationRequest {
    private String className;
    private String image;
    //TODO: add also text option
}
