package com.copypoint.api.domain.saleprofile.service;

import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleProfileService {

    @Transactional
    public SaleProfileDTO createSaleProfile(SaleProfileCreationDTO creationDTO) {
        return null;
    }
}
