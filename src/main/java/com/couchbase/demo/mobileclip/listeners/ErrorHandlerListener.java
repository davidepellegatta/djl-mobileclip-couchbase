package com.couchbase.demo.mobileclip.listeners;

import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.demo.mobileclip.replicator.ReplicationErrorHandler;
import com.couchbase.demo.mobileclip.triggers.ReplicationErrorTrigger;

public class ErrorHandlerListener implements ReplicatorChangeListener {

    final ReplicationErrorTrigger trigger;
    final ReplicationErrorHandler handler;

    public ErrorHandlerListener(ReplicationErrorTrigger trigger, ReplicationErrorHandler handler) {
        this.trigger = trigger;
        this.handler = handler;
    }

    @Override
    public void changed(ReplicatorChange change) {
        if(trigger.onNextElement(change) ) {
            handler.onError(change);
        }
    }
}
