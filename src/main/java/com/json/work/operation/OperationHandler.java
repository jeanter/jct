package com.json.work.operation;

public interface OperationHandler {

    // 没有用getId，考虑到实现类可能继承自java.lang.Thread，它里面也有一个getId，会导致冲突
    int getHandlerId();

    long getLoad();

    void handleOperation(Operation po);

    void addWaitingHandler(OperationHandler handler);

    void wakeUpWaitingHandlers();

    void wakeUp();

}
