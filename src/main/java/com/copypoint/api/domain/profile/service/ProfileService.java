package com.copypoint.api.domain.profile.service;

import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.profile.dto.ProfileCreationDTO;
import com.copypoint.api.domain.profile.dto.ProfileDTO;
import com.copypoint.api.domain.profile.repository.ProfileRepository;
import com.copypoint.api.domain.service.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
                    // Actualizar campos básicos
                    if (updateDto.name() != null) {
                        existingProfile.setName(updateDto.name());
                    }

                    if (updateDto.description() != null) {
                        existingProfile.setDescription(updateDto.description());
                    }

                    if (updateDto.unitPrice() != null) {
                        existingProfile.setUnitPrice(updateDto.unitPrice());
                    }

                    // Sincronizar servicios solo si se proporcionan en el DTO
                    if (updateDto.services() != null) {
                        syncProfileServices(existingProfile, updateDto.services());
                    }

                    existingProfile.setLastModifiedAt(LocalDateTime.now());
                    Profile savedProfile = profileRepository.save(existingProfile);
                    return new ProfileDTO(savedProfile);
                });
    }


    /**
     * Sincroniza los servicios del perfil con los IDs proporcionados.
     * Solo agrega servicios nuevos y elimina los que ya no están en la lista.
     *
     * @param profile el perfil a actualizar
     * @param serviceIds lista de IDs de servicios deseados
     */
    private void syncProfileServices(Profile profile, List<Long> serviceIds) {
        // Obtener servicios actuales del perfil
        List<com.copypoint.api.domain.service.Service> currentServices = profile.getServices();
        Set<Long> currentServiceIds = currentServices.stream()
                .map(com.copypoint.api.domain.service.Service::getId)
                .collect(Collectors.toSet());

        // Convertir la lista de IDs a Set para operaciones más eficientes
        Set<Long> newServiceIds = new HashSet<>(serviceIds);

        // Encontrar servicios a eliminar (están en current pero no en new)
        List<com.copypoint.api.domain.service.Service> servicesToRemove = currentServices.stream()
                .filter(service -> !newServiceIds.contains(service.getId()))
                .toList();

        // Encontrar IDs de servicios a agregar (están en new pero no en current)
        Set<Long> serviceIdsToAdd = newServiceIds.stream()
                .filter(serviceId -> !currentServiceIds.contains(serviceId))
                .collect(Collectors.toSet());

        // Remover servicios que ya no están en la lista
        for (com.copypoint.api.domain.service.Service serviceToRemove : servicesToRemove) {
            profile.getServices().remove(serviceToRemove);
            serviceToRemove.getProfiles().remove(profile);
        }

        // Agregar nuevos servicios
        if (!serviceIdsToAdd.isEmpty()) {
            List<com.copypoint.api.domain.service.Service> servicesToAdd = serviceRepository
                    .findAllById(serviceIdsToAdd);

            // Validar que todos los servicios existen
            if (servicesToAdd.size() != serviceIdsToAdd.size()) {
                Set<Long> foundServiceIds = servicesToAdd.stream()
                        .map(com.copypoint.api.domain.service.Service::getId)
                        .collect(Collectors.toSet());

                Set<Long> missingServiceIds = serviceIdsToAdd.stream()
                        .filter(id -> !foundServiceIds.contains(id))
                        .collect(Collectors.toSet());

                throw new IllegalArgumentException("Los siguientes servicios no existen: " + missingServiceIds);
            }

            // Agregar nuevos servicios y actualizar relación bidireccional
            for (com.copypoint.api.domain.service.Service serviceToAdd : servicesToAdd) {
                profile.getServices().add(serviceToAdd);
                serviceToAdd.getProfiles().add(profile);
            }
        }
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
