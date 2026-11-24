package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiAuthorityUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFrameworkUpdateRequestDto;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RegulatoryApiClient {

	private static final String AUTHORITIES_PATH = "/api/v1/authorities";

	private static final String FRAMEWORKS_PATH = "/api/v1/frameworks";

	private static final String FILES_PATH = "/api/v1/files";

	private final RestClient regulatoryApiRestClient;

	public RegulatoryApiClient(RestClient regulatoryApiRestClient) {
		this.regulatoryApiRestClient = regulatoryApiRestClient;
	}

	public void updateAuthority(UUID id, RegulatoryApiAuthorityUpdateRequestDto request) {
		call(() -> regulatoryApiRestClient.put()
			.uri(AUTHORITIES_PATH + "/{id}", id)
			.body(request)
			.retrieve()
			.toBodilessEntity());
	}

	public void updateFramework(UUID id, RegulatoryApiFrameworkUpdateRequestDto request) {
		call(() -> regulatoryApiRestClient.put()
			.uri(FRAMEWORKS_PATH + "/{id}", id)
			.body(request)
			.retrieve()
			.toBodilessEntity());
	}

	public void updateFile(UUID id, RegulatoryApiFileUpdateRequestDto request) {
		call(() -> regulatoryApiRestClient.put()
			.uri(FILES_PATH + "/{id}", id)
			.body(request)
			.retrieve()
			.toBodilessEntity());
	}

	private static <T> T call(Supplier<T> request) {
		try {
			return request.get();
		}
		catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				throw ex;
			}
			throw new RestClientException("The regulatory API returned " + ex.getStatusCode().value(), ex);
		}
		catch (ResourceAccessException ex) {
			throw new RestClientException("The regulatory API is unavailable", ex);
		}
	}

}
