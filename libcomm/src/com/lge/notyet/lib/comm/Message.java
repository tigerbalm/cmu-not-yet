package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */
public class Message {

    protected Uri mUri;

    public int result;

    public int arg1;
    public int arg2;
    public String str;
    public Object obj;

    public void Message (Uri uri) {
        mUri = uri;
    }
}
