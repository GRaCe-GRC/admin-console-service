package io.github.gracegrc.adminconsoleservice.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardControlResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardCoverageResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFileResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedControlResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardMappedRequirementResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardRequirementResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.DashboardResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.service.DashboardService;
import io.github.gracegrc.adminconsoleservice.service.regulatory.RegulatoryIngestionService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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

	private static final RegulatoryAuthorityResponseDto AUTHORITY = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
			Instant.parse("2026-10-01T10:15:30Z"), true, "admin-console");

	private static final String CREATE_BODY = "{\"name\":\"EBA\",\"jurisdiction\":\"EU\"}";

	private static final UUID FRAMEWORK_ID = UUID.fromString("8a1b2c3d-4e5f-4a6b-9c7d-0e1f2a3b4c5d");

	private static final String FRAMEWORK_BODY = "{\"authorityId\":\"" + ID + "\",\"name\":\"Digital Operational Resilience Act\",\"shortName\":\"DORA\","
			+ "\"citation\":\"Regulation (EU) 2022/2554\"}";

	private static final RegulatoryFrameworkRequestDto FRAMEWORK_REQUEST = new RegulatoryFrameworkRequestDto(ID,
			"Digital Operational Resilience Act", "DORA", "Regulation (EU) 2022/2554");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private RegulatoryIngestionService regulatoryIngestionService;

	@MockitoBean
	private DashboardService dashboardService;

	@Test
	void getReturnsTheDashboardList() {
		given(regulatoryIngestionService.findAllAuthorities()).willReturn(List.of(AUTHORITY));

		assertThat(mvc.get().uri("/api/dashboard/authorities")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"name\":\"EBA\",\"jurisdiction\":\"EU\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"enabled\":true,\"createdBy\":\"admin-console\"}]");
	}

	@Test
	void getDashboardReturnsTheWholeDashboard() {
		UUID frameworkId = FRAMEWORK_ID;
		UUID fileId = UUID.fromString("9b2c3d4e-5f60-4b7c-8d9e-1f2a3b4c5d6e");
		UUID requirementId = UUID.fromString("5d6e7f80-1a2b-4c3d-8e4f-5a6b7c8d9e0f");
		UUID groupId = UUID.fromString("6e7f8091-2b3c-4d4e-9f50-6b7c8d9e0f1a");
		UUID controlId = UUID.fromString("80910213-4d5e-4f60-b172-8d9e0f1a2b3c");
		UUID mappingId = UUID.fromString("7f809102-3c4d-4e5f-a061-7c8d9e0f1a2b");
		Instant createdAt = Instant.parse("2026-10-01T10:15:30Z");
		DashboardControlResponseDto control = new DashboardControlResponseDto(controlId, "AC-2", "Account Management",
				null, createdAt, "me",
				List.of(new DashboardMappedRequirementResponseDto(mappingId, requirementId, "Art. 5", "Governance", fileId,
						"DORA text", frameworkId, "DORA", ID, "EBA", "Covers it")),
				List.of());
		given(dashboardService.findDashboard()).willReturn(new DashboardResponseDto(
				List.of(new DashboardAuthorityResponseDto(ID, "EBA", "EU", true, createdAt, "admin-console",
						List.of(new DashboardFrameworkResponseDto(frameworkId, "DORA", "DORA", null, true, createdAt,
								"admin-console", new DashboardCoverageResponseDto(1, 1),
								List.of(new DashboardFileResponseDto(fileId, "DORA text", null, null,
										LocalDate.parse("2022-12-27"), createdAt, "admin-console",
										new DashboardCoverageResponseDto(1, 1),
										List.of(new DashboardRequirementResponseDto(requirementId, "Art. 5", "Governance",
												null, createdAt, "admin-console",
												List.of(new DashboardMappedControlResponseDto(mappingId, controlId, "AC-2",
														"Account Management", "Covers it")),
												List.of())))))))),
				List.of(new DashboardControlGroupResponseDto(groupId, "AC", "Access Control", createdAt, "me",
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
		given(dashboardService.findDashboard()).willThrow(new RestClientException("The control API is unavailable"));

		assertThat(mvc.get().uri("/api/dashboard")).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The control API is unavailable\"}");
	}

	@Test
	void getDashboardWhenAnApiReturnsAClientErrorReturnsItUnchanged() {
		String problem = "{\"title\":\"Bad Request\",\"status\":400}";
		given(dashboardService.findDashboard()).willThrow(clientError(HttpStatus.BAD_REQUEST, "Bad Request", problem));

		assertThat(mvc.get().uri("/api/dashboard")).hasStatus(HttpStatus.BAD_REQUEST).bodyText().isEqualTo(problem);
	}

	@Test
	void getByIdReturnsTheAuthority() {
		given(regulatoryIngestionService.findAuthorityById(ID)).willReturn(AUTHORITY);

		assertThat(mvc.get().uri("/api/dashboard/authorities/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"name\":\"EBA\"}");
	}

	@Test
	void getByIdWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/dashboard/authorities/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryIngestionService).shouldHaveNoInteractions();
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
		given(regulatoryIngestionService.findAuthorityById(ID)).willThrow(HttpClientErrorException.create(
				HttpStatus.NOT_FOUND, "Not Found", null, "Not Found".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

		assertThat(mvc.get().uri("/api/dashboard/authorities/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyText()
			.isEqualTo("Not Found");
	}

	@Test
	void aServerErrorFromTheIngestionServiceReturnsBadGateway() {
		given(regulatoryIngestionService.findAllAuthorities()).willThrow(HttpServerErrorException.create(
				HttpStatus.BAD_GATEWAY, "Bad Gateway", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

		assertThat(mvc.get().uri("/api/dashboard/authorities")).hasStatus(HttpStatus.BAD_GATEWAY)
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
		given(regulatoryIngestionService.findAllAuthorities()).willThrow(new ResourceAccessException("Connection refused"));

		assertThat(mvc.get().uri("/api/dashboard/authorities")).hasStatus(HttpStatus.BAD_GATEWAY)
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
