/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.example.office.events.bus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * The event bus allowing to listen to or to produce messages (events).
 * Is used for communication between the service and its UI.
 * <br>The singleton is unsafe because its initialization is separated from its usage
 * and it's guaranteed that the instance will be initialized before first use.
 */
public final class EventBus {

	private static EventBus instance;
	
	private final Bus bus = new Bus();
	private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());	
	
	private EventBus() {  }
	
	/**
	 * Initializes the bus.
	 */
	public static void init() {
		EventBus.instance = new EventBus();
	}
	
	/**
	 * Gets the instance of the bus.
	 * @return	event bus instance.
	 */
	public static EventBus getInstance() {
		return instance;
	}
	
	/**
	 * Posts a new event to the bus. The event is posted to the main thread.
	 * @param	event	the event to post
	 */
	public void post(Object event) {
		mainThreadHandler.post(new PostEventTask(event));
	}
	
	/**
	 * Registers a new bus listener.
	 * @param listener	the listener to register
	 */
	public void register(Object listener) {
		bus.register(listener);
	}
	
	/**
	 * Unregisters a listener previously registered via {@link #register(Object)}.
	 * @param listener	the listener to unregister
	 */
	public void unregister(Object listener) {
		bus.unregister(listener);
	}
	
	/**
	 * Posts events to the bus.
	 */
	private final class PostEventTask implements Runnable {
		
		private final Object message;
		
		PostEventTask(Object message) {
			this.message = message;
		}

		@Override
		public void run() {
			bus.post(message);
		}
	}	
}
