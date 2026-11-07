package io.github.gracegrc.adminconsoleservice.service.control;

import io.github.gracegrc.adminconsoleservice.boot.client.ControlMappingApiClient;
import io.github.gracegrc.adminconsoleservice.service.model.dto.ControlMappingApiMappingResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ControlMappingApiServiceImpl implements ControlMappingApiService {

	private final ControlMappingApiClient controlMappingApiClient;

	public ControlMappingApiServiceImpl(ControlMappingApiClient controlMappingApiClient) {
		this.controlMappingApiClient = controlMappingApiClient;
	}

	@Override
	public List<ControlMappingApiMappingResponseDto> findAllMappings() {
		return controlMappingApiClient.findAllMappings();
	}

}
