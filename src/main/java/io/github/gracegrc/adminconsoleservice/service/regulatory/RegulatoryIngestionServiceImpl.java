package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.boot.client.RegulatoryIngestionServiceClient;
import io.github.gracegrc.adminconsoleservice.domain.mapper.RegulatoryAuthorityMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegulatoryIngestionServiceImpl implements RegulatoryIngestionService {

	static final String CREATED_BY = "admin-console";

	private final RegulatoryIngestionServiceClient regulatoryIngestionServiceClient;

	public RegulatoryIngestionServiceImpl(RegulatoryIngestionServiceClient regulatoryIngestionServiceClient) {
		this.regulatoryIngestionServiceClient = regulatoryIngestionServiceClient;
	}

	@Override
	public UUID createAuthority(RegulatoryAuthorityRequestDto request) {
		return regulatoryIngestionServiceClient
			.createAuthority(RegulatoryAuthorityMapper.toIngestionRequest(request, CREATED_BY));
	}

	@Override
	public List<RegulatoryAuthorityResponseDto> findAllAuthorities() {
		return regulatoryIngestionServiceClient.findAllAuthorities()
			.stream()
			.map(RegulatoryAuthorityMapper::toResponseDto)
			.toList();
	}

	@Override
	public RegulatoryAuthorityResponseDto findAuthorityById(UUID id) {
		return RegulatoryAuthorityMapper.toResponseDto(regulatoryIngestionServiceClient.findAuthorityById(id));
	}

	@Override
	public void deleteAuthority(UUID id) {
		regulatoryIngestionServiceClient.deleteAuthority(id);
	}

}
