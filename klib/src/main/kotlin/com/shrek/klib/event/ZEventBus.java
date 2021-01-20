/*
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shrek.klib.event;

import android.os.Looper;

import com.shrek.klib.bo.Identity;
import com.shrek.klib.colligate.KConstantsKt;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.thread.HandlerEnforcer;
import com.shrek.klib.thread.ThreadMode;
import com.shrek.klib.thread.ZThreadEnforcer;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ZEventBus implements Identity {
	public static final String DEFAULT_IDENTIFIER = "ZEventBus";

//	private static ZEventBus instance;
	/** thread event*/
//	private Handler mainThreadHandler;
//	private ExecutorService backgroudHandler;
	
	private ZThreadEnforcer enforcer;
	
	/** All registered event handlers, indexed by event type. */
	private ConcurrentMap<ZEvent, Set<EventHandler>> handlersByType = new ConcurrentHashMap<ZEvent, Set<EventHandler>>();
	/** All registered event interceptors, indexed by event type. */
	private ConcurrentMap<ZEvent, EventHandler> interceptorsByType = new ConcurrentHashMap<ZEvent,EventHandler>();
	
	/** Identifier used to differentiate the event bus instance. */
	private String identifier;

	/** Thread enforcer for register, unregister, and posting events. */
//	private ThreadEnforcer enforcer;

	/** Used to find handler methods in register and unregister. */
	private HandlerFinder handlerFinder;

	/** Queues of events for the current thread to dispatch. */
	private ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>> eventsToDispatch = new ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>>() {
		@Override
		protected ConcurrentLinkedQueue<EventWithHandler> initialValue() {
			return new ConcurrentLinkedQueue<EventWithHandler>();
		}
	};

	/** True if the current thread is currently dispatching an event. */
	private ThreadLocal<Boolean> isDispatching = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	
//	public static ZEventBus newInstance(){
//		if(instance == null){
//			instance = new ZEventBus();
//		}
//		return instance;
//	}
	
	
	public ZEventBus() {
		this(DEFAULT_IDENTIFIER);
	}

	/**
	 * Creates a new Bus with the given {@code identifier} that enforces actions
	 * on the main thread.
	 *
	 * @param identifier
	 *            a brief name for this bus, for debugging purposes. Should be a
	 *            valid Java identifier.
	 */
	private ZEventBus(String identifier) {
		this(null, identifier);
	}

	/**
	 * Creates a new Bus named "default" with the given {@code enforcer} for
	 * actions.
	 *
	 * @param enforcer
	 *            Thread enforcer for register, unregister, and post actions.
	 */
	private ZEventBus(ZThreadEnforcer enforcer) {
		this(enforcer, DEFAULT_IDENTIFIER);
	}

	/**
	 * Creates a new Bus with the given {@code enforcer} for actions and the
	 * given {@code identifier}.
	 *
	 * @param enforcer
	 *            Thread enforcer for register, unregister, and post actions.
	 * @param identifier
	 *            A brief name for this bus, for debugging purposes. Should be a
	 *            valid Java identifier.
	 */
	private ZEventBus(ZThreadEnforcer enforcer, String identifier) {
		this(enforcer, identifier, HandlerFinder.ANNOTATED);
	}

	/**
	 * Test constructor which allows replacing the default {@code HandlerFinder}
	 * .
	 *
	 * @param enforcer
	 *            Thread enforcer for register, unregister, and post actions.
	 * @param identifier
	 *            A brief name for this bus, for debugging purposes. Should be a
	 *            valid Java identifier.
	 * @param handlerFinder
	 *            Used to discover event handlers and producers when
	 *            registering/unregistering an object.
	 */
	ZEventBus(ZThreadEnforcer enforcer, String identifier,
			  HandlerFinder handlerFinder) {
//		this.enforcer = enforcer;
		this.identifier = identifier;
		this.handlerFinder = handlerFinder;
		
		this.enforcer = HandlerEnforcer.newInstance();
	}

	@Override
	public String toString() {
		return "[ZEventBus \"" + identifier + "\"]";
	}

	/**
	 * Registers all handler methods on {@code object} to receive events and
	 * producer methods to provide events.
	 * <p>
	 * If any subscribers are registering for types which already have a
	 * producer they will be called immediately with the result of calling that
	 * producer.
	 * <p>
	 * If any producers are registering for types which already have
	 * subscribers, each subscriber will be called with the value from the
	 * result of calling the producer.
	 *
	 * @param object
	 *            object whose handler methods should be registered.
	 * @throws NullPointerException
	 *             if the object is null.
	 */
	public void register(Object object) {
		if (object == null) {
			throw new NullPointerException(
					"Object to register must not be null.");
		}
//		enforcer.enforce(this);
		Map<ZEvent, Set<EventHandler>> foundHandlersMap = handlerFinder
				.findAllSubscribers(object);
		for (ZEvent event : foundHandlersMap.keySet()) {
			Set<EventHandler> handlers = handlersByType.get(event);
			if (handlers == null) {
				// concurrent put if absent
				Set<EventHandler> handlersCreation = new CopyOnWriteArraySet<EventHandler>();
				handlers = handlersByType.putIfAbsent(event, handlersCreation);
				if (handlers == null) {
					handlers = handlersCreation;
				}
			}
			final Set<EventHandler> foundHandlers = foundHandlersMap.get(event);
			handlers.addAll(foundHandlers);
		}
	}

	/**
	 * register interceptor for bus
	 * @param interceptor
	 */
	public void registerInterceptor(ZEventInterceptor interceptor) {
		if (interceptor == null) {
			throw new NullPointerException(
					"Object to register must not be null.");
		}
		
		if(!interceptorsByType.isEmpty()){
			throw new IllegalArgumentException("interceptor 已经注册过了!");
		}
		
		interceptor.setBus(this);
		
//		enforcer.enforce(this);
		Map<ZEvent, EventHandler> foundHandlersMap = handlerFinder.findAllInterceptors(interceptor);
		for (ZEvent event : foundHandlersMap.keySet()) {
			EventHandler handlers = interceptorsByType.get(event);
			if (handlers == null) {
				// concurrent put if absent
				EventHandler handlersCreation = foundHandlersMap.get(event);
				handlers = interceptorsByType.putIfAbsent(event, handlersCreation);
				if (handlers == null) {
					handlers = handlersCreation;
				}
			}
		}
	}
	
	/**
	 * Unregisters all producer and handler methods on a registered
	 * {@code object}.
	 *
	 * @param object
	 *            object whose producer and handler methods should be
	 *            unregistered.
	 * @throws IllegalArgumentException
	 *             if the object was not previously registered.
	 * @throws NullPointerException
	 *             if the object is null.
	 */
	public void unregister(Object object) {
		if (object == null) {
			throw new NullPointerException(
					"Object to unregister must not be null.");
		}
//		enforcer.enforce(this);

		Map<ZEvent, Set<EventHandler>> handlersInListener = handlerFinder
				.findAllSubscribers(object);
		for (Map.Entry<ZEvent, Set<EventHandler>> entry : handlersInListener
				.entrySet()) {
			Set<EventHandler> currentHandlers = getHandlersForEventType(entry
					.getKey());
			Collection<EventHandler> eventMethodsInListener = entry.getValue();

			if (currentHandlers == null
					|| !currentHandlers.containsAll(eventMethodsInListener)) {
//				throw new IllegalArgumentException(
//						"Missing event handler for an annotated method. Is "
//								+ object.getClass() + " registered?");
				return;
			}

			for (EventHandler handler : currentHandlers) {
				if (eventMethodsInListener.contains(handler)) {
					handler.invalidate();
				}
			}
			currentHandlers.removeAll(eventMethodsInListener);
		}
	}
	
	public void unregisterInterceptor() {
		interceptorsByType.clear();
	}

	public void post(String eventTag) {
		ZEvent event = ZEvent.obtainObj(eventTag, KConstantsKt.NULL_INT_VALUE,
				null);
		post(event);
	}

	public void post(int eventFlag) {
		ZEvent event = ZEvent.obtainObj(KConstantsKt.NULL_STR_VALUE, eventFlag,
				null);
		post(event);
	}

	public void post(ZEventPara para) {
		ZEvent event = ZEvent.obtainObj(KConstantsKt.NULL_STR_VALUE,
				KConstantsKt.NULL_INT_VALUE, para);
		post(event);
	}

	public void post(String eventTag, ZEventPara para) {
		post(eventTag,  KConstantsKt.NULL_INT_VALUE, para);
	}
	
	public void post(int eventFlag, ZEventPara para) {
		post( KConstantsKt.NULL_STR_VALUE, eventFlag, para);
	}
	
	public void post(String eventTag, int eventFlag, ZEventPara para) {
		ZEvent event = ZEvent.obtainObj(eventTag, eventFlag, para);
		post(event);
	}

	/**
	 * Posts an event to all registered handlers. This method will return
	 * successfully after the event has been posted to all handlers, and
	 * regardless of any exceptions thrown by handlers.
	 *
	 * <p>
	 * If no handlers have been subscribed for {@code event}'s class, and
	 * {@code event} is not already a {@link DeadEvent}, it will be wrapped in a
	 * DeadEvent and reposted.
	 *
	 * @param event
	 *            event to post.
	 * @throws NullPointerException
	 *             if the event is null.
	 */
	public void post(ZEvent event) {
		post(event, true);
	}
	
	/**
	 * 
	 * @param event
	 * @param isIntercept  是否拦截
	 */
	void post(ZEvent event,boolean isIntercept) {
		if (event == null) {
			throw new NullPointerException("Event to post must not be null.");
		}
//		enforcer.enforce(this);
		// Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());
		if(isIntercept){
			EventHandler handler = null;
			for(Map.Entry<ZEvent, EventHandler> entry : interceptorsByType.entrySet()){
				if(entry.getKey().equals(event)){
					handler = entry.getValue();
					break;
				}
			}
			if(handler != null){
				Object obj = dispatch(event, handler);
				if(obj == null || !Boolean.parseBoolean(obj.toString())){
					return;
				}
			}
		}
		
		// 发送事件时候 他的父类 也随着触发
		Set<EventHandler> wrappers = getHandlersForEventType(event);

		if (wrappers != null && !wrappers.isEmpty()) {
			for (EventHandler wrapper : wrappers) {
				enqueueEvent(event, wrapper);
			}
		} else {
			ZLog.i(DEFAULT_IDENTIFIER, event.toString() + "没有监听者!");
		}

		dispatchQueuedEvents();
	}

	/**
	 * Queue the {@code event} for dispatch during
	 * {@link #dispatchQueuedEvents()}. Events are queued in-order of occurrence
	 * so they can be dispatched in the same order. 得到本线程的队列 发送event事件
	 */
	protected void enqueueEvent(ZEvent event, EventHandler handler) {
		eventsToDispatch.get().offer(new EventWithHandler(event, handler));
	}

	/**
	 * Drain the queue of events to be dispatched. As the queue is being
	 * drained, new events may be posted to the end of the queue.
	 */
	protected void dispatchQueuedEvents() {
		if (isDispatching.get()) {
			return;
		}

		isDispatching.set(true);
		try {
			while (true) {
				EventWithHandler eventWithHandler = eventsToDispatch.get()
						.poll();
				if (eventWithHandler == null) {
					break;
				}

				if (eventWithHandler.handler.isValid()) {
					dispatch(eventWithHandler.event, eventWithHandler.handler);
				}
				// 最后一个释放
				if (eventsToDispatch.get().size() == 0) {
					eventWithHandler.event.recycle();
				}
			}
		} finally {
			isDispatching.set(false);
		}
	}

	/**
	 * Dispatches {@code event} to the handler in {@code wrapper}. This method
	 * is an appropriate override point for subclasses that wish to make event
	 * delivery asynchronous.
	 *
	 * @param event
	 *            event to dispatch.
	 * @param wrapper
	 *            wrapper that will call the handler.
	 */
	protected Object dispatch(final ZEvent event,final EventHandler wrapper) {
		Object obj = null;
		try {
			ThreadMode tMode = wrapper.getThreadMode();
			switch (tMode) {
			case MainThread:
				if(Looper.myLooper() == Looper.getMainLooper()){
					obj = wrapper.handleEvent(event);
				}else{
					enforcer.enforceMainThread(new Runnable() {
						
						@Override
						public void run() {
							try {
								wrapper.handleEvent(event);
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					});
				}
				break;
			case BackgroundThread:
				enforcer.enforceBackgroud(new Runnable() {
					
					@Override
					public void run() {
						try {
							wrapper.handleEvent(event);
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				});
				break;
			case PostThread:
			default:
				obj = wrapper.handleEvent(event);
				break;
			}
			
		} catch (InvocationTargetException e) {
			throwRuntimeException(
					"Could not dispatch event: " + event.getClass()
							+ " to handler " + wrapper, e);
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * Retrieves a mutable set of the currently registered handlers for
	 * {@code type}. If no handlers are currently registered for {@code type},
	 * this method may either return {@code null} or an empty set.
	 *
	 * @param type
	 *            type of handlers to retrieve.
	 * @return currently registered handlers, or {@code null}.
	 */
	Set<EventHandler> getHandlersForEventType(ZEvent type) {
		for (Map.Entry<ZEvent, Set<EventHandler>> entry : handlersByType
				.entrySet()) {
			if (type.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		return handlersByType.get(type);
	}

	/**
	 * Throw a {@link RuntimeException} with given message and cause lifted from
	 * an {@link InvocationTargetException}. If the specified
	 * {@link InvocationTargetException} does not have a cause, neither will the
	 * {@link RuntimeException}.
	 */
	private static void throwRuntimeException(String msg,
			InvocationTargetException e) {
		Throwable cause = e.getCause();
		if (cause != null) {
			throw new RuntimeException(msg + ": " + cause.getMessage(), cause);
		} else {
			throw new RuntimeException(msg + ": " + e.getMessage(), e);
		}
	}

	/** Simple struct representing an event and its handler. */
	static class EventWithHandler {
		final ZEvent event;
		final EventHandler handler;

		public EventWithHandler(ZEvent event, EventHandler handler) {
			this.event = event;
			this.handler = handler;
		}
	}

	@Override
	public int getIdentityID() {
		return 0;
	}

	@Override
	public void recycle() {
		if(handlersByType != null){
			handlersByType.clear();
			handlersByType = null;
		}

		identifier = null;

//		enforcer = null;

		handlerFinder = null;

		if(eventsToDispatch != null){
			eventsToDispatch.remove();
			eventsToDispatch = null;
		}
	
		isDispatching.remove();
	}
}
