package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlApiClient;
import io.github.gracegrc.adminconsoleservice.domain.mapper.ControlGroupMapper;
import io.github.gracegrc.adminconsoleservice.domain.mapper.ControlMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ControlApiServiceImpl implements ControlApiService {

	static final String CREATED_BY = "admin-console";

	private final ControlApiClient controlApiClient;

	public ControlApiServiceImpl(ControlApiClient controlApiClient) {
		this.controlApiClient = controlApiClient;
	}

	@Override
	public List<ControlApiControlGroupResponseDto> findAllControlGroups() {
		return controlApiClient.findAllControlGroups();
	}

	@Override
	public List<ControlApiControlResponseDto> findAllControls() {
		return controlApiClient.findAllControls();
	}

	@Override
	public UUID createControlGroup(ControlGroupRequestDto request) {
		return controlApiClient.createControlGroup(ControlGroupMapper.toControlApiRequest(request, CREATED_BY));
	}

	@Override
	public void updateControlGroup(UUID id, ControlGroupUpdateRequestDto request) {
		controlApiClient.updateControlGroup(id, ControlGroupMapper.toControlApiUpdateRequest(request));
	}

	@Override
	public void deleteControlGroup(UUID id) {
		controlApiClient.deleteControlGroup(id);
	}

	@Override
	public UUID createControl(ControlRequestDto request) {
		return controlApiClient.createControl(ControlMapper.toControlApiRequest(request, CREATED_BY));
	}

	@Override
	public void updateControl(UUID id, ControlUpdateRequestDto request) {
		controlApiClient.updateControl(id, ControlMapper.toControlApiUpdateRequest(request));
	}

	@Override
	public void deleteControl(UUID id) {
		controlApiClient.deleteControl(id);
	}

}
