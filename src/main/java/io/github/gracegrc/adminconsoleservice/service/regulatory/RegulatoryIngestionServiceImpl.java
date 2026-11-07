package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.boot.client.RegulatoryIngestionServiceClient;
import io.github.gracegrc.adminconsoleservice.domain.mapper.RegulatoryAuthorityMapper;
import io.github.gracegrc.adminconsoleservice.domain.mapper.RegulatoryFrameworkMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
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
	public void deleteAuthority(UUID id) {
		regulatoryIngestionServiceClient.deleteAuthority(id);
	}

	@Override
	public UUID createFramework(RegulatoryFrameworkRequestDto request) {
		return regulatoryIngestionServiceClient
			.createFramework(RegulatoryFrameworkMapper.toIngestionRequest(request, CREATED_BY));
	}

	@Override
	public List<RegulatoryIngestionFrameworkResponseDto> findAllFrameworks() {
		return regulatoryIngestionServiceClient.findAllFrameworks();
	}

	@Override
	public void deleteFramework(UUID id) {
		regulatoryIngestionServiceClient.deleteFramework(id);
	}

	@Override
	public List<RegulatoryIngestionFileResponseDto> findAllFiles() {
		return regulatoryIngestionServiceClient.findAllFiles();
	}

	@Override
	public List<RegulatoryIngestionRequirementResponseDto> findAllRequirements() {
		return regulatoryIngestionServiceClient.findAllRequirements();
	}

}
