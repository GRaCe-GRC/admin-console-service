package io.github.gracegrc.adminconsoleservice.domain.mapper;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryApiFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.RegulatoryIngestionFileRequestDto;

public final class RegulatoryFileMapper {

	private RegulatoryFileMapper() {
	}

	public static RegulatoryIngestionFileRequestDto toIngestionRequest(RegulatoryFileRequestDto request,
			String createdBy) {
		return new RegulatoryIngestionFileRequestDto(request.frameworkId(), request.title(), request.sourceUrl(),
				request.versionLabel(), request.publicationDate(), createdBy);
	}

	public static RegulatoryApiFileUpdateRequestDto toRegulatoryApiUpdateRequest(
			RegulatoryFileUpdateRequestDto request) {
		return new RegulatoryApiFileUpdateRequestDto(request.title(), request.sourceUrl(), request.versionLabel(),
				request.publicationDate());
	}

}
