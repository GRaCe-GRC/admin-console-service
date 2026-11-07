package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionRequirementResponseDto;
import java.util.List;
import java.util.UUID;

public interface RegulatoryIngestionService {

	UUID createAuthority(RegulatoryAuthorityRequestDto request);

	List<RegulatoryAuthorityResponseDto> findAllAuthorities();

	void deleteAuthority(UUID id);

	UUID createFramework(RegulatoryFrameworkRequestDto request);

	List<RegulatoryIngestionFrameworkResponseDto> findAllFrameworks();

	void deleteFramework(UUID id);

	List<RegulatoryIngestionFileResponseDto> findAllFiles();

	List<RegulatoryIngestionRequirementResponseDto> findAllRequirements();

}
