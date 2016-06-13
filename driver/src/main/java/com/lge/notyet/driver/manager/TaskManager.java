package com.lge.notyet.driver.manager;

import com.lge.notyet.lib.comm.mqtt.MqttNetworkConnection;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public class TaskManager implements Runnable {

    ExecutorService executor = Executors.newFixedThreadPool(10);

    public static TaskManager sTaskManager = null;


    private TaskManager () {

    }

    public static TaskManager getInstance() {
        synchronized (TaskManager.class) {
            if (sTaskManager == null) sTaskManager = new TaskManager();
        }
        return sTaskManager;
    }

    public void runTask (FutureTask task, ITaskDoneCallback doneCallback) {
        System.out.println("RunTask");
        executor.execute(task);
    }

    @Override
    public void run() {
    }
}
