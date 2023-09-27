
package com.json.work.operation;

import com.json.work.async.AsyncHandler;
import com.json.work.async.AsyncResult;

public abstract class CASOperation<R> implements Operation {

	protected R result;

	/**
	 * 可设置同步回调Listener 也可以设置异步回调 AsyncHandler 
	 */
	AsyncHandler<AsyncResult<R>> resultHandler;

	@SuppressWarnings("unchecked")
	public OperationResult run(OperationHandler currentHandler) {
		DataReference dataReference = getOpData();
		// cas对目标数据进行加锁
		if (dataReference.tryLock(currentHandler)) {
			// 对目标数据进行操作
			result = (R) doRun();
			// 快速释放锁，不用等处理结果 不需要cas操作
			dataReference.unlock();
			if (resultHandler != null) {
				resultHandler.handle(new AsyncResult<R>(result));
			}
			return OperationResult.SUCCEEDED;
		} else {
			return OperationResult.LOCKED;
		}
	};

	protected abstract Object doRun();

	public abstract DataReference getOpData();

	// 延后设置
	public void setResultHandler(AsyncHandler<AsyncResult<R>> resultHandler) {
		this.resultHandler = resultHandler;
	}

	public AsyncHandler<AsyncResult<R>> getResultHandler() {
		return resultHandler;
	}

	public R getResult() {
		return result;
	}

}
