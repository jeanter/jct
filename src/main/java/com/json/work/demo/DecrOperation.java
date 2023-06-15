package com.json.work.demo;

import com.json.work.operation.CASOperation;
import com.json.work.operation.DataReference;


public class DecrOperation<R> extends CASOperation<R> {

	private WriteData data;

	public DecrOperation(WriteData data) {
		this.data = data;
	}

	@Override
	public Integer doRun() {
		data.decr();
		return data.getNum();
	}

	@Override
	public DataReference getOpData() {
		// TODO Auto-generated method stub
		return data;
	}

}
