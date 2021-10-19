package com.rose.ggpo;

import java.util.ArrayList;
import java.util.List;

public class StaticBuffer<T> extends ArrayList<T> {
    private List<T> elements;
    private int N;
    private int size;

    public StaticBuffer(int maxSize) {
        this.size = 0;
        N = maxSize;
        elements = new ArrayList<T>(N);
    }

    public void push_back(T element) {
        if(size == (N-1)) throw new NullPointerException("Array full");
        elements.add(size++, element);
    }

    @Override
    public T get(int i) {
        assert(i >= 0 && i < size);
        return elements.get(i);
    }

    @Override
    public int size() { return size; }
}