package com.copypoint.api.domain.customerservicephone.controller;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.dto.CustomerServicePhoneCreationDTO;
import com.copypoint.api.domain.customerservicephone.dto.CustomerServicePhoneDTO;
import com.copypoint.api.domain.customerservicephone.service.CustomerServicePhoneService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class CustomerServicePhoneController {
    @Autowired
    private CustomerServicePhoneService phoneService;

    @PostMapping("/copypoints/{copypointId}/phones")
    public ResponseEntity<CustomerServicePhoneDTO> saveCustomerServicePhone(
            @Valid @RequestBody CustomerServicePhoneCreationDTO creationDTO,
            @PathVariable Long copypointId
    ) {
        CustomerServicePhone phone = phoneService.save(creationDTO, copypointId);
        return ResponseEntity.created(URI.create(
                        "/copypoints/" + phone.getCopypoint().getId()
                                + "/phones/" + phone.getId()))
                .body(new CustomerServicePhoneDTO(phone));
    }

    @GetMapping("/copypoints/{copypointId}/phones")
    public ResponseEntity<Page<CustomerServicePhoneDTO>> getByCopypoint(
            @PathVariable Long copypointId, Pageable pageable) {
        Page<CustomerServicePhone> phones = phoneService.getByCopypointId(copypointId, pageable);
        return ResponseEntity.ok(phones.map(CustomerServicePhoneDTO::new));
    }
}
