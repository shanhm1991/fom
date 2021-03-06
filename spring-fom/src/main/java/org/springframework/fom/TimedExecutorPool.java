package org.springframework.fom;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
class TimedExecutorPool extends ThreadPoolExecutor { 

	private final Map<Task<?>, Thread> threadMap = new ConcurrentHashMap<>();

	public TimedExecutorPool(int core, int max, long aliveTime, BlockingQueue<Runnable> workQueue) {
		super(core, max, aliveTime, TimeUnit.SECONDS, workQueue);
	}

	@Override
	protected <T> TimedFuture<T> newTaskFor(Runnable runnable, T value) {
		return new TimedFuture<>(runnable, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> TimedFuture<T> newTaskFor(Callable<T> callable) {
		if(callable instanceof Task){
			Task<T> task = (Task<T>)callable;
			task.setSubmitTime(System.currentTimeMillis()); 
		}
		return new TimedFuture<>(callable);
	}

	@SuppressWarnings({"rawtypes"})
	@Override
	protected void beforeExecute(Thread t, Runnable r) { 
		if(r instanceof TimedFuture){
			TimedFuture future = (TimedFuture)r;
			threadMap.put(future.getTask(), t);
		}
	}

	@SuppressWarnings({"rawtypes" })
	@Override
	protected void afterExecute(Runnable r, Throwable t) { 
		if(r instanceof TimedFuture){
			TimedFuture future = (TimedFuture)r;
			threadMap.remove(future.getTask());
		}
	}

	@Override
	public TimedFuture<Boolean> submit(Runnable runnable) {
		if (runnable == null)
			throw new NullPointerException();
		TimedFuture<Boolean> future = newTaskFor(runnable, true);
		execute(future);
		return future;
	}

	@Override
	public <T> TimedFuture<T> submit(Callable<T> callable) { 
		if (callable == null)
			throw new NullPointerException();
		TimedFuture<T> future = newTaskFor(callable);
		execute(future);
		return future;
	}

	public Map<Task<?>, Thread> getActiveThreads() {
		return threadMap;
	}
}
