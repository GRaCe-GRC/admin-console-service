package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import java.util.List;
import java.util.UUID;

public interface ControlApiService {

	List<ControlApiControlGroupResponseDto> findAllControlGroups();

	List<ControlApiControlResponseDto> findAllControls();

	UUID createControlGroup(ControlGroupRequestDto request);

	void updateControlGroup(UUID id, ControlGroupUpdateRequestDto request);

	void deleteControlGroup(UUID id);

	UUID createControl(ControlRequestDto request);

	void updateControl(UUID id, ControlUpdateRequestDto request);

	void deleteControl(UUID id);

}
