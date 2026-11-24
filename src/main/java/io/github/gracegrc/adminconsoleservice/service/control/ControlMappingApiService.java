package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.util.List;
import java.util.UUID;

public interface ControlMappingApiService {

	UUID createMapping(ControlMappingRequestDto request);

	List<ControlMappingApiMappingResponseDto> findAllMappings();

	void updateMapping(UUID id, ControlMappingUpdateRequestDto request);

	void deleteMapping(UUID id);

}
