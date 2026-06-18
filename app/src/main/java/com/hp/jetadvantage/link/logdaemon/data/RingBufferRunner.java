package com.hp.jetadvantage.link.logdaemon.data;

import android.util.Log;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RingBufferRunner {
    public static final String TAG = "[LD][RING]";
    public static final int MAX_SIZE = 10000;
    private String[] buffer;
    private int n, head, tail;
    boolean isFull;

    Lock ringBufferLock = new ReentrantLock();

    public RingBufferRunner () {
        init();
    }

    public void init(){
        this.buffer = new String[MAX_SIZE];
        this.n = MAX_SIZE;
        this.head = this.tail = 0;
        this.isFull = false;
    }

    public boolean isEmpty () {
        return !this.isFull && (this.head == this.tail);
    }

    public void append (String s) {
        if (this.isFull) this.head = (this.head + 1) % this.n;

        this.buffer[this.tail] = s;
        this.tail = (this.tail + 1) % this.n;
        if (this.tail == this.head) this.isFull = true;
    }

    public void remove () {
        if (!this.isEmpty()) {
            this.head = (this.head + 1) % this.n;
            this.isFull = false;
        }
    }

    public Vector<String> getRingBuffer () {
        ringBufferLock.lock();
        int numElements = size();
        Vector<String> buffer = new Vector<String>(numElements);
        for (int i = 0; i < numElements; i++) {
            buffer.add(this.buffer[(this.head + i) % this.n]);
        }
        ringBufferLock.unlock();
        return buffer;
    }

    public int size(){
        int size = 0;
        size = (this.isFull) ? this.n : ((this.head > this.tail) ? (this.tail + this.n) - this.head : this.tail - this.head);
        return size;
    }

}
