package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import java.util.UUID;

public interface RegulatoryApiService {

	void updateFile(UUID id, RegulatoryFileUpdateRequestDto request);

}
