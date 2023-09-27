package com.json.work.async;



public interface Listener<T> extends AsyncHandler<AsyncResult<T>> { 


	T await();
}
