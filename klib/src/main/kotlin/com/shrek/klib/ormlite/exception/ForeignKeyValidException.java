package com.shrek.klib.ormlite.exception;

public class ForeignKeyValidException extends RuntimeException{

	/**
	 *  外键无效的异常
	 */
	private static final long serialVersionUID = 3494742589876470663L;

	public ForeignKeyValidException() {
		
		super();
	}

	public ForeignKeyValidException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ForeignKeyValidException(String detailMessage) {
		super(detailMessage);
	}

	public ForeignKeyValidException(Throwable throwable) {
		super(throwable);
	}
	
	

	 

}
