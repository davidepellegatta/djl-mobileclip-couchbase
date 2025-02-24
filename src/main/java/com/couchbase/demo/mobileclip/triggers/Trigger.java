package com.couchbase.demo.mobileclip.triggers;

import com.couchbase.demo.mobileclip.utils.Resetable;

public interface Trigger<T> extends Resetable {
    boolean onNextElement(T element);
    default boolean onNextElement() {
        return onNextElement(null);
    }

}
