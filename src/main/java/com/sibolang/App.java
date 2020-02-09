package com.sibolang;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Return task is class to store the task result
 */
class ResultRet {
    private int threadNo;
    private int val;

    public int getThreadNo() {
        return this.threadNo;
    }

    public int getVal() {
        return this.val;
    }

    public ResultRet() {
    }

    public ResultRet(final int threadNo, final int val) {
        this.threadNo = threadNo;
        this.val = val;
    }
}

/**
 * The task itself, implements callable, manual naming and generate random
 * number
 */
class Task implements Callable<ResultRet> {

    private final Integer threadNo;
    private Integer random;

    public Task(final Integer threadNo) {
        this.threadNo = threadNo;
    }

    @Override
    public ResultRet call() throws Exception {
        try {
            Thread.sleep(100);
            for (int i = 0; i <= threadNo * 10000; i++) {
                System.out.println("Thread no = " + threadNo + " doing something-" + i);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        random = Math.abs(new Random().nextInt()) % 10;
        System.out.println("Thread no = " + threadNo + " generate random-" + random);

        return new ResultRet(this.threadNo, this.random);
    }
}

/**
 * Our main class
 */
public final class App {
    private App() {
    }

    public static void main(final String[] args) {
        final Integer procNum = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(procNum);
        final int taskNum = 10; // the number of task to simulate

        System.out.println("Parallel in " + procNum + " processors");

        final List<Future> allFutures = new ArrayList<>();
        for (int i = 0; i < taskNum; i++) {
            final Future<ResultRet> future = executor.submit(new Task(i));
            allFutures.add(future);
        }

        executor.shutdown();

        try {
            while (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.out.println("Awaiting completion of threads.");
              }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        int[] ret = new int[taskNum];
        for (final Future<ResultRet> fut : allFutures) {
            try {
                final ResultRet retTask = fut.get();
                ret[retTask.getThreadNo()] = retTask.getVal();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < taskNum; i++) {
            System.out.println("Thread no = " + i + " result = " + ret[i]);
        }
    }
}
