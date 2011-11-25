package extractorapp.ws.extractor.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExtractionManager {
    private ThreadPoolExecutor executor;
    private int maxExtractions;
    private int minThreads;
    // ThreadPoolExecutor API says that the internal queue should not be
    // accessed except for debugging so this
    // queue is here so that the non-running tasks can be accessed
    private Collection<ExtractionTask> tasks = new PriorityBlockingQueue<ExtractionTask>();
    private Collection<ExtractionTask> cancelled = new PriorityBlockingQueue<ExtractionTask>();

    public synchronized void init() {
        BlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<Runnable>();
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Extractorapp-thread"
                        + System.currentTimeMillis());
                thread.setDaemon(true);
                return thread;
            }
        };
        final ExtractionManager manager = this;
        executor = new ThreadPoolExecutor(minThreads, maxExtractions, 5,
                TimeUnit.SECONDS, workQueue, threadFactory) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                synchronized (manager) {
                    ExtractionTask task = (ExtractionTask) r;
                    task.executionMetadata.setRunning();
                }
                super.beforeExecute(t, r);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                synchronized (manager) {
                    ExtractionTask task = (ExtractionTask) r;
                    task.executionMetadata.setCompleted();
                }
                super.afterExecute(r, t);
            }
        };
    }

    public void setMaxExtractions(int maxExtractions) {
        this.maxExtractions = maxExtractions;
    }

    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    public synchronized void submit(ExtractionTask extractor) {
        tasks.add(extractor);
        Future<?> future = executor.submit(extractor);
        extractor.executionMetadata.setFuture(future);
    }

    public synchronized void updatePriority(String uuid, ExecutionPriority newPriority) {
        for (ExtractionTask task : tasks) {
            if (task.equalId(uuid)) {
                if(task.executionMetadata.isWaiting()) { 
                    if(executor.remove(task)) {
                        tasks.remove(task);
                        task.executionMetadata.setPriority(newPriority);
                        
                        submit(task);
                    } 
                }
                break;
            }
        }
    }
    public synchronized void removeTask(String uuid) {
        for (ExtractionTask task : tasks) {
            if (task.equalId(uuid)) {
                if(executor.remove(task)) {
                    tasks.remove(task);
                    task.executionMetadata.cancel();
                    cancelled.add(task);
                }
            }
        }
    }
    
    /**
     * Will set priorities of all tasks to MEDIUM and re-add all waiting tasks back to the queue in the order of the uuids in newOrder.  
     * If a uuid is not the newOrder it will be deleted from the queue. 
     */
    public synchronized void updateAllPriorities(final List<String> newOrder) {
        executor.purge();
        Collection<ExtractionTask> newWaitingTasks = new TreeSet<ExtractionTask>(new Comparator<ExtractionTask>(){

            @Override
            public int compare(ExtractionTask task1, ExtractionTask task2) {
                return newOrder.indexOf(task1.executionMetadata.getUuid()) - newOrder.indexOf(task2.executionMetadata.getUuid());
            }
            
        });
        
        for (ExtractionTask task : tasks) {
            if(task.executionMetadata.isWaiting()) {
                tasks.remove(task);
                if(newOrder.contains(task.executionMetadata.getUuid())) {
                    newWaitingTasks.add(task);
                } else {
                    task.executionMetadata.cancel();
                    cancelled.add(task);
                }
            }
        }
        
        for (ExtractionTask task : newWaitingTasks) {
            task.executionMetadata.setPriority(ExecutionPriority.MEDIUM);
            submit(task);
        }
    }

    /**
     * Get a deep copy of task queue metadata.  The metadata objects are only copies so no changes will be reflected on the actual tasks
     */
    public synchronized List<ExecutionMetadata> getTaskQueue() {
        List<ExecutionMetadata> queue = new ArrayList<ExecutionMetadata>();
        for (ExtractionTask task : tasks) {
            queue.add(new ExecutionMetadata(task.executionMetadata));
        }
        for (ExtractionTask task : cancelled) {
            queue.add(new ExecutionMetadata(task.executionMetadata));
        }
        return queue;
    }
    public synchronized void cleanExpiredTasks(long expiry) {
        ArrayList<ExtractionTask> toRemove = new ArrayList<ExtractionTask>();
        for (ExtractionTask task : tasks) {
            ExecutionMetadata metadata = task.executionMetadata;
            if (metadata.isCompleted() && (metadata.getStateChangeTime().getTime() + expiry) > System.currentTimeMillis()) {
                toRemove.add(task);
            }
        }
        tasks.removeAll(toRemove);
        toRemove.clear();
        for (ExtractionTask task : cancelled) {
            ExecutionMetadata metadata = task.executionMetadata;
            if ((metadata.getStateChangeTime().getTime() + expiry) > System.currentTimeMillis()) {
                toRemove.add(task);
            }
        }
        cancelled.removeAll(toRemove);
    }

}
