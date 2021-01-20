/*
 * Copyright (C) 2012 Square, Inc.
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

import java.util.Map;
import java.util.Set;

/** Finds producer and subscriber methods. */
interface HandlerFinder {

	Map<ZEvent, Set<EventHandler>> findAllSubscribers(Object listener);
	
	Map<ZEvent, EventHandler> findAllInterceptors(Object listener);

	HandlerFinder ANNOTATED = new HandlerFinder() {

		@Override
		public Map<ZEvent, Set<EventHandler>> findAllSubscribers(
				Object listener) {
			return AnnotatedHandlerFinder.findAllSubscribers(listener);
		}

		@Override
		public Map<ZEvent, EventHandler> findAllInterceptors(Object listener) {
			// TODO Auto-generated method stub
			return AnnotatedHandlerFinder.findAllInterceptors(listener);
		}
	};
}
