package com.json.work.sync;

public interface Listener<V> {

	void handle(Result<V> result);

	V await();
}
