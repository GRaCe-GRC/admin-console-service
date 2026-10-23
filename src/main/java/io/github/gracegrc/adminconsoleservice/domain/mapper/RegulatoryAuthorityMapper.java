package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionAuthorityResponseDto;

public final class RegulatoryAuthorityMapper {

	private RegulatoryAuthorityMapper() {
	}

	public static RegulatoryIngestionAuthorityRequestDto toIngestionRequest(RegulatoryAuthorityRequestDto request,
			String createdBy) {
		return new RegulatoryIngestionAuthorityRequestDto(request.name(), request.jurisdiction(), createdBy);
	}

	public static RegulatoryAuthorityResponseDto toResponseDto(RegulatoryIngestionAuthorityResponseDto authority) {
		return new RegulatoryAuthorityResponseDto(authority.id(), authority.name(), authority.jurisdiction(),
				authority.createdAt(), authority.enabled(), authority.createdBy());
	}

}
