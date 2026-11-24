package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlMappingApiClient;
import io.github.gracegrc.adminconsoleservice.domain.mapper.ControlMappingMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ControlMappingApiServiceImpl implements ControlMappingApiService {

	static final String CREATED_BY = "admin-console";

	private final ControlMappingApiClient controlMappingApiClient;

	public ControlMappingApiServiceImpl(ControlMappingApiClient controlMappingApiClient) {
		this.controlMappingApiClient = controlMappingApiClient;
	}

	@Override
	public UUID createMapping(ControlMappingRequestDto request) {
		return controlMappingApiClient
			.createMapping(ControlMappingMapper.toControlMappingApiRequest(request, CREATED_BY));
	}

	@Override
	public List<ControlMappingApiMappingResponseDto> findAllMappings() {
		return controlMappingApiClient.findAllMappings();
	}

	@Override
	public void updateMapping(UUID id, ControlMappingUpdateRequestDto request) {
		controlMappingApiClient.updateMapping(id, ControlMappingMapper.toControlMappingApiUpdateRequest(request));
	}

	@Override
	public void deleteMapping(UUID id) {
		controlMappingApiClient.deleteMapping(id);
	}

}
