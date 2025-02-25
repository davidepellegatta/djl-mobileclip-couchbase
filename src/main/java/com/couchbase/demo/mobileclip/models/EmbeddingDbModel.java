package com.couchbase.demo.mobileclip.models;

import com.couchbase.lite.Array;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmbeddingDbModel {

    private String id;
    private float[] embeddings;
    private String className;

    public MutableDocument toMutable() {

        MutableDocument mutableDocument = new MutableDocument();

        mutableDocument.setString("id", id);
        mutableDocument.setString("className", className);

        MutableArray array = new MutableArray();

        for (float val : embeddings) {
            array.addNumber(val);
        }

        mutableDocument.setArray("embeddings", array);

        return mutableDocument;
    }
}
