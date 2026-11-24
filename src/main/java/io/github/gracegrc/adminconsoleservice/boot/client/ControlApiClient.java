package io.github.gracegrc.adminconsoleservice.boot.client;

import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlUpdateRequestDto;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ControlApiClient {

	private static final String CONTROL_GROUPS_PATH = "/api/v1/control-groups";

	private static final String CONTROLS_PATH = "/api/v1/controls";

	private final RestClient controlApiRestClient;

	public ControlApiClient(RestClient controlApiRestClient) {
		this.controlApiRestClient = controlApiRestClient;
	}

	public List<ControlApiControlGroupResponseDto> findAllControlGroups() {
		return call(() -> controlApiRestClient.get()
			.uri(CONTROL_GROUPS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<List<ControlApiControlGroupResponseDto>>() {
			}));
	}

	public List<ControlApiControlResponseDto> findAllControls() {
		return call(() -> controlApiRestClient.get()
			.uri(CONTROLS_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<List<ControlApiControlResponseDto>>() {
			}));
	}

	public UUID createControlGroup(ControlApiControlGroupRequestDto request) {
		return idFrom(create(CONTROL_GROUPS_PATH, request), "control group");
	}

	public void updateControlGroup(UUID id, ControlApiControlGroupUpdateRequestDto request) {
		update(CONTROL_GROUPS_PATH, id, request);
	}

	public void deleteControlGroup(UUID id) {
		delete(CONTROL_GROUPS_PATH, id);
	}

	public UUID createControl(ControlApiControlRequestDto request) {
		return idFrom(create(CONTROLS_PATH, request), "control");
	}

	public void updateControl(UUID id, ControlApiControlUpdateRequestDto request) {
		update(CONTROLS_PATH, id, request);
	}

	public void deleteControl(UUID id) {
		delete(CONTROLS_PATH, id);
	}

	private URI create(String path, Object request) {
		return call(() -> controlApiRestClient.post()
			.uri(path)
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getHeaders()
			.getLocation());
	}

	private void update(String path, UUID id, Object request) {
		call(() -> controlApiRestClient.put().uri(path + "/{id}", id).body(request).retrieve().toBodilessEntity());
	}

	private void delete(String path, UUID id) {
		call(() -> controlApiRestClient.delete().uri(path + "/{id}", id).retrieve().toBodilessEntity());
	}

	private static UUID idFrom(URI location, String resource) {
		if (location == null) {
			throw new RestClientException("The control API did not return the location of the new " + resource);
		}
		String path = location.getPath();
		try {
			return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
		}
		catch (IllegalArgumentException ex) {
			throw new RestClientException("The control API returned an invalid location: " + location, ex);
		}
	}

	private static <T> T call(Supplier<T> request) {
		try {
			return request.get();
		}
		catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				throw ex;
			}
			throw new RestClientException("The control API returned " + ex.getStatusCode().value(), ex);
		}
		catch (ResourceAccessException ex) {
			throw new RestClientException("The control API is unavailable", ex);
		}
	}

}
