package com.couchbase.demo.mobileclip.retry;

import com.couchbase.demo.mobileclip.utils.Resetable;

public interface RetryStrategy extends Resetable {

    void onNext(Runnable callback);

    void reset();

}
