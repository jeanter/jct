
package com.json.work.operation;

import com.json.work.sync.Listener;
import com.json.work.sync.Result;

public abstract class CASOperation<R> implements Operation {

	Listener<R> resultHandler;

	protected R result;


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
				resultHandler.handle(new Result<R>(result));
			}
			return OperationResult.SUCCEEDED;
		} else {
			return OperationResult.LOCKED;
		}
	};


	protected abstract Object doRun();

	public abstract DataReference getOpData();

	// 延后设置
	public void setResultHandler(Listener<R> listen) {
		this.resultHandler = listen;
	}

	public Listener<R> getResultHandler() {
		return resultHandler;
	}

	public R getResult() {
		return result;
	}

}
