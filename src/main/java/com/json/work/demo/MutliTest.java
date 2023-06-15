package com.json.work.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.json.work.operation.OperationHandler;
import com.json.work.operation.OperationHandlerFactory;
import com.json.work.sync.Listener;
import com.json.work.sync.SyncListener;

public class MutliTest {
	
	public static final ThreadLocal<Long> TIME = new ThreadLocal<>();

	public static ExecutorService exec = new ThreadPoolExecutor(10, 100, 6000, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>(5000), new RejectedExecutionHandler() {
				// 队列满了 直接抛拒绝异常
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					throw new RejectedExecutionException("queue is full!");
				}
			});

	public static void main(String[] args) throws Exception {
		testA();
	}
	

	public static void testA() throws Exception {
		Map<String, String> config = new HashMap<String, String>();
		config.put("operation_handler_count", "4");
		int threadCount = 1000;
		int loopCout = 10;
		OperationHandlerFactory pohFactory = OperationHandlerFactory.create(config, null);
		WriteData data = new WriteData();
		List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
		System.out.println("start time: " + System.currentTimeMillis());
		for (int count = 0; count <= threadCount; count++) {
			Callable<Integer> callable = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					for (int i = 0; i <= loopCout; i++) {
						AddOperation<Integer> operation = new AddOperation<Integer>(data);
						OperationHandler handler = pohFactory.getOperationHandler();
						//Listener<Integer> listener = new SyncListener<Integer>();
						//operation.setResultHandler(listener);
						handler.handleOperation(operation);
					  //	listener.await();
						//operation.getResult();
					}
					return data.getNum();
				}
			};
			Future<Integer> futere = exec.submit(callable);
			futureList.add(futere);
		}

		for (int count = 0; count <= threadCount; count++) {
			Callable<Integer> callable = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					for (int i = 0; i <= loopCout; i++) {
						// 业务任务包装
						DecrOperation<Integer> operation = new DecrOperation<Integer>(data);
						// 业务线程
						OperationHandler handler = pohFactory.getOperationHandler();
//						//业务线程同步获取结果
//						Listener<Integer> listener = new SyncListener<Integer>();
//						operation.setResultHandler(listener);
						handler.handleOperation(operation);
					   //  listener.await();
						//operation.getResult();
					}
					return data.getNum();
				}
			};
			Future<Integer> futere = exec.submit(callable);
			futureList.add(futere);
		}
		for (Future<Integer> futere : futureList) {
			//System.out.println(futere.get());
			futere.get();
		}
		Thread.currentThread().sleep(1000);
		System.out.println("finish data size: " + data.getNum());
	}

	public static void testB() throws Exception {
		int threadCount = 500;
		int loopCout = 10;
		WriteData2 data = new WriteData2();
		List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
		for (int count = 1; count <= threadCount; count++) {
			Callable<Integer> callable = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					for (int i = 0; i < loopCout; i++) {
						data.incr();
					}
					return 1;
				}
			};
			Future<Integer> futere = exec.submit(callable);
			futureList.add(futere);
		}
		for (int count = 1; count <= threadCount; count++) {
			Callable<Integer> callable = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					for (int i = 0; i < loopCout; i++) {
						data.decr();
					}
					return 1;
				}
			};
			Future<Integer> futere = exec.submit(callable);
			futureList.add(futere);
		}
		for (Future<Integer> futere : futureList) {
			System.out.println("futere.get(): " + futere.get());
		}

		System.out.println(data.getNum());
	}
}
