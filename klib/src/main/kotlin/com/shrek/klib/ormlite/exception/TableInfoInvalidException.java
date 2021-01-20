package com.shrek.klib.ormlite.exception;

public class TableInfoInvalidException extends RuntimeException{

	/**
	 *  TableInfo的异常
	 */
	private static final long serialVersionUID = 3494742589876470663L;

	public TableInfoInvalidException() {
		
		super();
	}

	public TableInfoInvalidException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public TableInfoInvalidException(String detailMessage) {
		super(detailMessage);
	}

	public TableInfoInvalidException(Throwable throwable) {
		super(throwable);
	}
	
	

	 

}
