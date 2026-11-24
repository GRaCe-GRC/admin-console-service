package io.github.gracegrc.adminconsoleservice.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RegulatoryApiConfig {

	@Bean
	public RestClient regulatoryApiRestClient(RestClient.Builder builder,
			@Value("${regulatory-api.base-url}") String baseUrl) {
		return builder.baseUrl(baseUrl).build();
	}

}
