package io.github.gracegrc.adminconsoleservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.adminconsoleservice.boot.ControlApiConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
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

@RestClientTest(ControlApiClient.class)
@Import(ControlApiConfig.class)
@TestPropertySource(properties = "control-api.base-url=http://control.test")
class ControlApiClientTest {

	private static final UUID GROUP_ID = UUID.fromString("6e7f8091-2b3c-4d4e-9f50-6b7c8d9e0f1a");

	private static final UUID PARENT_GROUP_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID CONTROL_ID = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");

	private static final UUID PARENT_CONTROL_ID = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Autowired
	private ControlApiClient client;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void findAllControlGroupsReturnsEveryGroup() {
		server.expect(requestTo("http://control.test/api/v1/control-groups"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[{\"id\":\"" + GROUP_ID + "\",\"parentGroupId\":\"" + PARENT_GROUP_ID + "\","
					+ "\"groupRef\":\"AC\",\"title\":\"Access Control\",\"createdAt\":\"2026-10-01T10:15:30Z\","
					+ "\"createdBy\":\"me\"}]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllControlGroups()).containsExactly(
				new ControlApiControlGroupResponseDto(GROUP_ID, PARENT_GROUP_ID, "AC", "Access Control", CREATED_AT, "me"));
	}

	@Test
	void findAllControlsReturnsEveryControl() {
		server.expect(requestTo("http://control.test/api/v1/controls"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[{\"id\":\"" + CONTROL_ID + "\",\"groupId\":\"" + GROUP_ID + "\","
					+ "\"parentControlId\":\"" + PARENT_CONTROL_ID + "\",\"controlRef\":\"AC-2(1)\","
					+ "\"title\":\"Automated Account Management\",\"statement\":\"Support the management...\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"me\"}]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllControls()).containsExactly(new ControlApiControlResponseDto(CONTROL_ID, GROUP_ID,
				PARENT_CONTROL_ID, "AC-2(1)", "Automated Account Management", "Support the management...", CREATED_AT,
				"me"));
	}

	@Test
	void aClientErrorKeepsItsStatusAndBody() {
		String problem = "{\"status\":404,\"detail\":\"Not found\"}";
		server.expect(requestTo("http://control.test/api/v1/controls"))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.findAllControls()).isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
			assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
		});
	}

	@Test
	void aServerErrorIsThrownAsAnInvalidResponseOfTheControlApi() {
		server.expect(requestTo("http://control.test/api/v1/control-groups")).andRespond(withServerError());

		assertThatThrownBy(() -> client.findAllControlGroups()).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API returned 500");
	}

	@Test
	void anUnreachableApiIsThrownAsUnavailable() {
		server.expect(requestTo("http://control.test/api/v1/controls"))
			.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> client.findAllControls()).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API is unavailable")
			.hasRootCauseInstanceOf(IOException.class);
	}

}
