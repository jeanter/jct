package com.json.work.async;

import java.util.concurrent.CountDownLatch;



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

	  public void handle(AsyncResult<V> ar) {
          if (ar.isSucceeded())
              result = ar.getResult();
          else
              e = new RuntimeException(ar.getCause());
          latch.countDown();
      }

	 


}
