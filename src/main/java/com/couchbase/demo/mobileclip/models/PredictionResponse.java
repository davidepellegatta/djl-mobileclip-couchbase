package com.couchbase.demo.mobileclip.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PredictionResponse {

    private List<PredictionDbModel> predictions;
}
