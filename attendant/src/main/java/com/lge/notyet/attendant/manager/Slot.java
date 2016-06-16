package com.lge.notyet.attendant.manager;

/**
 * Created by beney.kim on 2016-06-16.
 */
public class Slot {

    int mId;
    int mNumber;
    boolean mOccupied;
    long mOccupiedTimeStamp;

    public Slot(int id, int number, boolean occupied, long occupiedTimeStamp) {

        mId = id;
        mNumber = number;
        mOccupied = occupied;
        mOccupiedTimeStamp = occupiedTimeStamp;
    }

    public int getId() {
        return mId;
    }

    public int getNumber() {
        return mNumber;
    }

    public boolean isOccupied() {
        return mOccupied;
    }

    public long getOccupiedTimeStamp() {
        return mOccupiedTimeStamp;
    }

    public void setOccupied(boolean occupied) {
        mOccupied = occupied;
    }

    public void getOccupiedTimeStamp(long occupiedTimeStamp) {
        mOccupiedTimeStamp =occupiedTimeStamp;
    }
}
