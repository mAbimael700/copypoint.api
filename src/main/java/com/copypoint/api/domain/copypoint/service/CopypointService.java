package com.copypoint.api.domain.copypoint.service;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.copypoint.repository.CopypointRepository;
import com.copypoint.api.domain.copypoint.CopypointStatus;
import com.copypoint.api.domain.copypoint.dto.CopypointCreationDTO;
import com.copypoint.api.domain.copypoint.dto.CopypointDTO;
import com.copypoint.api.domain.employee.service.EmployeeService;
import com.copypoint.api.domain.role.RoleType;
import com.copypoint.api.domain.store.repository.StoreRepository;
import com.copypoint.api.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CopypointService {
    @Autowired
    private CopypointRepository copypointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EmployeeService employeeService;

    @Transactional
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
                .status(CopypointStatus.ACTIVE)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        Copypoint savedCopypoint = copypointRepository.save(newCopypoint);

        try {
            employeeService.saveEmployee(userCreator.get(), null, savedCopypoint, RoleType.COPYPOINT_MANAGER);
        } catch (Exception e) {
            // Si falla la creación del empleado, podríamos considerar hacer rollback
            throw new RuntimeException("Error al crear el empleado propietario: " + e.getMessage(), e);
        }

        return new CopypointDTO(savedCopypoint);
    }

    public Page<CopypointDTO> getlAllByStoreId(Pageable pageable, Long storeId) {
        Page<Copypoint> copypoints = copypointRepository.findAllByStoreId(pageable, storeId);
        return copypoints.map(CopypointDTO::new);

    }
}
