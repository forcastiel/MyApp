package com.shrek.klib.event;

import com.shrek.klib.bo.Identity;
import com.shrek.klib.logger.ZLog;

public class ZEvent implements Identity {
	private static final int MAX_POOL_SIZE = 10;

	private static final int FLAG_IN_USE = 1 << 0;

	// TAG标识
	String eventTag;

	// 支持int 类型
	int eventFlag;
	
	// 作为参数和匹配类型
	Class<? extends ZEventPara> objClazz;

	ZEventPara obj;

	ZEvent next;

	// 支持延迟发送
	long when;

	int flags;

	private static final Object sPoolSync = new Object();
	private static ZEvent sPool;
	private static int sPoolSize = 0;

	private ZEvent() {

	}

	public static ZEvent obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				ZEvent event = sPool;
				sPool = event.next;
				event.next = null;
				sPoolSize--;
				return event;
			}
		}
		return new ZEvent();
	}

	public static ZEvent obtain(ZEvent orig) {
		ZEvent event = obtain();
		event.eventTag = orig.eventTag;
		event.eventFlag = orig.eventFlag;
		event.objClazz = orig.objClazz;

		return event;
	}

	public static ZEvent obtain(String eventTag, int eventFlag,
			Class<? extends ZEventPara> clazz) {
		ZEvent event = obtain();
		event.eventTag = eventTag;
		event.eventFlag = eventFlag;
		event.objClazz = clazz;
		return event;
	}

	public static ZEvent obtainObj(String eventTag, int eventFlag,
			ZEventPara obj) {
		ZEvent event = obtain();
		event.eventTag = eventTag;
		event.eventFlag = eventFlag;
		if (obj != null) {
			event.objClazz = obj.getClass();
		}

		event.obj = obj;

		return event;
	}
	

	public long getWhen() {
		return when;
	}

	boolean isInUse() {
		return ((flags & FLAG_IN_USE) == FLAG_IN_USE);
	}

	void markInUse() {
		flags |= FLAG_IN_USE;
	}

	@Override
	public int getIdentityID() {
		return 0;
	}

	void clearForRecycle() {
		eventTag = null;
		eventFlag = 0;
		objClazz = null;

		when = 0;
	}

	@Override
	public void recycle() {
		ZLog.i(ZEvent.class,toString() + "  释放到池中!");
		clearForRecycle();

		synchronized (sPoolSync) {
			if (sPoolSize < MAX_POOL_SIZE) {
				next = sPool;
				sPool = this;
				sPoolSize++;
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ZEvent) {
			ZEvent event = (ZEvent) o;
			if ((event.objClazz == null && objClazz != null)
					|| (objClazz == null && event.objClazz != null)) {
				return false;
			}
			return event.eventFlag == eventFlag
					&& event.eventTag.equals(eventTag)
					&& (event.objClazz == null ? objClazz == null
							: event.objClazz.isAssignableFrom(objClazz));
		}
		return super.equals(o);
	}

	@Override
	public String toString() {
		return "ZEvent [eventTag=" + eventTag + ", eventFlag=" + eventFlag
				+ ", objClazz=" + objClazz + "]";
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
