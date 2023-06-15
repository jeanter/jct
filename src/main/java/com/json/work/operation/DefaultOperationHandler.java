
package com.json.work.operation;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.json.work.operation.Operation.OperationResult;
import com.json.work.util.ShutdownHookUtils;

public class DefaultOperationHandler extends Thread implements OperationHandler {

	private static final Logger logger = LoggerFactory.getLogger(DefaultOperationHandler.class);

	private final Semaphore haveWork = new Semaphore(1);
	private final long loopInterval;
	private boolean stopped;
	private volatile boolean waiting;

	// LinkedBlockingQueue测出的性能不如ConcurrentLinkedQueue好
	protected final ConcurrentLinkedQueue<Operation> operations = new ConcurrentLinkedQueue<>();
	protected final AtomicLong size = new AtomicLong();
	protected final int handlerId;
	protected final AtomicReferenceArray<OperationHandler> waitingHandlers;
	protected final AtomicBoolean hasWaitingHandlers = new AtomicBoolean(false);

	public DefaultOperationHandler(int handlerId, String name, int waitingQueueSize) {
		setDaemon(false);
		this.handlerId = handlerId;
		waitingHandlers = new AtomicReferenceArray<>(waitingQueueSize);
		// 默认100毫秒
		loopInterval = 100;
	}

	public DefaultOperationHandler(int id, int waitingQueueSize, Map<String, String> config) {
		this(id, DefaultOperationHandler.class.getSimpleName() + "-" + id, waitingQueueSize);
	}

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
		if (size.get() <= 0) {
			long finishedTime = System.currentTimeMillis();
			System.out.println("runPageOperationTasks finish time =   " + (finishedTime));
			return;
		}
		long size = this.size.get();
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
					logger.warn("Failed to run page operation: " + po, e);
				}
				break;
			}
		}
	}

	public void startHandler() {
		if (stopped)
			return;
		stopped = false;
		ShutdownHookUtils.addShutdownHook(getName(), () -> {
			stopHandler();
		});
		start();
	}

	public void stopHandler() {
		stopped = true;
		wakeUp();
	}

	@Override
	public void wakeUp() {
		if (waiting)
			haveWork.release(1);
	}

	@Override
	public void run() {
		while (!stopped) {
			runPageOperationTasks();
			doAwait();
		}
	}

	private void doAwait() {
		waiting = true;
		try {
			haveWork.tryAcquire(loopInterval, TimeUnit.MILLISECONDS);
			haveWork.drainPermits();
		} catch (InterruptedException e) {
			logger.warn("", e);
		} finally {
			waiting = false;
		}
	}

}
