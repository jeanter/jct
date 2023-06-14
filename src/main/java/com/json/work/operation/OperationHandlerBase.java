package com.json.work.operation;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.slf4j.Logger;
import com.json.work.operation.Operation.OperationResult;
 



public abstract class OperationHandlerBase extends Thread implements OperationHandler {

    // LinkedBlockingQueue测出的性能不如ConcurrentLinkedQueue好
    protected final ConcurrentLinkedQueue<Operation> operations = new ConcurrentLinkedQueue<>();
    protected final AtomicLong size = new AtomicLong();
    protected final int handlerId;
    protected final AtomicReferenceArray<OperationHandler> waitingHandlers;
    protected final AtomicBoolean hasWaitingHandlers = new AtomicBoolean(false);

    public OperationHandlerBase(int handlerId, String name, int waitingQueueSize) {
        super(name);
        setDaemon(false);
        this.handlerId = handlerId;
        waitingHandlers = new AtomicReferenceArray<>(waitingQueueSize);
    }
 
    protected abstract Logger getLogger();

    @Override
    public int getHandlerId() {
        return handlerId;
    }

    @Override
    public long getLoad() {
        return size.get();
    }

    @Override
    public void handleOperation(Operation po) {
        size.incrementAndGet();
        operations.add(po);
        wakeUp();
    }

    @Override
    public void addWaitingHandler(OperationHandler handler) {
        int id = handler.getHandlerId();
        if (id >= 0) {
            waitingHandlers.set(id, handler);
            hasWaitingHandlers.set(true);
        }
    }

    @Override
    public void wakeUpWaitingHandlers() {
        if (hasWaitingHandlers.compareAndSet(true, false)) {
            for (int i = 0, length = waitingHandlers.length(); i < length; i++) {
                OperationHandler handler = waitingHandlers.get(i);
                if (handler != null) {
                    handler.wakeUp();
                    waitingHandlers.compareAndSet(i, handler, null);
                }
            }
        }
    }

    protected void runPageOperationTasks() {
        if (size.get() <= 0)
            return;
        long size = this.size.get();
        System.out.println("operations size "+size);
        for (int i = 0; i < size; i++) {
            Operation po = operations.poll();
            while (true) {
                try {
                	OperationResult result = po.run(this);
                    if (result == OperationResult.LOCKED) {
                        operations.add(po);
                    } else if (result == OperationResult.RETRY) {
                        continue;
                    } else {
                        this.size.decrementAndGet();
                    }
                } catch (Throwable e) {
                    getLogger().warn("Failed to run page operation: " + po, e);
                }
                break;
            }
        }
    }
}
