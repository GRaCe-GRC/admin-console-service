package io.github.gracegrc.adminconsoleservice.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthority;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControl;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControlGroup;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardCoverage;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFile;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFramework;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedControl;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirement;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.service.DashboardService;
import io.github.gracegrc.adminconsoleservice.service.control.ControlMappingApiService;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryApiService;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryIngestionService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@WebMvcTest(AdminConsoleServiceController.class)
class AdminConsoleServiceControllerTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final String CREATE_BODY = "{\"name\":\"EBA\",\"jurisdiction\":\"EU\"}";

	private static final UUID FRAMEWORK_ID = UUID.fromString("8a1b2c3d-4e5f-4a6b-9c7d-0e1f2a3b4c5d");

	private static final String FRAMEWORK_BODY = "{\"authorityId\":\"" + ID + "\",\"name\":\"Digital Operational Resilience Act\",\"shortName\":\"DORA\","
			+ "\"citation\":\"Regulation (EU) 2022/2554\"}";

	private static final RegulatoryFrameworkRequestDto FRAMEWORK_REQUEST = new RegulatoryFrameworkRequestDto(ID,
			"Digital Operational Resilience Act", "DORA", "Regulation (EU) 2022/2554");

	private static final String AUTHORITY_UPDATE_BODY = "{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"enabled\":false}";

	private static final RegulatoryAuthorityUpdateRequestDto AUTHORITY_UPDATE_REQUEST = new RegulatoryAuthorityUpdateRequestDto(
			"EBA", "EU", false);

	private static final String FRAMEWORK_UPDATE_BODY = "{\"name\":\"Digital Operational Resilience Act\","
			+ "\"shortName\":\"DORA\",\"citation\":\"Regulation (EU) 2022/2554\",\"enabled\":true}";

	private static final RegulatoryFrameworkUpdateRequestDto FRAMEWORK_UPDATE_REQUEST = new RegulatoryFrameworkUpdateRequestDto(
			"Digital Operational Resilience Act", "DORA", "Regulation (EU) 2022/2554", true);

	private static final UUID FILE_ID = UUID.fromString("9b2c3d4e-5f60-4b7c-8d9e-1f2a3b4c5d6e");

	private static final String FILE_BODY = "{\"frameworkId\":\"" + FRAMEWORK_ID + "\",\"title\":\"DORA\","
			+ "\"sourceUrl\":\"https://example.org/dora\",\"versionLabel\":\"OJ L 333\",\"publicationDate\":\"2022-12-27\"}";

	private static final RegulatoryFileRequestDto FILE_REQUEST = new RegulatoryFileRequestDto(FRAMEWORK_ID, "DORA",
			"https://example.org/dora", "OJ L 333", LocalDate.parse("2022-12-27"));

	private static final String FILE_UPDATE_BODY = "{\"title\":\"DORA\",\"sourceUrl\":\"https://example.org/dora\","
			+ "\"versionLabel\":\"OJ L 333\",\"publicationDate\":\"2022-12-27\"}";

	private static final RegulatoryFileUpdateRequestDto FILE_UPDATE_REQUEST = new RegulatoryFileUpdateRequestDto("DORA",
			"https://example.org/dora", "OJ L 333", LocalDate.parse("2022-12-27"));

	private static final UUID REQUIREMENT_ID = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");

	private static final UUID CONTROL_ID = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");

	private static final UUID MAPPING_ID = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");

	private static final String MAPPING_BODY = "{\"requirementId\":\"" + REQUIREMENT_ID + "\",\"controlId\":\""
			+ CONTROL_ID + "\",\"rationale\":\"Covers it\"}";

	private static final ControlMappingRequestDto MAPPING_REQUEST = new ControlMappingRequestDto(REQUIREMENT_ID,
			CONTROL_ID, "Covers it");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private RegulatoryIngestionService regulatoryIngestionService;

	@MockitoBean
	private RegulatoryApiService regulatoryApiService;

	@MockitoBean
	private DashboardService dashboardService;

	@MockitoBean
	private ControlMappingApiService controlMappingApiService;

	@Test
	void getDashboardReturnsTheWholeDashboard() {
		UUID frameworkId = FRAMEWORK_ID;
		UUID fileId = UUID.fromString("9b2c3d4e-5f60-4b7c-8d9e-1f2a3b4c5d6e");
		UUID requirementId = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");
		UUID groupId = UUID.fromString("6e7f8091-2b3c-4d4e-9f50-6b7c8d9e0f1a");
		UUID controlId = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");
		UUID mappingId = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");
		Instant createdAt = Instant.parse("2026-10-01T10:15:30Z");
		DashboardControl control = new DashboardControl(controlId, "AC-2", "Account Management",
				null, createdAt, "me",
				List.of(new DashboardMappedRequirement(mappingId, requirementId, "Art. 5", "Governance", fileId,
						"DORA text", frameworkId, "DORA", ID, "EBA", "Covers it")),
				List.of());
		given(dashboardService.retrieveDashboard()).willReturn(new DashboardResponseDto(
				List.of(new DashboardAuthority(ID, "EBA", "EU", true, createdAt, "admin-console",
						List.of(new DashboardFramework(frameworkId, "DORA", "DORA", null, true, createdAt,
								"admin-console", new DashboardCoverage(1, 1),
								List.of(new DashboardFile(fileId, "DORA text", null, null,
										LocalDate.parse("2022-12-27"), createdAt, "admin-console",
										new DashboardCoverage(1, 1),
										List.of(new DashboardRequirement(requirementId, "Art. 5", "Governance",
												null, createdAt, "admin-console",
												List.of(new DashboardMappedControl(mappingId, controlId, "AC-2",
														"Account Management", "Covers it")),
												List.of())))))))),
				List.of(new DashboardControlGroup(groupId, "AC", "Access Control", createdAt, "me",
						List.of(control), List.of())),
				List.of()));

		assertThat(mvc.get().uri("/api/dashboard")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"authorities\":[{\"id\":\"" + ID + "\",\"name\":\"EBA\",\"frameworks\":[{"
					+ "\"id\":\"" + frameworkId + "\",\"coverage\":{\"mapped\":1,\"total\":1},\"files\":[{"
					+ "\"id\":\"" + fileId + "\",\"publicationDate\":\"2022-12-27\",\"coverage\":{\"mapped\":1,\"total\":1},"
					+ "\"requirements\":[{\"id\":\"" + requirementId + "\",\"provisionRef\":\"Art. 5\","
					+ "\"mappedControls\":[{\"mappingId\":\"" + mappingId + "\",\"controlId\":\"" + controlId + "\","
					+ "\"controlRef\":\"AC-2\",\"title\":\"Account Management\",\"rationale\":\"Covers it\"}],"
					+ "\"children\":[]}]}]}]}],"
					+ "\"controlGroups\":[{\"id\":\"" + groupId + "\",\"groupRef\":\"AC\",\"controls\":[{"
					+ "\"id\":\"" + controlId + "\",\"controlRef\":\"AC-2\",\"mappedRequirements\":[{"
					+ "\"mappingId\":\"" + mappingId + "\",\"requirementId\":\"" + requirementId + "\","
					+ "\"fileTitle\":\"DORA text\",\"frameworkName\":\"DORA\",\"authorityName\":\"EBA\"}],"
					+ "\"enhancements\":[]}],\"children\":[]}],"
					+ "\"ungroupedControls\":[]}");
	}

	@Test
	void getDashboardWhenAnApiIsUnavailableReturnsBadGateway() {
		given(dashboardService.retrieveDashboard())
			.willThrow(new CompletionException(new RestClientException("The control API is unavailable")));

		assertThat(mvc.get().uri("/api/dashboard")).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The control API is unavailable\"}");
	}

	@Test
	void getDashboardWhenAnApiReturnsAClientErrorReturnsItUnchanged() {
		String problem = "{\"title\":\"Bad Request\",\"status\":400}";
		given(dashboardService.retrieveDashboard())
			.willThrow(new CompletionException(clientError(HttpStatus.BAD_REQUEST, "Bad Request", problem)));

		assertThat(mvc.get().uri("/api/dashboard")).hasStatus(HttpStatus.BAD_REQUEST).bodyText().isEqualTo(problem);
	}

	@Test
	void postCreatesTheAuthorityAndReturnsAnEmptyBody() {
		given(regulatoryIngestionService.createAuthority(new RegulatoryAuthorityRequestDto("EBA", "EU"))).willReturn(ID);

		assertThat(mvc.post().uri("/api/dashboard/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/dashboard/authorities/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/dashboard/authorities")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"name\":\"must not be blank\","
					+ "\"jurisdiction\":\"must not be blank\"}}");
		then(regulatoryIngestionService).shouldHaveNoInteractions();
	}

	@Test
	void aClientErrorFromTheIngestionServiceIsReturnedUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"An authority named 'EBA' already exists\"}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		given(regulatoryIngestionService.createAuthority(any())).willThrow(HttpClientErrorException.create(
				HttpStatus.CONFLICT, "Conflict", headers, problem.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

		assertThat(mvc.post().uri("/api/dashboard/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void aClientErrorWithoutHeadersIsReturnedWithItsStatusAndBody() {
		willThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null,
				"Not Found".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
			.given(regulatoryIngestionService)
			.deleteAuthority(ID);

		assertThat(mvc.delete().uri("/api/dashboard/authorities/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyText()
			.isEqualTo("Not Found");
	}

	@Test
	void aServerErrorFromTheIngestionServiceReturnsBadGateway() {
		given(regulatoryIngestionService.createAuthority(any())).willThrow(HttpServerErrorException.create(
				HttpStatus.BAD_GATEWAY, "Bad Gateway", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

		assertThat(mvc.post().uri("/api/dashboard/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory ingestion service returned 502\"}");
	}

	@Test
	void anInvalidResponseFromTheIngestionServiceReturnsBadGateway() {
		given(regulatoryIngestionService.createAuthority(any())).willThrow(
				new RestClientException("The regulatory ingestion service did not return the location of the new authority"));

		assertThat(mvc.post().uri("/api/dashboard/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,"
					+ "\"detail\":\"The regulatory ingestion service did not return the location of the new authority\"}");
	}

	@Test
	void anUnreachableIngestionServiceReturnsBadGateway() {
		given(regulatoryIngestionService.createAuthority(any())).willThrow(new ResourceAccessException("Connection refused"));

		assertThat(mvc.post().uri("/api/dashboard/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory ingestion service is unavailable\"}");
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/dashboard/authorities/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT).body().isEmpty();
		then(regulatoryIngestionService).should().deleteAuthority(ID);
	}

	@Test
	void deleteWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Authority " + ID + " not found\"}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		willThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", headers,
				problem.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
			.given(regulatoryIngestionService)
			.deleteAuthority(ID);

		assertThat(mvc.delete().uri("/api/dashboard/authorities/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void deleteWhenTheIngestionServiceIsUnreachableReturnsBadGateway() {
		willThrow(new ResourceAccessException("Connection refused")).given(regulatoryIngestionService).deleteAuthority(ID);

		assertThat(mvc.delete().uri("/api/dashboard/authorities/{id}", ID)).hasStatus(HttpStatus.BAD_GATEWAY);
	}

	@Test
	void deleteWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.delete().uri("/api/dashboard/authorities/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryIngestionService).shouldHaveNoInteractions();
	}

	@Test
	void postFrameworkCreatesItAndReturnsItsLocation() {
		given(regulatoryIngestionService.createFramework(FRAMEWORK_REQUEST)).willReturn(FRAMEWORK_ID);

		assertThat(mvc.post()
			.uri("/api/dashboard/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/dashboard/frameworks/" + FRAMEWORK_ID)
			.body()
			.isEmpty();
	}

	@Test
	void postFrameworkWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/dashboard/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\" \",\"shortName\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"authorityId\":\"must not be null\",\"name\":\"must not be blank\","
					+ "\"shortName\":\"size must be between 0 and 255\"}}");
		then(regulatoryIngestionService).shouldHaveNoInteractions();
	}

	@Test
	void postFrameworkWithADuplicateNameReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,"
				+ "\"detail\":\"A framework named 'Digital Operational Resilience Act' already exists\"}";
		given(regulatoryIngestionService.createFramework(FRAMEWORK_REQUEST)).willThrow(conflict(problem));

		assertThat(mvc.post()
			.uri("/api/dashboard/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void postFrameworkForAnUnknownAuthorityReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Authority " + ID + " not found\"}";
		given(regulatoryIngestionService.createFramework(FRAMEWORK_REQUEST)).willThrow(notFound(problem));

		assertThat(mvc.post()
			.uri("/api/dashboard/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void postFrameworkWhenTheIngestionServiceIsUnreachableReturnsBadGateway() {
		given(regulatoryIngestionService.createFramework(FRAMEWORK_REQUEST))
			.willThrow(new ResourceAccessException("Connection refused"));

		assertThat(mvc.post()
			.uri("/api/dashboard/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory ingestion service is unavailable\"}");
	}

	@Test
	void deleteFrameworkReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)).hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(regulatoryIngestionService).should().deleteFramework(FRAMEWORK_ID);
	}

	@Test
	void deleteFrameworkWithFilesReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"Framework " + FRAMEWORK_ID
				+ " has files. Delete them first.\"}";
		willThrow(conflict(problem)).given(regulatoryIngestionService).deleteFramework(FRAMEWORK_ID);

		assertThat(mvc.delete().uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)).hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void deleteFrameworkWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Framework " + FRAMEWORK_ID
				+ " not found\"}";
		willThrow(notFound(problem)).given(regulatoryIngestionService).deleteFramework(FRAMEWORK_ID);

		assertThat(mvc.delete().uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void deleteFrameworkWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.delete().uri("/api/dashboard/frameworks/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryIngestionService).shouldHaveNoInteractions();
	}

	@Test
	void putAuthorityUpdatesItAndReturnsNoContent() {
		assertThat(mvc.put()
			.uri("/api/dashboard/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(AUTHORITY_UPDATE_BODY))
			.hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(regulatoryApiService).should().updateAuthority(ID, AUTHORITY_UPDATE_REQUEST);
	}

	@Test
	void putAuthorityWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.put()
			.uri("/api/dashboard/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\" \",\"jurisdiction\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"name\":\"must not be blank\","
					+ "\"jurisdiction\":\"size must be between 0 and 255\",\"enabled\":\"must not be null\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void putAuthorityWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/dashboard/authorities/not-a-uuid")
			.contentType(MediaType.APPLICATION_JSON)
			.content(AUTHORITY_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void putAuthorityWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Authority " + ID + " not found\"}";
		willThrow(notFound(problem)).given(regulatoryApiService).updateAuthority(ID, AUTHORITY_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(AUTHORITY_UPDATE_BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void putAuthorityWithADuplicateNameReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"An authority named 'EBA' already exists\"}";
		willThrow(conflict(problem)).given(regulatoryApiService).updateAuthority(ID, AUTHORITY_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(AUTHORITY_UPDATE_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void putAuthorityWhenTheRegulatoryApiIsUnreachableReturnsBadGateway() {
		willThrow(new RestClientException("The regulatory API is unavailable")).given(regulatoryApiService)
			.updateAuthority(ID, AUTHORITY_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(AUTHORITY_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory API is unavailable\"}");
	}

	@Test
	void putFrameworkUpdatesItAndReturnsNoContent() {
		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_UPDATE_BODY))
			.hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(regulatoryApiService).should().updateFramework(FRAMEWORK_ID, FRAMEWORK_UPDATE_REQUEST);
	}

	@Test
	void putFrameworkWithoutTheOptionalFieldsClearsThem() {
		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"DORA\",\"enabled\":true}"))
			.hasStatus(HttpStatus.NO_CONTENT);
		then(regulatoryApiService).should()
			.updateFramework(FRAMEWORK_ID, new RegulatoryFrameworkUpdateRequestDto("DORA", null, null, true));
	}

	@Test
	void putFrameworkWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\",\"citation\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"name\":\"must not be blank\","
					+ "\"citation\":\"size must be between 0 and 255\",\"enabled\":\"must not be null\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void putFrameworkWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/not-a-uuid")
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void putFrameworkWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Framework " + FRAMEWORK_ID
				+ " not found\"}";
		willThrow(notFound(problem)).given(regulatoryApiService).updateFramework(FRAMEWORK_ID, FRAMEWORK_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_UPDATE_BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void putFrameworkWithADuplicateNameReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,"
				+ "\"detail\":\"A framework named 'Digital Operational Resilience Act' already exists\"}";
		willThrow(conflict(problem)).given(regulatoryApiService).updateFramework(FRAMEWORK_ID, FRAMEWORK_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_UPDATE_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void putFrameworkWhenTheRegulatoryApiFailsReturnsBadGateway() {
		willThrow(new RestClientException("The regulatory API returned 500")).given(regulatoryApiService)
			.updateFramework(FRAMEWORK_ID, FRAMEWORK_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/frameworks/{id}", FRAMEWORK_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FRAMEWORK_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory API returned 500\"}");
	}

	@Test
	void postFileCreatesItAndReturnsItsLocation() {
		given(regulatoryIngestionService.createFile(FILE_REQUEST)).willReturn(FILE_ID);

		assertThat(mvc.post().uri("/api/dashboard/files").contentType(MediaType.APPLICATION_JSON).content(FILE_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/dashboard/files/" + FILE_ID)
			.body()
			.isEmpty();
	}

	@Test
	void postFileWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/dashboard/files")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\" \",\"versionLabel\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"frameworkId\":\"must not be null\","
					+ "\"title\":\"must not be blank\",\"versionLabel\":\"size must be between 0 and 255\"}}");
		then(regulatoryIngestionService).shouldHaveNoInteractions();
	}

	@Test
	void postFileForAnUnknownFrameworkReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Framework " + FRAMEWORK_ID
				+ " not found\"}";
		given(regulatoryIngestionService.createFile(FILE_REQUEST)).willThrow(notFound(problem));

		assertThat(mvc.post().uri("/api/dashboard/files").contentType(MediaType.APPLICATION_JSON).content(FILE_BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void postFileWhenTheIngestionServiceIsUnreachableReturnsBadGateway() {
		given(regulatoryIngestionService.createFile(FILE_REQUEST))
			.willThrow(new ResourceAccessException("Connection refused"));

		assertThat(mvc.post().uri("/api/dashboard/files").contentType(MediaType.APPLICATION_JSON).content(FILE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory ingestion service is unavailable\"}");
	}

	@Test
	void putFileUpdatesItAndReturnsNoContent() {
		assertThat(mvc.put()
			.uri("/api/dashboard/files/{id}", FILE_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FILE_UPDATE_BODY))
			.hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(regulatoryApiService).should().updateFile(FILE_ID, FILE_UPDATE_REQUEST);
	}

	@Test
	void putFileWithOnlyATitleClearsTheOptionalFields() {
		assertThat(mvc.put()
			.uri("/api/dashboard/files/{id}", FILE_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"DORA\"}"))
			.hasStatus(HttpStatus.NO_CONTENT);
		then(regulatoryApiService).should()
			.updateFile(FILE_ID, new RegulatoryFileUpdateRequestDto("DORA", null, null, null));
	}

	@Test
	void putFileWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.put()
			.uri("/api/dashboard/files/{id}", FILE_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"versionLabel\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"title\":\"must not be blank\","
					+ "\"versionLabel\":\"size must be between 0 and 255\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void putFileWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/dashboard/files/not-a-uuid")
			.contentType(MediaType.APPLICATION_JSON)
			.content(FILE_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void putFileWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"File " + FILE_ID + " not found\"}";
		willThrow(notFound(problem)).given(regulatoryApiService).updateFile(FILE_ID, FILE_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/files/{id}", FILE_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FILE_UPDATE_BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void putFileWhenTheRegulatoryApiFailsReturnsBadGateway() {
		willThrow(new RestClientException("The regulatory API returned 500")).given(regulatoryApiService)
			.updateFile(FILE_ID, FILE_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/files/{id}", FILE_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FILE_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory API returned 500\"}");
	}

	@Test
	void putFileWhenTheRegulatoryApiIsUnreachableReturnsBadGateway() {
		willThrow(new RestClientException("The regulatory API is unavailable")).given(regulatoryApiService)
			.updateFile(FILE_ID, FILE_UPDATE_REQUEST);

		assertThat(mvc.put()
			.uri("/api/dashboard/files/{id}", FILE_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content(FILE_UPDATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The regulatory API is unavailable\"}");
	}

	@Test
	void deleteFileReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/dashboard/files/{id}", FILE_ID)).hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(regulatoryIngestionService).should().deleteFile(FILE_ID);
	}

	@Test
	void deleteFileWithRequirementsReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"File " + FILE_ID
				+ " has requirements. Delete them first.\"}";
		willThrow(conflict(problem)).given(regulatoryIngestionService).deleteFile(FILE_ID);

		assertThat(mvc.delete().uri("/api/dashboard/files/{id}", FILE_ID)).hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void deleteFileWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.delete().uri("/api/dashboard/files/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryIngestionService).shouldHaveNoInteractions();
	}

	@Test
	void postMappingCreatesItAndReturnsItsLocation() {
		given(controlMappingApiService.createMapping(MAPPING_REQUEST)).willReturn(MAPPING_ID);

		assertThat(mvc.post().uri("/api/dashboard/mappings").contentType(MediaType.APPLICATION_JSON).content(MAPPING_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/dashboard/mappings/" + MAPPING_ID)
			.body()
			.isEmpty();
	}

	@Test
	void postMappingWithoutARationaleCreatesIt() {
		given(controlMappingApiService.createMapping(new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, null)))
			.willReturn(MAPPING_ID);

		assertThat(mvc.post()
			.uri("/api/dashboard/mappings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"requirementId\":\"" + REQUIREMENT_ID + "\",\"controlId\":\"" + CONTROL_ID + "\"}"))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/dashboard/mappings/" + MAPPING_ID);
	}

	@Test
	void postMappingWithMissingIdsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post().uri("/api/dashboard/mappings").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"requirementId\":\"must not be null\","
					+ "\"controlId\":\"must not be null\"}}");
		then(controlMappingApiService).shouldHaveNoInteractions();
	}

	@Test
	void postMappingWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.post()
			.uri("/api/dashboard/mappings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"requirementId\":\"not-a-uuid\",\"controlId\":\"" + CONTROL_ID + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(controlMappingApiService).shouldHaveNoInteractions();
	}

	@Test
	void postMappingForAnExistingPairReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"Requirement " + REQUIREMENT_ID
				+ " is already mapped to control " + CONTROL_ID + "\"}";
		given(controlMappingApiService.createMapping(MAPPING_REQUEST)).willThrow(conflict(problem));

		assertThat(mvc.post().uri("/api/dashboard/mappings").contentType(MediaType.APPLICATION_JSON).content(MAPPING_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void postMappingWhenTheControlMappingApiFailsReturnsBadGateway() {
		given(controlMappingApiService.createMapping(MAPPING_REQUEST))
			.willThrow(new RestClientException("The control mapping API returned 500"));

		assertThat(mvc.post().uri("/api/dashboard/mappings").contentType(MediaType.APPLICATION_JSON).content(MAPPING_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The control mapping API returned 500\"}");
	}

	@Test
	void putMappingUpdatesTheRationaleAndReturnsNoContent() {
		assertThat(mvc.put()
			.uri("/api/dashboard/mappings/{id}", MAPPING_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"rationale\":\"Now covers it\"}"))
			.hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(controlMappingApiService).should()
			.updateMapping(MAPPING_ID, new ControlMappingUpdateRequestDto("Now covers it"));
	}

	@Test
	void putMappingWithoutABodyReturnsBadRequest() {
		assertThat(mvc.put().uri("/api/dashboard/mappings/{id}", MAPPING_ID).contentType(MediaType.APPLICATION_JSON))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(controlMappingApiService).shouldHaveNoInteractions();
	}

	@Test
	void putMappingWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Control mapping " + MAPPING_ID
				+ " not found\"}";
		willThrow(notFound(problem)).given(controlMappingApiService)
			.updateMapping(MAPPING_ID, new ControlMappingUpdateRequestDto(null));

		assertThat(mvc.put()
			.uri("/api/dashboard/mappings/{id}", MAPPING_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{}"))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void putMappingWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/dashboard/mappings/not-a-uuid")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"rationale\":\"Now covers it\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(controlMappingApiService).shouldHaveNoInteractions();
	}

	@Test
	void deleteMappingReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/dashboard/mappings/{id}", MAPPING_ID)).hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(controlMappingApiService).should().deleteMapping(MAPPING_ID);
	}

	@Test
	void deleteMappingWithAnUnknownIdReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Control mapping " + MAPPING_ID
				+ " not found\"}";
		willThrow(notFound(problem)).given(controlMappingApiService).deleteMapping(MAPPING_ID);

		assertThat(mvc.delete().uri("/api/dashboard/mappings/{id}", MAPPING_ID)).hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void deleteMappingWhenTheControlMappingApiIsUnreachableReturnsBadGateway() {
		willThrow(new RestClientException("The control mapping API is unavailable")).given(controlMappingApiService)
			.deleteMapping(MAPPING_ID);

		assertThat(mvc.delete().uri("/api/dashboard/mappings/{id}", MAPPING_ID)).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The control mapping API is unavailable\"}");
	}

	@Test
	void deleteMappingWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.delete().uri("/api/dashboard/mappings/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(controlMappingApiService).shouldHaveNoInteractions();
	}

	private static HttpClientErrorException notFound(String problem) {
		return clientError(HttpStatus.NOT_FOUND, "Not Found", problem);
	}

	private static HttpClientErrorException conflict(String problem) {
		return clientError(HttpStatus.CONFLICT, "Conflict", problem);
	}

	private static HttpClientErrorException clientError(HttpStatus status, String statusText, String problem) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		return HttpClientErrorException.create(status, statusText, headers, problem.getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8);
	}
}
