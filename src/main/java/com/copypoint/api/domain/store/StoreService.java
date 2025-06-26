package com.copypoint.api.domain.store;

import com.copypoint.api.domain.employee.EmployeeService;
import com.copypoint.api.domain.role.RoleType;
import com.copypoint.api.domain.store.dto.StoreCreationDTO;
import com.copypoint.api.domain.store.dto.StoreDTO;
import com.copypoint.api.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StoreService {
    @Autowired
    private UserService userService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EmployeeService employeeService;


    public StoreDTO createStore(StoreCreationDTO creationDTO, Long userId) {

        LocalDateTime creationDate = LocalDateTime.now();
        var userOwner = userService.getById(userId);

        if (!userOwner.isPresent()) {
            throw new RuntimeException("No existe el usuario proporcionado");
        }

        Store newStore = Store.builder()
                .createdAt(creationDate)
                .name(creationDTO.name())
                .owner(userOwner.get()).build();


        var savedStore = storeRepository.save(newStore);

        try {
            employeeService.saveEmployee(userOwner.get(), savedStore, null, RoleType.STORE_OWNER);
        } catch (Exception e) {
            // Si falla la creación del empleado, podríamos considerar hacer rollback
            throw new RuntimeException("Error al crear el empleado propietario: " + e.getMessage(), e);
        }

        return new StoreDTO(savedStore);
    }

    public Page<StoreDTO> getAll(Pageable pageable) {
        return storeRepository.findAll(pageable)
                .map(StoreDTO::new);
    }
}
