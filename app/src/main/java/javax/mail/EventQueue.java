/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.mail;

import java.util.EventListener;
import java.util.Vector;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executor;
import javax.mail.event.MailEvent;

/**
 * Package private class used by Store & Folder to dispatch events.
 * This class implements an event queue, and a dispatcher thread that
 * dequeues and dispatches events from the queue.
 *
 * @author	Bill Shannon
 */
class EventQueue implements Runnable {

    private volatile BlockingQueue<QueueElement> q;
    private Executor executor;

    private static WeakHashMap<ClassLoader,EventQueue> appq;

    /**
     * A special event that causes the queue processing task to terminate.
     */
    static class TerminatorEvent extends MailEvent {
	private static final long serialVersionUID = -2481895000841664111L;

	TerminatorEvent() {
	    super(new Object());
	}

	@Override
	public void dispatch(Object listener) {
	    // Kill the event dispatching thread.
	    Thread.currentThread().interrupt();
	}
    }

    /**
     * A "struct" to put on the queue.
     */
    static class QueueElement {
	MailEvent event = null;
	Vector<? extends EventListener> vector = null;

	QueueElement(MailEvent event, Vector<? extends EventListener> vector) {
	    this.event = event;
	    this.vector = vector;
	}
    }

    /**
     * Construct an EventQueue using the specified Executor.
     * If the Executor is null, threads will be created as needed.
     */
    EventQueue(Executor ex) {
	this.executor = ex;
    }

    /**
     * Enqueue an event.
     */
    synchronized void enqueue(MailEvent event,
	    Vector<? extends EventListener> vector) {
	// if this is the first event, create the queue and start the event task
	if (q == null) {
	    q = new LinkedBlockingQueue<>();
	    if (executor != null) {
		executor.execute(this);
	    } else {
		Thread qThread = new Thread(this, "Jakarta-Mail-EventQueue");
		qThread.setDaemon(true);  // not a user thread
		qThread.start();
	    }
	}
	q.add(new QueueElement(event, vector));
    }

    /**
     * Terminate the task running the queue, but only if there is a queue.
     */
    synchronized void terminateQueue() {
	if (q != null) {
	    Vector<EventListener> dummyListeners = new Vector<>();
	    dummyListeners.setSize(1); // need atleast one listener
	    q.add(new QueueElement(new TerminatorEvent(), dummyListeners));
	    q = null;
	}
    }

    /**
     * Create (if necessary) an application-scoped event queue.
     * Application scoping is based on the thread's context class loader.
     */
    static synchronized EventQueue getApplicationEventQueue(Executor ex) {
	ClassLoader cl = Session.getContextClassLoader();
	if (appq == null)
	    appq = new WeakHashMap<>();
	EventQueue q = appq.get(cl);
	if (q == null) {
	    q = new EventQueue(ex);
	    appq.put(cl, q);
	}
	return q;
    }

    /**
     * Pull events off the queue and dispatch them.
     */
    @Override
    public void run() {

	BlockingQueue<QueueElement> bq = q;
	if (bq == null)
	    return;
	try {
	    loop:
	    for (;;) {
		// block until an item is available
		QueueElement qe = bq.take();
		MailEvent e = qe.event;
		Vector<? extends EventListener> v = qe.vector;

		for (int i = 0; i < v.size(); i++)
		    try {
			e.dispatch(v.elementAt(i));
		    } catch (Throwable t) {
			if (t instanceof InterruptedException)
			    break loop;
			// ignore anything else thrown by the listener
		    }

		qe = null; e = null; v = null;
	    }
	} catch (InterruptedException e) {
	    // just die
	}
    }
}
