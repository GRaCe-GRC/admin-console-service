package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import java.util.List;

public interface ControlApiService {

	List<ControlApiControlGroupResponseDto> findAllControlGroups();

	List<ControlApiControlResponseDto> findAllControls();

}
