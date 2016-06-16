package com.lge.notyet.driver.manager;

import java.util.concurrent.*;

public class TaskManager implements Runnable {

    private static final int TASK_QUEUE_MAX = 20;
    private static final int TASK_THREAD_MAX = 10;

    private final BlockingQueue<FutureTask> mTaskQueue;
    private final ExecutorService mTaskExecutor;

    private static TaskManager sTaskManager = null;

    private TaskManager () {
        mTaskQueue = new ArrayBlockingQueue<>(TASK_QUEUE_MAX);
        mTaskExecutor = Executors.newFixedThreadPool(TASK_THREAD_MAX);

        new Thread(this).start();
    }

    public static TaskManager getInstance() {
        synchronized (TaskManager.class) {
            if (sTaskManager == null) sTaskManager = new TaskManager();
        }
        return sTaskManager;
    }

    public void runTask (FutureTask task) {
        try {
            mTaskQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        //noinspection InfiniteLoopStatement
        while(true) {

            try {
                FutureTask task = mTaskQueue.take();
                mTaskExecutor.execute(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
