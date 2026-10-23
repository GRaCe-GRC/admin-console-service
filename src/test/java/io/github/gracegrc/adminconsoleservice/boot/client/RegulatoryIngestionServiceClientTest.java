package io.github.gracegrc.adminconsoleservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.adminconsoleservice.boot.RegulatoryIngestionServiceConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@RestClientTest(RegulatoryIngestionServiceClient.class)
@Import(RegulatoryIngestionServiceConfig.class)
@TestPropertySource(properties = "regulatory-ingestion-service.base-url=http://ingestion.test")
class RegulatoryIngestionServiceClientTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final String AUTHORITY_JSON = "{\"id\":\"" + ID + "\",\"name\":\"EBA\",\"jurisdiction\":\"EU\","
			+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"enabled\":true,\"createdBy\":\"admin-console\"}";

	private static final RegulatoryIngestionAuthorityResponseDto AUTHORITY = new RegulatoryIngestionAuthorityResponseDto(ID, "EBA", "EU",
			Instant.parse("2026-10-01T10:15:30Z"), true, "admin-console");

	private static final RegulatoryIngestionAuthorityRequestDto REQUEST = new RegulatoryIngestionAuthorityRequestDto(
			"EBA", "EU", "admin-console");

	@Autowired
	private RegulatoryIngestionServiceClient client;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void createAuthorityPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"createdBy\":\"admin-console\"}"))
			.andRespond(withStatus(HttpStatus.CREATED).location(URI.create("http://ingestion.test/api/v1/authorities/" + ID)));

		assertThat(client.createAuthority(REQUEST)).isEqualTo(ID);
	}

	@Test
	void createAuthorityWithoutALocationThrows() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities")).andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> client.createAuthority(REQUEST)).isInstanceOf(RestClientException.class)
			.hasMessage("The regulatory ingestion service did not return the location of the new authority");
	}

	@Test
	void createAuthorityWithAnInvalidLocationThrows() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities"))
			.andRespond(withStatus(HttpStatus.CREATED).location(URI.create("http://ingestion.test/api/v1/authorities/x")));

		assertThatThrownBy(() -> client.createAuthority(REQUEST)).isInstanceOf(RestClientException.class)
			.hasMessageStartingWith("The regulatory ingestion service returned an invalid location");
	}

	@Test
	void findAllAuthoritiesReturnsTheList() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + AUTHORITY_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllAuthorities()).containsExactly(AUTHORITY);
	}

	@Test
	void findAuthorityByIdReturnsTheAuthority() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities/" + ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(AUTHORITY_JSON, MediaType.APPLICATION_JSON));

		assertThat(client.findAuthorityById(ID)).isEqualTo(AUTHORITY);
	}

	@Test
	void aClientErrorKeepsItsStatusAndBody() {
		String problem = "{\"status\":409,\"detail\":\"An authority named 'EBA' already exists\"}";
		server.expect(requestTo("http://ingestion.test/api/v1/authorities"))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.createAuthority(REQUEST))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void aServerErrorIsThrown() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities")).andRespond(withServerError());

		assertThatThrownBy(() -> client.findAllAuthorities()).isInstanceOf(HttpServerErrorException.class);
	}

}
