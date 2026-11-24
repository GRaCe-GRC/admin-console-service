package io.github.gracegrc.adminconsoleservice.service.regulatory;

import io.github.gracegrc.adminconsoleservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.adminconsoleservice.domain.mapper.RegulatoryFileMapper;
import io.github.gracegrc.adminconsoleservice.domain.model.dto.RegulatoryFileUpdateRequestDto;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegulatoryApiServiceImpl implements RegulatoryApiService {

	private final RegulatoryApiClient regulatoryApiClient;

	public RegulatoryApiServiceImpl(RegulatoryApiClient regulatoryApiClient) {
		this.regulatoryApiClient = regulatoryApiClient;
	}

	@Override
	public void updateFile(UUID id, RegulatoryFileUpdateRequestDto request) {
		regulatoryApiClient.updateFile(id, RegulatoryFileMapper.toRegulatoryApiUpdateRequest(request));
	}

}
