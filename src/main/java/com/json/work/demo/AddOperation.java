package com.json.work.demo;

import com.json.work.operation.CASOperation;
import com.json.work.operation.DataReference;
import com.json.work.operation.OperationHandler;

public class AddOperation<R> extends CASOperation<R> {

	private WriteData data;

	public AddOperation(WriteData data) {
		this.data = data;
	}

	@Override
	public Integer doRun(OperationHandler currentHandler) {
		data.incr();
		return data.getNum();
	}

	@Override
	public DataReference getOpData() {
		// TODO Auto-generated method stub
		return data;
	}

}
