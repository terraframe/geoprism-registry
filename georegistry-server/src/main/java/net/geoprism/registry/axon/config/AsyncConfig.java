package net.geoprism.registry.axon.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer
{
  @Override
  @Bean(name = "taskExecutor")
  public Executor getAsyncExecutor()
  {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("async");
    return executor;
  }

  @Bean(name = "concurrentTaskExecutor")
  protected ConcurrentTaskExecutor getTaskExecutor()
  {
    return new ConcurrentTaskExecutor(this.getAsyncExecutor());
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
  {
    return new SimpleAsyncUncaughtExceptionHandler();
  }

}
