package com.json.work.sync;

import java.util.concurrent.CountDownLatch;

/**
 * 同步listen 让业务线程同步获取操作结果
 * 
 * @author diannao
 *
 * @param <V>
 */
public class SyncListener<V> implements Listener<V> {

	private final CountDownLatch latch = new CountDownLatch(1);
	private volatile RuntimeException e;
	private volatile V result;

	@Override
	public V await() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			this.e = new RuntimeException(e);
		}
		if (e != null)
			throw e;
		return result;
	}

	@Override
	public void handle(Result<V> ar) {

		if (ar.isSucceeded()) {
			result = ar.getResult();
		} else {
			e = new RuntimeException(ar.getCause());
		}
		latch.countDown();
	}
}
