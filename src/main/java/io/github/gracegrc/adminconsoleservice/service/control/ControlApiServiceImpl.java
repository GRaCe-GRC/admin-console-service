package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlApiClient;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlGroupResponseDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlApiControlResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ControlApiServiceImpl implements ControlApiService {

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

}
