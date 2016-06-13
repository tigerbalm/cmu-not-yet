package com.lge.notyet.driver.manager;

import java.util.concurrent.*;

public class TaskManager implements Runnable {

    private static int TASK_QUEUE_MAX = 10;
    private static int TASK_THREAD_MAX = 10;

    private final BlockingQueue<FutureTask> mTaskQueue;
    private final ExecutorService mTaskExecutor;

    public static TaskManager sTaskManager = null;

    private TaskManager () {
        mTaskQueue = new ArrayBlockingQueue<FutureTask>(TASK_QUEUE_MAX);
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
        System.out.println("RunTask");
        try {
            mTaskQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while(true) {

            FutureTask task = null;
            try {
                task = mTaskQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTaskExecutor.execute(task);
        }
    }
}
