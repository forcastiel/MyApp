package com.shrek.klib.presenter;

import java.lang.reflect.Method;

public class JoinPoint {
	private Method method;
	private Object[] objs;
	
	private long dealTime;

	public JoinPoint(Method method, Object[] objs) {
		super();
		this.method = method;
		this.objs = objs;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object[] getObjs() {
		return objs;
	}

	public void setObjs(Object[] objs) {
		this.objs = objs;
	}

	public long getDealTime() {
		return dealTime;
	}

	public void setDealTime(long dealTime) {
		this.dealTime = dealTime;
	}

	
}
