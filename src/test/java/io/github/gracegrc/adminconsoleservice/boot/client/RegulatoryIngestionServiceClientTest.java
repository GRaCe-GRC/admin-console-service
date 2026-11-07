package io.github.gracegrc.adminconsoleservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.adminconsoleservice.boot.RegulatoryIngestionServiceConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
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

	private static final UUID FRAMEWORK_ID = UUID.fromString("8a1b2c3d-4e5f-4a6b-9c7d-0e1f2a3b4c5d");

	private static final UUID FILE_ID = UUID.fromString("9b2c3d4e-5f60-4b7c-8d9e-1f2a3b4c5d6e");

	private static final UUID REQUIREMENT_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID PARENT_ID = UUID.fromString("6e7f8091-2b3c-4d4e-9f50-6b7c8d9e0f1a");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final RegulatoryIngestionFrameworkRequestDto FRAMEWORK_REQUEST = new RegulatoryIngestionFrameworkRequestDto(
			ID, "Digital Operational Resilience Act", "DORA", "Regulation (EU) 2022/2554", "admin-console");

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

	@Test
	void deleteAuthoritySendsADeleteRequest() {
		server.expect(requestTo("http://ingestion.test/api/v1/authorities/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		client.deleteAuthority(ID);
	}

	@Test
	void deleteAuthorityWithAnUnknownIdKeepsTheNotFound() {
		String problem = "{\"status\":404,\"detail\":\"Authority " + ID + " not found\"}";
		server.expect(requestTo("http://ingestion.test/api/v1/authorities/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.deleteAuthority(ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void createFrameworkPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://ingestion.test/api/v1/frameworks"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"authorityId\":\"" + ID + "\",\"name\":\"Digital Operational Resilience Act\","
					+ "\"shortName\":\"DORA\",\"citation\":\"Regulation (EU) 2022/2554\",\"createdBy\":\"admin-console\"}"))
			.andRespond(withStatus(HttpStatus.CREATED)
				.location(URI.create("http://ingestion.test/api/v1/frameworks/" + FRAMEWORK_ID)));

		assertThat(client.createFramework(FRAMEWORK_REQUEST)).isEqualTo(FRAMEWORK_ID);
	}

	@Test
	void createFrameworkWithoutALocationThrows() {
		server.expect(requestTo("http://ingestion.test/api/v1/frameworks")).andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> client.createFramework(FRAMEWORK_REQUEST)).isInstanceOf(RestClientException.class)
			.hasMessage("The regulatory ingestion service did not return the location of the new framework");
	}

	@Test
	void findAllFrameworksReturnsEveryFramework() {
		server.expect(requestTo("http://ingestion.test/api/v1/frameworks"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[{\"id\":\"" + FRAMEWORK_ID + "\",\"authorityId\":\"" + ID + "\","
					+ "\"name\":\"Digital Operational Resilience Act\",\"shortName\":\"DORA\","
					+ "\"citation\":\"Regulation (EU) 2022/2554\",\"enabled\":true,"
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllFrameworks()).containsExactly(new RegulatoryIngestionFrameworkResponseDto(
				FRAMEWORK_ID, ID, "Digital Operational Resilience Act", "DORA", "Regulation (EU) 2022/2554", true,
				CREATED_AT, "admin-console"));
	}

	@Test
	void deleteFrameworkSendsADeleteRequest() {
		server.expect(requestTo("http://ingestion.test/api/v1/frameworks/" + FRAMEWORK_ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		client.deleteFramework(FRAMEWORK_ID);
	}

	@Test
	void deleteFrameworkWithFilesKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"Framework " + FRAMEWORK_ID + " has files. Delete them first.\"}";
		server.expect(requestTo("http://ingestion.test/api/v1/frameworks/" + FRAMEWORK_ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.deleteFramework(FRAMEWORK_ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void findAllFilesReturnsEveryFile() {
		server.expect(requestTo("http://ingestion.test/api/v1/files"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[{\"id\":\"" + FILE_ID + "\",\"frameworkId\":\"" + FRAMEWORK_ID + "\","
					+ "\"title\":\"DORA\",\"sourceUrl\":\"https://eur-lex.europa.eu/eli/reg/2022/2554/oj\","
					+ "\"versionLabel\":\"OJ L 333\",\"publicationDate\":\"2022-12-27\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllFiles()).containsExactly(new RegulatoryIngestionFileResponseDto(
				FILE_ID, FRAMEWORK_ID, "DORA", "https://eur-lex.europa.eu/eli/reg/2022/2554/oj", "OJ L 333",
				LocalDate.parse("2022-12-27"), CREATED_AT, "admin-console"));
	}

	@Test
	void findAllRequirementsReturnsEveryRequirement() {
		server.expect(requestTo("http://ingestion.test/api/v1/requirements"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[{\"id\":\"" + REQUIREMENT_ID + "\",\"fileId\":\"" + FILE_ID + "\","
					+ "\"parentRequirementId\":\"" + PARENT_ID + "\",\"provisionRef\":\"Art. 5(1)\","
					+ "\"heading\":\"Governance\",\"text\":\"Financial entities shall...\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}]", MediaType.APPLICATION_JSON));

		assertThat(client.findAllRequirements()).containsExactly(new RegulatoryIngestionRequirementResponseDto(
				REQUIREMENT_ID, FILE_ID, PARENT_ID, "Art. 5(1)", "Governance", "Financial entities shall...", CREATED_AT,
				"admin-console"));
	}
}
