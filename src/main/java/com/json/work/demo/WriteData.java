package com.json.work.demo;

import com.json.work.operation.DataReference;

public class WriteData extends DataReference {

	private Integer num = 0;

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public void incr() {
		num = num + 1;

	}

	public void decr() {
		num = num - 1;

	}
}
