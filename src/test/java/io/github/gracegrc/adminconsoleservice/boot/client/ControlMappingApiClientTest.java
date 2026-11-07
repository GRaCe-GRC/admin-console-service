package io.github.gracegrc.adminconsoleservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.adminconsoleservice.boot.ControlMappingApiConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@RestClientTest(ControlMappingApiClient.class)
@Import(ControlMappingApiConfig.class)
@TestPropertySource(properties = "control-mapping-api.base-url=http://control-mapping.test")
class ControlMappingApiClientTest {

	private static final String URL = "http://control-mapping.test/api/v1/control-mappings";

	private static final UUID REQUIREMENT_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID MAPPING_ID = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");

	private static final UUID CONTROL_ID = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");

	@Autowired
	private ControlMappingApiClient client;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void findAllMappingsReturnsEveryMapping() {
		server.expect(requestTo(URL))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[{\"id\":\"" + MAPPING_ID + "\",\"requirementId\":\"" + REQUIREMENT_ID
					+ "\",\"controlId\":\"" + CONTROL_ID + "\",\"rationale\":\"Covers it\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"me\"}]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllMappings()).containsExactly(new ControlMappingApiMappingResponseDto(MAPPING_ID,
				REQUIREMENT_ID, CONTROL_ID, "Covers it", Instant.parse("2026-10-01T10:15:30Z"), "me"));
	}

	@Test
	void aClientErrorKeepsItsStatusAndBody() {
		String problem = "{\"status\":400,\"detail\":\"Bad request\"}";
		server.expect(requestTo(URL))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.findAllMappings()).isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
			assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
		});
	}

	@Test
	void aServerErrorIsThrownAsAnInvalidResponseOfTheControlMappingApi() {
		server.expect(requestTo(URL)).andRespond(withServerError());

		assertThatThrownBy(() -> client.findAllMappings()).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API returned 500");
	}

	@Test
	void anUnreachableApiIsThrownAsUnavailable() {
		server.expect(requestTo(URL)).andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> client.findAllMappings()).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API is unavailable")
			.hasRootCauseInstanceOf(IOException.class);
	}

}
