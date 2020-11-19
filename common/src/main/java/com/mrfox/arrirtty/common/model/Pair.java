package com.mrfox.arrirtty.common.model;

/*****
 * 封装类
 * @author     : MrFox
 * @date       : 2020-09-04 16:17
 * @description:
 * @version    :
 ****/
public class Pair<T1, T2> {
    private T1 object1;
    private T2 object2;

    public Pair(T1 object1, T2 object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    public T1 getObject1() {
        return object1;
    }

    public void setObject1(T1 object1) {
        this.object1 = object1;
    }

    public T2 getObject2() {
        return object2;
    }

    public void setObject2(T2 object2) {
        this.object2 = object2;
    }
}
