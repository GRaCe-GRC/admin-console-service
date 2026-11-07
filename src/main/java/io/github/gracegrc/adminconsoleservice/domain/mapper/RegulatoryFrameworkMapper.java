package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFrameworkRequestDto;

public final class RegulatoryFrameworkMapper {

	private RegulatoryFrameworkMapper() {
	}

	public static RegulatoryIngestionFrameworkRequestDto toIngestionRequest(RegulatoryFrameworkRequestDto request,
			String createdBy) {
		return new RegulatoryIngestionFrameworkRequestDto(request.authorityId(), request.name(), request.shortName(),
				request.citation(), createdBy);
	}

}
