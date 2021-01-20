package com.shrek.klib.event;

/**
 * 事件拦截器
 * @author shrek
 *
 */
public abstract class ZEventInterceptor {

	ZEventBus bus;
	
	public ZEventInterceptor(){
		super();
	}

	public void setBus(ZEventBus bus) {
		if(bus == null){
			throw new IllegalArgumentException("事件拦截器的 event bus不能为NULL!");
		}
		this.bus = bus;
	}


	public void destroy(){
		bus.unregisterInterceptor();
		bus = null;
	}
	
	protected final void resendEvent(ZEvent event){
		bus.post(event, false);
	}
}
