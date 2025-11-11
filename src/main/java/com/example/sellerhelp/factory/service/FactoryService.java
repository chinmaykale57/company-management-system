package com.example.sellerhelp.factory.service;

import com.cloudinary.api.exceptions.AlreadyExists;
import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.constant.ActiveStatus;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.factory.dto.*;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.entity.UserFactoryMapping;
import com.example.sellerhelp.factory.repository.FactoryRepository;
import com.example.sellerhelp.factory.repository.FactorySpecifications;
import com.example.sellerhelp.factory.repository.UserFactoryMappingRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactoryService {

    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;
    private final UserFactoryMappingRepository userFactoryMappingRepository;
    private final EntityManager entityManager;

    @Transactional
    public FactoryDetailsDto createFactory(CreateFactoryDto dto) throws AlreadyExists {
        if (factoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new AlreadyExists("A factory with this name already exists.");
        }

        User plantHead = userRepository.findByUserId(dto.getPlantHeadUserId()).orElseThrow(()-> new NoSuchElementException("User with this user_id does not exist"));
        if (!plantHead.getRole().getName().name().equals("PLANT_HEAD")) throw new IllegalArgumentException("This user is not a plant head");

        Factory factory = Factory.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .plantHead(plantHead)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        Factory savedFactory = factoryRepository.save(factory);
        factoryRepository.flush();
        entityManager.refresh(savedFactory);
//        savedFactory.setFactoryId(factoryRepository.findByFactoryId(savedFactory.getFactoryId()).orElseThrow(()-> new IllegalArgumentException("there was some problem fetching factory id")).getFactoryId());
        if (plantHead != null) {
            createUserFactoryMapping(plantHead, savedFactory);
        }

        return toDetailsDto(savedFactory);
    }

    // CORRECTED: Returns a Page<FactoryDetailsDto>
    public Page<FactoryDetailsDto> getAllFactories(FactoryFilterDto filter, PageableDto pageReq) {
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("name").ascending());
        Page<Factory> factoryPage = factoryRepository.findAll(FactorySpecifications.withFilter(filter), pageable);
        return factoryPage.map(this::toDetailsDto);
    }

    public FactoryDetailsDto getFactoryById(String factoryId) {
        return factoryRepository.findByFactoryId(factoryId)
                .map(this::toDetailsDto)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));
    }

    // Placeholder for your new DTO
    public FactoryToolDetailsDto getFactoryToolDetails(String factoryId) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));

        // In the future, we will add logic here to fetch the tools and product stock
        // associated with this factory.

        return FactoryToolDetailsDto.builder()
                .factoryId(factory.getFactoryId())
                .City(factory.getCity())
                .address(factory.getAddress())
                // .tools(fetchedTools)
                // .productStock(fetchedProductStock)
                .build();
    }


    @Transactional
    public FactoryDetailsDto updateFactory(String factoryId, UpdateFactoryDto dto) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));

        if (StringUtils.hasText(dto.getName())) factory.setName(dto.getName());
        if (StringUtils.hasText(dto.getCity())) factory.setCity(dto.getCity());
        if (StringUtils.hasText(dto.getAddress())) factory.setAddress(dto.getAddress());
        if (dto.getStatus() != null) factory.setIsActive(dto.getStatus());

        if (StringUtils.hasText(dto.getPlantHeadUserId())) {
            User newPlantHead = findAndValidatePlantHead(dto.getPlantHeadUserId());
            factory.setPlantHead(newPlantHead);
            createUserFactoryMapping(newPlantHead, factory);
        }

        return toDetailsDto(factoryRepository.save(factory));
    }

    // --- Helper Methods ---
    private User findAndValidatePlantHead(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User specified as Plant Head not found with ID: " + userId));
        if (user.getRole().getName() != UserRole.PLANT_HEAD) {
            throw new IllegalArgumentException("User '" + user.getName() + "' is not a Plant Head.");
        }
        return user;
    }

    private void createUserFactoryMapping(User user, Factory factory) {
        // Optional: Check if a mapping already exists to avoid duplicates
        if (userFactoryMappingRepository.existsByUserAndFactory(user, factory)) {
            return; // Mapping already exists, do nothing.
        }
        UserFactoryMapping mapping = UserFactoryMapping.builder()
                .user(user)
                .factory(factory)
                .assignedRole(user.getRole())
                .build();
        userFactoryMappingRepository.save(mapping);
    }

    // CORRECTED: Converter method for your new FactoryDetailsDto
    private FactoryDetailsDto toDetailsDto(Factory factory) {
        User plantHead = factory.getPlantHead();
        return FactoryDetailsDto.builder()
                .factoryId(factory.getFactoryId())
                .name(factory.getName())
                .city(factory.getCity())
                .address(factory.getAddress())
                .plantHeadName(plantHead != null ? plantHead.getName() : null)
                .plantHeadUserId(plantHead != null ? plantHead.getUserId() : null)
                .status(factory.getIsActive())
                .createdAt(factory.getCreatedAt())
                .updatedAt(factory.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deactivateFactory(String factoryId) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));

        if (factory.getIsActive() == ActiveStatus.INACTIVE) {
            throw new IllegalStateException("This factory is already inactive.");
        }

        factory.setIsActive(ActiveStatus.INACTIVE);
        factoryRepository.save(factory);
    }
}