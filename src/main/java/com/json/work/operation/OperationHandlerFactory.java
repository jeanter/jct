package com.json.work.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class OperationHandlerFactory {

	protected OperationHandler[] operationHandlers;

	protected OperationHandlerFactory(Map<String, String> config, OperationHandler[] handlers) {
		if (handlers != null) {
			setOperationHandlers(handlers);
			return;
		}
		int handlerCount;
		if (config != null && config.containsKey("operation_handler_count"))
			handlerCount = Math.max(1, Integer.parseInt(config.get("operation_handler_count")));
		else {
			handlerCount = Runtime.getRuntime().availableProcessors();
		}
		operationHandlers = new OperationHandler[handlerCount];
		for (int i = 0; i < handlerCount; i++) {
			operationHandlers[i] = new DefaultOperationHandler(i, handlerCount, config);
		}
		System.out.println("operationHandlers length "+ operationHandlers.length);
		startHandlers();
	}

	public abstract OperationHandler getOperationHandler();

	public OperationHandler[] getOperationHandlers() {
		return operationHandlers;
	}

	public void setOperationHandlers(OperationHandler[] handlers) {
		operationHandlers = new OperationHandler[handlers.length];
		System.arraycopy(handlers, 0, operationHandlers, 0, handlers.length);
	}

	public void startHandlers() {
		for (OperationHandler h : operationHandlers) {
			if (h instanceof DefaultOperationHandler) {
				((DefaultOperationHandler) h).startHandler();
			}
		}
	}

	public void stopHandlers() {
		for (OperationHandler h : operationHandlers) {
			if (h instanceof DefaultOperationHandler) {
				((DefaultOperationHandler) h).stopHandler();
			}
		}
	}

	public static OperationHandlerFactory create(Map<String, String> config) {
		return create(config, null);
	}

	public static synchronized OperationHandlerFactory create(Map<String, String> config, OperationHandler[] handlers) {
		if (config == null)
			config = new HashMap<>(0);
		OperationHandlerFactory factory = null;
		String key = "page_operation_handler_factory_type";
		String type = null; // "LoadBalance";
		if (config.containsKey(key)) {
			type = config.get(key);
		}
		if (type == null || type.equalsIgnoreCase("RoundRobin"))
			factory = new RoundRobinFactory(config, handlers);
		else if (type.equalsIgnoreCase("Random"))
			factory = new RandomFactory(config, handlers);
		else if (type.equalsIgnoreCase("LoadBalance"))
			factory = new LoadBalanceFactory(config, handlers);
		else {
			throw new RuntimeException("Unknow " + key + ": " + type);
		}
		return factory;
	}

	private static class RandomFactory extends OperationHandlerFactory {

		private static final Random random = new Random();

		protected RandomFactory(Map<String, String> config, OperationHandler[] handlers) {
			super(config, handlers);
		}

		@Override
		public OperationHandler getOperationHandler() {
			int index = random.nextInt(operationHandlers.length);
			return operationHandlers[index];
		}
	}

	private static class RoundRobinFactory extends OperationHandlerFactory {

		private static final AtomicInteger index = new AtomicInteger(0);

		protected RoundRobinFactory(Map<String, String> config, OperationHandler[] handlers) {
			super(config, handlers);
		}

		@Override
		public OperationHandler getOperationHandler() {
			return operationHandlers[index.getAndIncrement() % operationHandlers.length];
		}
	}

	private static class LoadBalanceFactory extends OperationHandlerFactory {

		protected LoadBalanceFactory(Map<String, String> config, OperationHandler[] handlers) {
			super(config, handlers);
		}

		@Override
		public OperationHandler getOperationHandler() {
			long minLoad = Long.MAX_VALUE;
			int index = 0;
			for (int i = 0, size = operationHandlers.length; i < size; i++) {
				long load = operationHandlers[i].getLoad();
				if (load < minLoad) {
					index = i;
				}
					
			}
			return operationHandlers[index];
		}
	}
}
