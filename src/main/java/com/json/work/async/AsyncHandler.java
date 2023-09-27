package com.json.work.async;

public interface AsyncHandler<E> {

    /**
     * Something has happened, so handle it.
     *
     * @param event the event to handle
     */
	  void handle(E event);

}