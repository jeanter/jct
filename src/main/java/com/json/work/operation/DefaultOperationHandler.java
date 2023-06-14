
package com.json.work.operation;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.json.work.util.ShutdownHookUtils;

public class DefaultOperationHandler extends OperationHandlerBase implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultOperationHandler.class);

	private final Semaphore haveWork = new Semaphore(1);
	private final long loopInterval;
	private boolean stopped;
	private volatile boolean waiting;

	public DefaultOperationHandler(int id, int waitingQueueSize, Map<String, String> config) {
		super(id, DefaultOperationHandler.class.getSimpleName() + "-" + id, waitingQueueSize);
		// 默认100毫秒
		loopInterval = 100;
	}

	@Override
	protected Logger getLogger() {
		return logger;
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
