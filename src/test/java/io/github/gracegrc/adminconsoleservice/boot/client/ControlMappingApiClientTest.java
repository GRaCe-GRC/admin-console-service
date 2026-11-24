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

import io.github.gracegrc.adminconsoleservice.boot.ControlMappingApiConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingUpdateRequestDto;
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
import org.springframework.test.json.JsonCompareMode;
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

	private static final ControlMappingApiMappingRequestDto MAPPING_REQUEST = new ControlMappingApiMappingRequestDto(
			REQUIREMENT_ID, CONTROL_ID, "Covers it", "admin-console");

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

	@Test
	void createMappingPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo(URL))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"requirementId\":\"" + REQUIREMENT_ID + "\",\"controlId\":\"" + CONTROL_ID
					+ "\",\"rationale\":\"Covers it\",\"createdBy\":\"admin-console\"}", JsonCompareMode.STRICT))
			.andRespond(withStatus(HttpStatus.CREATED).location(URI.create(URL + "/" + MAPPING_ID)));

		assertThat(client.createMapping(MAPPING_REQUEST)).isEqualTo(MAPPING_ID);
	}

	@Test
	void createMappingWithoutALocationThrows() {
		server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> client.createMapping(MAPPING_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API did not return the location of the new mapping");
	}

	@Test
	void createMappingWithAnInvalidLocationThrows() {
		server.expect(requestTo(URL))
			.andRespond(withStatus(HttpStatus.CREATED).location(URI.create(URL + "/not-a-uuid")));

		assertThatThrownBy(() -> client.createMapping(MAPPING_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API returned an invalid location: " + URL + "/not-a-uuid");
	}

	@Test
	void createMappingForAnExistingPairKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"Requirement " + REQUIREMENT_ID + " is already mapped to control "
				+ CONTROL_ID + "\"}";
		server.expect(requestTo(URL))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.createMapping(MAPPING_REQUEST))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void createMappingWhenTheApiFailsThrowsItsStatus() {
		server.expect(requestTo(URL)).andRespond(withServerError());

		assertThatThrownBy(() -> client.createMapping(MAPPING_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API returned 500");
	}

	@Test
	void updateMappingPutsTheRationale() {
		server.expect(requestTo(URL + "/" + MAPPING_ID))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(content().json("{\"rationale\":\"Now covers it\"}", JsonCompareMode.STRICT))
			.andRespond(withSuccess("{\"id\":\"" + MAPPING_ID + "\",\"rationale\":\"Now covers it\"}",
					MediaType.APPLICATION_JSON));

		client.updateMapping(MAPPING_ID, new ControlMappingApiMappingUpdateRequestDto("Now covers it"));
	}

	@Test
	void updateMappingWithAnUnknownIdKeepsTheNotFound() {
		String problem = "{\"status\":404,\"detail\":\"Control mapping " + MAPPING_ID + " not found\"}";
		server.expect(requestTo(URL + "/" + MAPPING_ID))
			.andExpect(method(HttpMethod.PUT))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.updateMapping(MAPPING_ID, new ControlMappingApiMappingUpdateRequestDto(null)))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void updateMappingWhenTheApiIsUnreachableThrowsUnavailable() {
		server.expect(requestTo(URL + "/" + MAPPING_ID))
			.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> client.updateMapping(MAPPING_ID, new ControlMappingApiMappingUpdateRequestDto(null)))
			.isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API is unavailable");
	}

	@Test
	void deleteMappingSendsADeleteRequest() {
		server.expect(requestTo(URL + "/" + MAPPING_ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		client.deleteMapping(MAPPING_ID);
	}

	@Test
	void deleteMappingWithAnUnknownIdKeepsTheNotFound() {
		String problem = "{\"status\":404,\"detail\":\"Control mapping " + MAPPING_ID + " not found\"}";
		server.expect(requestTo(URL + "/" + MAPPING_ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.deleteMapping(MAPPING_ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void deleteMappingWhenTheApiFailsThrowsItsStatus() {
		server.expect(requestTo(URL + "/" + MAPPING_ID)).andRespond(withServerError());

		assertThatThrownBy(() -> client.deleteMapping(MAPPING_ID)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The control mapping API returned 500");
	}

}
