package com.howcode.aichat.infrastructure.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** 业务线程池：承接 AI 调用等耗时任务。 */
@Configuration
public class ExecutorConfig {

    @Bean(name = "chatExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor chatExecutor() {
        AtomicInteger index = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "aichat-worker-" + index.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        };
        return new ThreadPoolExecutor(4, 16, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), factory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
