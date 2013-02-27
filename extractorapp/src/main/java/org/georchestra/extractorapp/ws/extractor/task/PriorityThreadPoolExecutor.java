package org.georchestra.extractorapp.ws.extractor.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {
	  
	public PriorityThreadPoolExecutor (int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
	    super (corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	  }

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
	}

	protected <T> RunnableFuture<T> newTaskFor (Runnable runnable, T value) {
	    return new ComparableFutureTask<T> (runnable, value);
	  }

	  protected <T> RunnableFuture<T> newTaskFor (Callable<T> callable) {
	    return new ComparableFutureTask<T> (callable);
	  }

	  static class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<V>> {
	    Comparable comparable;

	    ComparableFutureTask (Callable<V> callable) {
	      super (callable);
	      comparable = (Comparable) callable;
	    }

	    ComparableFutureTask (Runnable runnable, V result) {
	      super (runnable, result);
	      comparable = (Comparable) runnable;
	    }

	    @SuppressWarnings("unchecked")
	    public int compareTo (ComparableFutureTask<V> ftask) {
	      return comparable.compareTo (ftask.comparable);
	    }
	  }
	}

