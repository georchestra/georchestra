// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//   
// ============================================================================
package routines.system;

public class TalendThreadPool {

    private volatile boolean stopAllWorkers = false;

    private TalendThread errorThread = null;

    private TalendThreadResult threadResult = null;

    private ThreadQueue idleWorkers;

    private ThreadPoolWorker[] workerList;

    public TalendThreadPool(int numberOfThreads) {
        threadResult = new TalendThreadResult();
        numberOfThreads = Math.max(1, numberOfThreads);
        idleWorkers = new ThreadQueue(numberOfThreads);
        workerList = new ThreadPoolWorker[numberOfThreads];
        for (int i = 0; i < workerList.length; i++) {
            workerList[i] = new ThreadPoolWorker(idleWorkers);
        }
    }

    public void execute(TalendThread target) throws InterruptedException {
        if (!stopAllWorkers) {
            ThreadPoolWorker worker = (ThreadPoolWorker) idleWorkers.remove();
            target.talendThreadPool = this;
            if (worker != null) {
                worker.process(target);
            }
        }
    }

    public void waitForEndOfQueue() {
        try {
            while (!stopAllWorkers && idleWorkers.getSize() != workerList.length) {
                Thread.sleep(100);
            }
            for (int i = 0; i < workerList.length; i++) {
                workerList[i].stopRequest();
                while (workerList[i].isAlive()) {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException x) {
        }
    }

    public void stopAllWorkers() {
        if (!stopAllWorkers) {
            try {
                stopAllWorkers = true;
                idleWorkers.destory();
                for (int i = 0; i < workerList.length; i++) {
                    workerList[i].stopRequest();
                    while (workerList[i].isAlive()) {
                        Thread.sleep(100);
                    }
                }
            } catch (InterruptedException x) {
            }

        }
    }

    public TalendThread getErrorThread() {
        return errorThread;
    }

    // only keep the first ErrorThread
    public void setErrorThread(TalendThread errorThread) {
        if (this.errorThread == null) {
            this.errorThread = errorThread;
        }
    }

    public TalendThreadResult getTalendThreadResult() {
        return threadResult;
    }
}

class ThreadPoolWorker extends Object {

    private static int nextWorkerID = 0;

    private ThreadQueue idleWorkers;

    private int workerID;

    private ThreadQueue handoffBox;

    private Thread internalThread;

    private volatile boolean noStopRequested;

    public ThreadPoolWorker(ThreadQueue idleWorkers) {
        this.idleWorkers = idleWorkers;

        workerID = getNextWorkerID();
        handoffBox = new ThreadQueue(1); // only one slot

        // just before returning, the thread should be created and started.
        noStopRequested = true;

        Runnable r = new Runnable() {

            public void run() {
                try {
                    runWork();
                } catch (Exception x) {
                    // in case ANY exception slips through
                    x.printStackTrace();
                }
            }
        };

        internalThread = new Thread(r);
        internalThread.start();
    }

    public static synchronized int getNextWorkerID() {
        // notice: synchronized at the class level to ensure uniqueness
        int id = nextWorkerID;
        nextWorkerID++;
        return id;
    }

    public void process(Runnable target) throws InterruptedException {
        handoffBox.add(target);
    }

    private void runWork() {
        while (noStopRequested) {
            try {
                idleWorkers.add(this);
                Runnable r = (Runnable) handoffBox.remove();
                runIt(r);
            } catch (InterruptedException x) {
                Thread.currentThread().interrupt(); // re-assert
            }
        }
    }

    private void runIt(Runnable r) {
        try {
            r.run();
        } catch (Exception runex) {
            runex.printStackTrace();
        } finally {
            Thread.interrupted();
        }
    }

    public void stopRequest() {
        noStopRequested = false;
        internalThread.interrupt();
    }

    public boolean isAlive() {
        return internalThread.isAlive();
    }
}

class ThreadQueue {

    private Object[] queue;

    private int maxSize;

    private int size;

    private int head;

    private int tail;

    private volatile boolean isDestory = false;

    public ThreadQueue(int cap) {
        maxSize = (cap > 0) ? cap : 1; // at least 1
        queue = new Object[maxSize];
        head = 0;
        tail = 0;
        size = 0;
    }

    public synchronized int getSize() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return (size == 0);
    }

    public synchronized boolean isFull() {
        return (size == maxSize);
    }

    public synchronized void add(Object obj) throws InterruptedException {
        waitWhileFull();
        queue[head] = obj;
        head = (head + 1) % maxSize;
        size++;

        notifyAll();
    }

    public synchronized Object remove() throws InterruptedException {
        waitWhileEmpty();
        Object obj = queue[tail];
        queue[tail] = null;
        tail = (tail + 1) % maxSize;
        size--;
        notifyAll();
        return obj;
    }

    public synchronized Object[] removeAll() throws InterruptedException {
        Object[] list = new Object[size];
        for (int i = 0; i < list.length; i++) {
            list[i] = remove();
        }
        return list;
    }

    public synchronized boolean waitUntilEmpty(long msTimeout) throws InterruptedException {
        if (msTimeout == 0L) {
            waitUntilEmpty();
            return true;
        }

        long endTime = System.currentTimeMillis() + msTimeout;
        long msRemaining = msTimeout;

        while (!isEmpty() && (msRemaining > 0L)) {
            wait(msRemaining);
            msRemaining = endTime - System.currentTimeMillis();
        }
        return isEmpty();
    }

    public synchronized void waitUntilEmpty() throws InterruptedException {
        while (!isEmpty()) {
            wait();
        }
    }

    // "remove" work with sign "isDestory"
    public synchronized void waitWhileEmpty() throws InterruptedException {
        while (!isDestory && isEmpty()) {
            wait();
        }
    }

    public synchronized void waitUntilFull() throws InterruptedException {
        while (!isFull()) {
            wait();
        }
    }

    // "add" work with sign "isDestory"
    public synchronized void waitWhileFull() throws InterruptedException {
        while (!isDestory && isFull()) {
            wait();
        }
    }

    public synchronized void destory() throws InterruptedException {
        isDestory = true;
        this.notify();
    }

}
