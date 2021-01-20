package com.shrek.klib.presenter;

import com.shrek.klib.presenter.ann.After;
import com.shrek.klib.presenter.ann.Before;
import com.shrek.klib.presenter.ann.Pointcut;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AopInvocation<ByProxy> implements InvocationHandler {

	ByProxy implObj;

	// Class<ByProxy> byProxyClazz;

	Map<String, Method> beforeMethods, afterMethods, pointcutMethod;

	public AopInvocation(ByProxy implObj) {
		this.implObj = implObj;
		// this.byProxyClazz = byProxyClazz;

		beforeMethods = new HashMap<>();
		afterMethods = new HashMap<>();
		pointcutMethod = new HashMap<>();

		Class<?> clazz = implObj.getClass();

		parseMethod(clazz.getDeclaredMethods());
		parseMethod(clazz.getMethods());
	}

	private void parseMethod(Method[] methods){
		for (Method method : methods) {

			if (method.getAnnotation(Before.class) != null) {
				if (!beforeMethods.containsKey(method.getName())) {
					beforeMethods.put(method.getName(), method);
				}
			}

			if (method.getAnnotation(After.class) != null) {
				if (!afterMethods.containsKey(method.getName())) {
					afterMethods.put(method.getName(), method);
				}
			}

			if (method.getAnnotation(Pointcut.class) != null) {
				if (!pointcutMethod.containsKey(method.getName())) {
					pointcutMethod.put(method.getName(), method);
				}
			}
		}
	}

	/**
	 * 生成代理实现类
	 * @param byProxyClazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ByProxy bind(Class<ByProxy> byProxyClazz) {
		return (ByProxy) Proxy.newProxyInstance(byProxyClazz.getClassLoader(), implObj.getClass().getInterfaces(),
				this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
		Method orginMethod = pointcutMethod.get(method.getName());
		Pointcut pointcut = null;
		if (orginMethod != null) {
			pointcut = orginMethod.getAnnotation(Pointcut.class);
		}

		JoinPoint joinPoint = null;
		
		if (pointcut != null && pointcut.before().length > 0) {

			joinPoint = new JoinPoint(orginMethod, args);

			for (String methodName : pointcut.before()) {
				Method beforMethod = beforeMethods.get(methodName);

				if (beforMethod == null) {
					continue;
				}

				Class<?>[] parameters = beforMethod.getParameterTypes();
				Object[] paraObjs = new Object[parameters.length];

				int initParaIndex = 0;
				for (int i = 0; i < parameters.length; i++) {
					Class<?> paraClazz = parameters[i];
					
					if(JoinPoint.class.isAssignableFrom(paraClazz)){
						paraObjs[i] = joinPoint;
						initParaIndex++;
						continue;
					}
					
					for (Object indexArg : args) {
						if (indexArg.getClass().isAssignableFrom(paraClazz)) {
							paraObjs[i] = indexArg;
							initParaIndex++;
						}
					}
				}

				// 检测代码
				if (initParaIndex != paraObjs.length) {
					throw new Exception("Before method parameter init error!");
				}

				try {
					beforMethod.invoke(implObj, paraObjs);
					if(joinPoint != null){
						args = joinPoint.getObjs();
					}
				} catch (InvocationTargetException e) {
					throw (RuntimeException) e.getTargetException();
				}

			}
			joinPoint.setDealTime(System.nanoTime());
		}

		Object returnVal = method.invoke(implObj, args);

		if (pointcut != null && pointcut.after().length > 0) {

			for (String methodName : pointcut.after()) {
				Method afterMethod = afterMethods.get(methodName);

				if (afterMethod == null) {
					continue;
				}

				Class<?>[] parasClazzs = afterMethod.getParameterTypes();
				Object[] paraObjs = new Object[parasClazzs.length];

				int initParaIndex = 0;
				for (int i = 0; i < parasClazzs.length; i++) {
					
					if(JoinPoint.class.isAssignableFrom(parasClazzs[i])){
						paraObjs[i] = joinPoint;
						initParaIndex++;
						continue;
					}

					if (returnVal != null && returnVal.getClass().isAssignableFrom(parasClazzs[i])) {
						paraObjs[i] = returnVal;
						initParaIndex++;
						continue;
					}

					for (Object indexArg : args) {
						if (indexArg.getClass().isAssignableFrom(parasClazzs[i])) {
							paraObjs[i] = indexArg;
							initParaIndex++;
						}
					}
				}

				// 检测代码
				if (initParaIndex != paraObjs.length) {
					throw new Exception("After method parameter init error!");
				}

				try {
					afterMethod.invoke(implObj, paraObjs);
				} catch (InvocationTargetException e) {
					throw (RuntimeException) e.getTargetException();
				}
			}

		}

		return returnVal;
	}

}
