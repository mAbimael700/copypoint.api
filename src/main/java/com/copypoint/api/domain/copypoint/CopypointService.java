package com.copypoint.api.domain.copypoint;

import com.copypoint.api.domain.copypoint.dto.CopypointCreationDTO;
import com.copypoint.api.domain.copypoint.dto.CopypointDTO;
import com.copypoint.api.domain.store.StoreRepository;
import com.copypoint.api.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CopypointService {
    @Autowired
    private CopypointRepository copypointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    public CopypointDTO create(CopypointCreationDTO creationDto, Long creatorId, Long storeId) {

        var store = storeRepository.findById(storeId);
        var userCreator = userRepository.findById(creatorId);

        if ((store.isEmpty()) || (userCreator.isEmpty())) {
            throw new RuntimeException("Store or User does not exists.");
        }

        Copypoint newCopypoint = Copypoint.builder()
                .store(store.get())
                .name(creationDto.name())
                .createdBy(userCreator.get())
                .responsible(userCreator.get())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        var createdCopypoint = copypointRepository.save(newCopypoint);
        return new CopypointDTO(createdCopypoint);
    }

    public Page<CopypointDTO> getlAllByStoreId(Pageable pageable, Long storeId) {
        Page<Copypoint> copypoints = copypointRepository.findAllByStoreId(pageable, storeId);
        return copypoints.map(CopypointDTO::new);

    }
}
