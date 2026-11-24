package io.github.gracegrc.adminconsoleservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.adminconsoleservice.boot.ControlApiConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlUpdateRequestDto;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
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

	private static final ControlApiControlGroupRequestDto GROUP_REQUEST = new ControlApiControlGroupRequestDto(
			PARENT_GROUP_ID, "AC-1", "Policy and Procedures", "admin-console");

	private static final ControlApiControlRequestDto CONTROL_REQUEST = new ControlApiControlRequestDto(GROUP_ID,
			PARENT_CONTROL_ID, "AC-2(1)", "Automated Account Management", "Support the management...",
			"admin-console");

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

	@Test
	void createControlGroupPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://control.test/api/v1/control-groups"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"parentGroupId\":\"" + PARENT_GROUP_ID + "\",\"groupRef\":\"AC-1\","
					+ "\"title\":\"Policy and Procedures\",\"createdBy\":\"admin-console\"}", true))
			.andRespond(withStatus(HttpStatus.CREATED)
				.location(URI.create("http://control.test/api/v1/control-groups/" + GROUP_ID)));

		assertThat(client.createControlGroup(GROUP_REQUEST)).isEqualTo(GROUP_ID);
	}

	@Test
	void createControlGroupWithoutALocationThrows() {
		server.expect(requestTo("http://control.test/api/v1/control-groups")).andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> client.createControlGroup(GROUP_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API did not return the location of the new control group");
	}

	@Test
	void createControlGroupWithAnInvalidLocationThrows() {
		server.expect(requestTo("http://control.test/api/v1/control-groups"))
			.andRespond(withStatus(HttpStatus.CREATED).location(URI.create("http://control.test/api/v1/control-groups/x")));

		assertThatThrownBy(() -> client.createControlGroup(GROUP_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API returned an invalid location: http://control.test/api/v1/control-groups/x");
	}

	@Test
	void createControlGroupWithADuplicateRefKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"A control group with ref 'AC-1' already exists\"}";
		server.expect(requestTo("http://control.test/api/v1/control-groups"))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.createControlGroup(GROUP_REQUEST))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void updateControlGroupPutsEveryField() {
		server.expect(requestTo("http://control.test/api/v1/control-groups/" + GROUP_ID))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(content().json("{\"parentGroupId\":null,\"groupRef\":\"AC\",\"title\":\"Access Control\"}",
					true))
			.andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		client.updateControlGroup(GROUP_ID, new ControlApiControlGroupUpdateRequestDto(null, "AC", "Access Control"));
	}

	@Test
	void updateControlGroupWithAServerErrorIsThrownAsAnInvalidResponse() {
		server.expect(requestTo("http://control.test/api/v1/control-groups/" + GROUP_ID)).andRespond(withServerError());

		assertThatThrownBy(() -> client.updateControlGroup(GROUP_ID,
				new ControlApiControlGroupUpdateRequestDto(null, "AC", "Access Control")))
			.isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API returned 500");
	}

	@Test
	void deleteControlGroupSendsADeleteRequest() {
		server.expect(requestTo("http://control.test/api/v1/control-groups/" + GROUP_ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		client.deleteControlGroup(GROUP_ID);
	}

	@Test
	void deleteControlGroupWithChildrenKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"Control group " + GROUP_ID
				+ " still has child groups or controls\"}";
		server.expect(requestTo("http://control.test/api/v1/control-groups/" + GROUP_ID))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.deleteControlGroup(GROUP_ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void deleteControlGroupWhenTheApiIsUnreachableIsThrownAsUnavailable() {
		server.expect(requestTo("http://control.test/api/v1/control-groups/" + GROUP_ID))
			.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> client.deleteControlGroup(GROUP_ID)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API is unavailable");
	}

	@Test
	void createControlPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://control.test/api/v1/controls"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"groupId\":\"" + GROUP_ID + "\",\"parentControlId\":\"" + PARENT_CONTROL_ID
					+ "\",\"controlRef\":\"AC-2(1)\",\"title\":\"Automated Account Management\","
					+ "\"statement\":\"Support the management...\",\"createdBy\":\"admin-console\"}", true))
			.andRespond(
					withStatus(HttpStatus.CREATED).location(URI.create("http://control.test/api/v1/controls/" + CONTROL_ID)));

		assertThat(client.createControl(CONTROL_REQUEST)).isEqualTo(CONTROL_ID);
	}

	@Test
	void createControlWithoutALocationThrows() {
		server.expect(requestTo("http://control.test/api/v1/controls")).andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> client.createControl(CONTROL_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API did not return the location of the new control");
	}

	@Test
	void createControlWithAnUnknownGroupKeepsTheNotFound() {
		String problem = "{\"status\":404,\"detail\":\"Control group " + GROUP_ID + " not found\"}";
		server.expect(requestTo("http://control.test/api/v1/controls"))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.createControl(CONTROL_REQUEST))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void createControlWhenTheApiIsUnreachableIsThrownAsUnavailable() {
		server.expect(requestTo("http://control.test/api/v1/controls"))
			.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> client.createControl(CONTROL_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API is unavailable");
	}

	@Test
	void updateControlPutsEveryFieldIncludingNulls() {
		server.expect(requestTo("http://control.test/api/v1/controls/" + CONTROL_ID))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(content().json("{\"groupId\":null,\"parentControlId\":null,\"controlRef\":\"AC-2\","
					+ "\"title\":\"Account Management\",\"statement\":null}", true))
			.andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		client.updateControl(CONTROL_ID,
				new ControlApiControlUpdateRequestDto(null, null, "AC-2", "Account Management", null));
	}

	@Test
	void updateControlWithAnUnknownIdKeepsTheNotFound() {
		String problem = "{\"status\":404,\"detail\":\"Control " + CONTROL_ID + " not found\"}";
		server.expect(requestTo("http://control.test/api/v1/controls/" + CONTROL_ID))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.updateControl(CONTROL_ID,
				new ControlApiControlUpdateRequestDto(null, null, "AC-2", "Account Management", null)))
			.isInstanceOfSatisfying(HttpClientErrorException.class,
					ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void deleteControlSendsADeleteRequest() {
		server.expect(requestTo("http://control.test/api/v1/controls/" + CONTROL_ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		client.deleteControl(CONTROL_ID);
	}

	@Test
	void deleteControlWithAServerErrorIsThrownAsAnInvalidResponse() {
		server.expect(requestTo("http://control.test/api/v1/controls/" + CONTROL_ID)).andRespond(withServerError());

		assertThatThrownBy(() -> client.deleteControl(CONTROL_ID)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control API returned 500");
	}

}
