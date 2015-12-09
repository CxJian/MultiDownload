package org.kgmeng.dmlib;

/**
 * Created by Administrator on 2015/8/31.
 */
public enum Type {
    MULTI(1), SINGLE(2);

    private int value;

    private Type(int val) {
        this.value = val;
    }

    public int getValue() {
        return value;
    }
}
