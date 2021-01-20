/*
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

package com.shrek.klib.event.annotation;

import com.shrek.klib.colligate.KConstantsKt;
import com.shrek.klib.thread.ThreadMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
	
	String tag() default KConstantsKt.NULL_STR_VALUE;
	
	int flag() default KConstantsKt.NULL_INT_VALUE;
	
	//是否自动注入参数
	boolean isInjectParas() default true;
	
	//线程模式
	ThreadMode threadMode() default ThreadMode.MainThread;
}
