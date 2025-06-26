package com.copypoint.api.domain.profile;

import com.copypoint.api.domain.profile.dto.ProfileCreationDTO;
import com.copypoint.api.domain.profile.dto.ProfileDTO;
import com.copypoint.api.domain.service.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public ProfileDTO createProfile(ProfileCreationDTO creationDto) {
        List<com.copypoint.api.domain.service.Service> existingServices =
                creationDto.services().stream()
                        .map(serviceId -> serviceRepository.findById(serviceId))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

        // Crear el nuevo perfil
        Profile newProfile = Profile.builder()
                .services(existingServices)
                .name(creationDto.name())
                .description(creationDto.description())
                .unitPrice(creationDto.unitPrice())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .active(true)
                .build();

        // Guardar en la base de datos
        Profile savedProfile = profileRepository.save(newProfile);

        existingServices.forEach(service -> {
            if (!service.getProfiles().contains(savedProfile)) {
                service.getProfiles().add(savedProfile);
            }
        });

        // Retornar el DTO
        return new ProfileDTO(savedProfile);
    }

    public Page<ProfileDTO> getProfilesByServiceId(Long serviceId, Pageable pageable) {
        Page<Profile> profilesPage = profileRepository.findByServicesId(serviceId, pageable);
        return profilesPage.map(ProfileDTO::new);
    }

    public Optional<ProfileDTO> updateProfile(Long id, ProfileCreationDTO updateDto) {
        return profileRepository.findById(id)
                .map(existingProfile -> {
                    // Obtener servicios actualizados
                    List<com.copypoint.api.domain.service.Service> updatedServices =
                            updateDto.services().stream()
                                    .map(serviceId -> serviceRepository.findById(serviceId))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .toList();

                    // Actualizar campos
                    existingProfile.setName(updateDto.name());
                    existingProfile.setDescription(updateDto.description());
                    existingProfile.setServices(updatedServices);

                    Profile savedProfile = profileRepository.save(existingProfile);
                    return new ProfileDTO(savedProfile);
                });
    }

    public boolean deactivateProfile(Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    profile.setActive(false);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean activateProfile(Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    profile.setActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }
}
