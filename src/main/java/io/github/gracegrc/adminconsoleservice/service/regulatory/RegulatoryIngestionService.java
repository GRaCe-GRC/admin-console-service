package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import java.util.List;
import java.util.UUID;

public interface RegulatoryIngestionService {

	UUID createAuthority(RegulatoryAuthorityRequestDto request);

	List<RegulatoryAuthorityResponseDto> findAllAuthorities();

	RegulatoryAuthorityResponseDto findAuthorityById(UUID id);

	void deleteAuthority(UUID id);

}
