package com.cashley;

import java.lang.reflect.Method;

class ListenerMethod {
    Object instance; // null for static methods
    Method method;
    boolean async;

    public ListenerMethod(Object instance, Method method, boolean async) {
        this.instance = instance;
        this.method = method;
        this.async = async;
    }
}