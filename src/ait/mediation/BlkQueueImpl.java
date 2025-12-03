package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private final LinkedList<T> queue = new LinkedList<>();
    private final int maxSize;
    private final Lock mutex = new ReentrantLock();
    private final Condition consumerWaitCondition = mutex.newCondition();
    private final Condition producerWaitCondition = mutex.newCondition();

    public BlkQueueImpl(int maxSize) {
        // TODO
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be > 0");
        }
        this.maxSize = maxSize;
    }

    @Override
    public void push(T message) {
        // TODO
        mutex.lock();
        try {
            while (queue.size() == maxSize) {
                producerWaitCondition.await();
            }
            queue.add(message);
            consumerWaitCondition.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public T pop() {
        // TODO
        mutex.lock();
        try {
            while (queue.isEmpty()) {
                consumerWaitCondition.await();
            }
            producerWaitCondition.signal();
            return queue.poll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.unlock();
        }
    }
}