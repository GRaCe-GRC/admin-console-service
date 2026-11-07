package io.github.gracegrc.adminconsoleservice.boot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashboardExecutorConfig {

	@Bean(destroyMethod = "close")
	public ExecutorService dashboardExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

}
