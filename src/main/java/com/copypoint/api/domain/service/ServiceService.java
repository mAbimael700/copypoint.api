package com.copypoint.api.domain.service;

import com.copypoint.api.domain.service.dto.ServiceCreationDTO;
import com.copypoint.api.domain.service.dto.ServiceDTO;
import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.store.StoreRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Transactional
    public ServiceDTO create(@Valid ServiceCreationDTO creationDTO, Long storeId) {

        Optional<Store> store = storeRepository.findById(storeId);

        if (store.isEmpty()) {
            throw new RuntimeException("No existe el Store con el id: " + storeId);
        }

        var newService = com.copypoint.api.domain.service.Service
                .builder()
                .name(creationDTO.name())
                .store(store.get())
                .active(true)
                .build();

        var savedService = serviceRepository.save(newService);
        return new ServiceDTO(savedService);
    }

    public Page<ServiceDTO> getAllByStoreId(Pageable pageable, Long storeId) {
        return serviceRepository.findByStoreId(storeId, pageable)
                .map(ServiceDTO::new);
    }
}
