package com.couchbase.demo.mobileclip.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PredictionDbModel {

    private String className;
    private Float distance;
}
