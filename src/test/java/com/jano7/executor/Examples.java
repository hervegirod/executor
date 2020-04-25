/*
MIT License

Copyright (c) 2019 Jan Gaspar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.jano7.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Examples {

    private static long aTimeout = 30;

    public static void basicExample() throws InterruptedException {
        ExecutorService underlyingExecutor = Executors.newFixedThreadPool(10);
        KeySequentialRunner<String> runner = new KeySequentialRunner<>(underlyingExecutor);

        String tradeIdA = "327";
        String tradeIdB = "831";
        // more Trade IDs can arrive in a real scenario, but it is usually not known how many upfront

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // process a message for the trade
            }
        };

        runner.run(tradeIdA, task); // execute the task by the underlying executor

        runner.run(tradeIdB, task); // execution is not blocked by the task for tradeIdA

        runner.run(tradeIdA, task); // execution starts when the previous task for tradeIdA completes

        Executor executor = new KeySequentialExecutor(underlyingExecutor);

        Runnable runnable = new KeyRunnable<>(tradeIdA, task); // helper class delegating hashCode and equals to the key

        executor.execute(runnable);

        underlyingExecutor.shutdown();
        underlyingExecutor.awaitTermination(aTimeout, TimeUnit.SECONDS);
    }

    public static void boundedExecutorExample() throws InterruptedException {
        ExecutorService underlyingExecutor = Executors.newCachedThreadPool();
        BoundedExecutor boundedExecutor = new BoundedExecutor(10, underlyingExecutor);

        KeyRunnable<String> aTask = new KeyRunnable<>("my key", () -> {
            // do something
        });

        boundedExecutor.execute(aTask);

        // execute more tasks ...

        // before shutting down you can call a drain() method which blocks until all submitted task have been executed
        boundedExecutor.drain();
        // newly submitted tasks will be rejected after calling drain()

        underlyingExecutor.shutdown();
        underlyingExecutor.awaitTermination(aTimeout, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        basicExample();
        boundedExecutorExample();
    }
}