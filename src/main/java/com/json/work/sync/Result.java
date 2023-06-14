package com.json.work.sync;

public class Result<T> {

	protected T result;
	protected Throwable cause;
	protected boolean succeeded;
	protected boolean failed;

	public Result() {
	}

	public Result(T result) {
		setResult(result);
	}

	public Result(Throwable cause) {
		setCause(cause);
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
		failed = false;
		succeeded = true;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
		failed = true;
		succeeded = false;
	}

	public boolean isSucceeded() {
		return succeeded;
	}

	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}
}
