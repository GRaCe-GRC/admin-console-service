package io.github.gracegrc.adminconsoleservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.adminconsoleservice.boot.RegulatoryApiConfig;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFileUpdateRequestDto;
import java.io.IOException;
import java.net.ConnectException;
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
import org.springframework.web.client.RestClientException;

@RestClientTest(RegulatoryApiClient.class)
@Import(RegulatoryApiConfig.class)
@TestPropertySource(properties = "regulatory-api.base-url=http://regulatory.test")
class RegulatoryApiClientTest {

	private static final UUID FILE_ID = UUID.fromString("9b2c3d4e-5f60-4b7c-8d9e-1f2a3b4c5d6e");

	private static final RegulatoryApiFileUpdateRequestDto FILE_REQUEST = new RegulatoryApiFileUpdateRequestDto(
			"DORA", "https://eur-lex.europa.eu/eli/reg/2022/2554/oj", "OJ L 333", LocalDate.parse("2022-12-27"));

	@Autowired
	private RegulatoryApiClient client;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void updateFilePutsEveryField() {
		server.expect(requestTo("http://regulatory.test/api/v1/files/" + FILE_ID))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(content().json("{\"title\":\"DORA\",\"sourceUrl\":\"https://eur-lex.europa.eu/eli/reg/2022/2554/oj\","
					+ "\"versionLabel\":\"OJ L 333\",\"publicationDate\":\"2022-12-27\"}", true))
			.andRespond(withSuccess("{\"id\":\"" + FILE_ID + "\",\"title\":\"DORA\"}", MediaType.APPLICATION_JSON));

		client.updateFile(FILE_ID, FILE_REQUEST);
	}

	@Test
	void updateFileSendsTheClearedFieldsAsNull() {
		server.expect(requestTo("http://regulatory.test/api/v1/files/" + FILE_ID))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(content().json("{\"title\":\"DORA\",\"sourceUrl\":null,\"versionLabel\":null,"
					+ "\"publicationDate\":null}", true))
			.andRespond(withSuccess());

		client.updateFile(FILE_ID, new RegulatoryApiFileUpdateRequestDto("DORA", null, null, null));
	}

	@Test
	void aClientErrorKeepsItsStatusAndBody() {
		String problem = "{\"status\":404,\"detail\":\"File " + FILE_ID + " not found\"}";
		server.expect(requestTo("http://regulatory.test/api/v1/files/" + FILE_ID))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> client.updateFile(FILE_ID, FILE_REQUEST))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void aServerErrorIsThrownAsAnInvalidResponseOfTheRegulatoryApi() {
		server.expect(requestTo("http://regulatory.test/api/v1/files/" + FILE_ID)).andRespond(withServerError());

		assertThatThrownBy(() -> client.updateFile(FILE_ID, FILE_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The regulatory API returned 500");
	}

	@Test
	void anUnreachableApiIsThrownAsUnavailable() {
		server.expect(requestTo("http://regulatory.test/api/v1/files/" + FILE_ID))
			.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> client.updateFile(FILE_ID, FILE_REQUEST)).isExactlyInstanceOf(RestClientException.class)
			.hasMessage("The regulatory API is unavailable")
			.hasRootCauseInstanceOf(IOException.class);
	}

}
