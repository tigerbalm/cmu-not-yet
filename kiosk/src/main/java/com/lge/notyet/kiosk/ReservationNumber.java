package com.lge.notyet.kiosk;

/**
 * Created by sjun.lee on 2016-06-09.
 */
public class ReservationNumber {
    public interface NumberTextChangedListener {
        void onNumberTextChanged(int index, String number);
        void onNumberTextRemoved(int index);
    }

    int index;
    int[] reservationNumber = new int[4];
    NumberTextChangedListener listener;

    public void setNumberTextChangedListener(NumberTextChangedListener listener) {
        this.listener = listener;
    }

    public void addLast(String number) {
        if (index >= reservationNumber.length) {
            return;
        }

        reservationNumber[index++] = Integer.valueOf(number);

        if (listener != null) {
            listener.onNumberTextChanged(index - 1, number);
        }
    }

    public int removeLast() {
        if (index <= 0) {
            return 0;
        }

        if (listener != null) {
            listener.onNumberTextRemoved(index - 1);
        }

        return reservationNumber[--index];
    }

    public int get() {
        int number = 0;

        for (int i = 0, j = reservationNumber.length - 1; i < index; i ++, j --) {
            number += reservationNumber[i] * Math.pow(10, j);
        }

        return number;
    }

    public String getAsString() {
        return get() + "";
    }
}