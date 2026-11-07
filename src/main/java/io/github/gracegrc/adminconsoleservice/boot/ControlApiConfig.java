package io.github.gracegrc.adminconsoleservice.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ControlApiConfig {

	@Bean
	public RestClient controlApiRestClient(RestClient.Builder builder,
			@Value("${control-api.base-url}") String baseUrl) {
		return builder.baseUrl(baseUrl).build();
	}

}
