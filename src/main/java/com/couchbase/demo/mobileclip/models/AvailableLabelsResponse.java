package com.couchbase.demo.mobileclip.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AvailableLabelsResponse {

    private List<String> classes;
    private Long totalEmbeddings;
}
