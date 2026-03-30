package galindo.raul.virtualclubs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class to enable and configure asynchronous execution in the application.
 * Implements {@link AsyncConfigurer} para registrar un manejador global de excepciones
 * en métodos {@code @Async void}, cuyas excepciones no llegan al hilo del llamador.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

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

  /**
   * Manejador global para excepciones no capturadas en métodos {@code @Async void}.
   * Spring lo invoca automáticamente cuando un método async lanza una excepción
   * que no fue capturada dentro del propio método.
   */
  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (throwable, method, params) ->
        log.error("Excepción no capturada en método async '{}.{}': {}",
            method.getDeclaringClass().getSimpleName(),
            method.getName(),
            throwable.getMessage(), throwable);
  }
}