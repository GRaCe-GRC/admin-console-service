package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.util.List;

public interface ControlMappingApiService {

	List<ControlMappingApiMappingResponseDto> findAllMappings();

}
