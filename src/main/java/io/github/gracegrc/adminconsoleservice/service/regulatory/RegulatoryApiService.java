package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import java.util.UUID;

public interface RegulatoryApiService {

	void updateAuthority(UUID id, RegulatoryAuthorityUpdateRequestDto request);

	void updateFramework(UUID id, RegulatoryFrameworkUpdateRequestDto request);

	void updateFile(UUID id, RegulatoryFileUpdateRequestDto request);

}
