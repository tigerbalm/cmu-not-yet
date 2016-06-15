package com.lge.notyet.owner.ui;

public interface ITaskDoneCallback<T> {

    int SUCCESS = 0;
    int FAIL = 1;

    void onDone(int result, T response);
}
