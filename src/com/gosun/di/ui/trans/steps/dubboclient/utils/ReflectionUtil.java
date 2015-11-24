package com.gosun.di.ui.trans.steps.dubboclient.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {
	
	public static List<Method> getInterfaceMethod(Class<?> interfaceClazz) {
		List<Method> result = new ArrayList<Method>();
		Method[] methods = interfaceClazz.getMethods();
		for(Method method : methods) {
			if(method.getModifiers() == (Modifier.PUBLIC + Modifier.ABSTRACT)) {
				result.add(method);
			}
		}
		return result;
	}
	
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] methodArgTypes) throws Exception {
		Method method = null;
		if(clazz != null ) {
			method = clazz.getDeclaredMethod(methodName, methodArgTypes);
		}
		return method;
	}
	
	public static Class<?>[] getMethodArgTypes(Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		return paramTypes;
	}
	
	public static Class<?> getMethodReturnType(Method method) {
		return method.getReturnType();
	}
	
	public static Object invoke(Method method, Object obj, Object... args) throws Exception {
		method.setAccessible(true);
		return method.invoke(obj, args);
	}

}
