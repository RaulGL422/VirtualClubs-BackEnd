package galindo.raul.virtualclubs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class to enable and configure asynchronous execution in the application.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
  
  /**
   * Configures a custom {@link ThreadPoolTaskExecutor} for handling asynchronous tasks.
   *
   * @return a configured ThreadPoolTaskExecutor instance.
   */
  @Bean(name = "taskExecutor")
  public ThreadPoolTaskExecutor taskExecutor() {
    
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("async-");
    
    executor.initialize();
    return executor;
  }
}