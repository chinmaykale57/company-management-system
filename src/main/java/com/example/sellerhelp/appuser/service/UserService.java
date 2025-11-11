package com.example.sellerhelp.appuser.service;

import com.example.sellerhelp.appuser.dto.*;
import com.example.sellerhelp.appuser.entity.Role;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.RoleRepository;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.appuser.repository.UserSpecifications;
import com.example.sellerhelp.constant.ActiveStatus;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.entity.UserFactoryMapping;
import com.example.sellerhelp.factory.repository.FactoryRepository;
import com.example.sellerhelp.factory.repository.UserFactoryMappingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserFactoryMappingRepository userFactoryMappingRepository;
    private final FactoryRepository factoryRepository;
    private final CloudinaryService cloudinaryService;

    //  filters + pagination + sorting
    public Page<UserDto> searchEmployees(UserFilterDto filter, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.withFilter(filter);
        return userRepo.findAll(spec, toPageable(pageReq)).map(UserService::toDto);
    }

    // globalSearch is also simplified
    public Page<UserDto> globalSearch(String query, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.globalSearch(query);
        return userRepo.findAll(spec, toPageable(pageReq)).map(UserService::toDto);
    }

    // getFactoryWorkers uses its own specific spec
    public Page<UserDto> getFactoryWorkers(String factoryId, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.isWorkerInFactory(factoryId);
        return userRepo.findAll(spec, toPageable(pageReq)).map(UserService::toDto);
    }

    // getBayWorkers uses its spec
    public Page<UserDto> getBayWorkers(String bayId, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.isWorkerInBay(bayId);
        return userRepo.findAll(spec, toPageable(pageReq)).map(UserService::toDto);
    }

    // Get all dealers
    public Page<UserDto> getAllDealers(PageableDto pageReq) {
        return userRepo.findByRole_NameAndIsActive(UserRole.DEALER, ActiveStatus.ACTIVE, toPageable(pageReq))
                .map(UserService::toDto);
    }

    // Dashboard counts
    public DashboardCountsDto getCounts() {
        return DashboardCountsDto.builder()
                .totalActive(userRepo.countByIsActive(ActiveStatus.ACTIVE))
                .totalWorkers(userRepo.countByRole_NameAndIsActive(UserRole.WORKER, ActiveStatus.ACTIVE))
                .totalSupervisors(userRepo.countByRole_NameAndIsActive(UserRole.CHIEF_SUPERVISOR, ActiveStatus.ACTIVE))
                .totalDealers(userRepo.countByRole_NameAndIsActive(UserRole.DEALER, ActiveStatus.ACTIVE))
                .build();
    }

    // Convert entity to DTO
    private static UserDto toDto(User u) {
        List<String> factories = u.getFactoryMappings() == null ? List.of() :
                u.getFactoryMappings().stream()
                        .map(m -> m.getFactory() != null ? m.getFactory().getName() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        String bay = u.getFactoryMappings() == null ? null :
                u.getFactoryMappings().stream()
                        .map(m -> m.getBay() != null ? m.getBay().getName() : null)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        return UserDto.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .imageUrl(u.getImageUrl())
                .roleName(u.getRole() != null ? u.getRole().getName().name() : null)
                .factories(factories)
                .bay(bay)
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private Pageable toPageable(PageableDto dto) {
        String sortField = "user_id".equalsIgnoreCase(dto.getSortBy())
                ? "userId"
                : dto.getSortBy();

        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    @PreAuthorize("isAuthenticated()")
    public UserDto getUserById(String userId) {
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        return toDto(user);
    }



    /**
     * Creates a new user with optional single factory assignment.
     */
    @Transactional
    public UserDto createUser(CreateUserRequestDto dto) throws BadRequestException {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        UserRole roleEnum;
        try {
            roleEnum = UserRole.valueOf(dto.getRoleName().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + dto.getRoleName());
        }

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new BadRequestException("Role not found: " + dto.getRoleName()));
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(role)
                .isActive(ActiveStatus.ACTIVE)
                .factoryMappings(new ArrayList<>())
                .build();

        User savedUser = userRepo.save(user);

        // Assign to ONE factory only
        if (StringUtils.hasText(dto.getFactoryId())) {
            Factory factory = factoryRepository.findByFactoryId(dto.getFactoryId())
                    .orElseThrow(() -> new BadRequestException("Factory not found: " + dto.getFactoryId()));

            UserFactoryMapping mapping = UserFactoryMapping.builder()
                    .user(savedUser)
                    .factory(factory)
                    .assignedRole(role)
                    .build();

            userFactoryMappingRepository.save(mapping);
            savedUser.getFactoryMappings().add(mapping); // for toDto()
        }

        return toDto(savedUser);
    }

    @Transactional
    public void deactivateUser(String userId) {
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        int updatedRows = userRepo.updateUserStatus(userId, ActiveStatus.INACTIVE);

        if (updatedRows == 0) {
            // This is a safety net in case the user was deleted between the exists check and the update
            throw new NoSuchElementException("User not found with ID: " + userId);
        }
    }

//    @Transactional
//    public UserDto updateUser(String userId, UpdateUserRequestDto dto) {
//        User user = userRepo.findByUserId(userId)
//                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
//
//        if (dto.getName() != null) user.setName(dto.getName());
//        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
//        if (dto.getImageUrl() != null) user.setImageUrl(dto.getImageUrl());
//
//        return toDto(userRepo.save(user));
//    }

    @Transactional
    public UserDto updateUser(String userId, UpdateUserRequestDto dto) {
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        Role newRole = user.getRole();
        if (StringUtils.hasText(dto.getRoleName())) {
            try {
                UserRole roleEnum = UserRole.valueOf(dto.getRoleName().toUpperCase());
                newRole = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new NoSuchElementException("Role not found: " + dto.getRoleName()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role provided: " + dto.getRoleName());
            }
        }

        String newName = dto.getName() != null ? dto.getName() : user.getName();
        String newPhone = dto.getPhone() != null ? dto.getPhone() : user.getPhone();
        String newImageUrl = dto.getImageUrl() != null ? dto.getImageUrl() : user.getImageUrl();

        userRepo.updateUserProfile(userId, newName, newPhone, newImageUrl, newRole);

        User updatedUser = userRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Failed to refetch user after update."));

        return toDto(updatedUser);
    }


    @Transactional
    public String uploadUserImage(String userId, MultipartFile file) {
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        String imageUrl = cloudinaryService.uploadFile(file);
        user.setImageUrl(imageUrl);

        userRepo.save(user);
        return imageUrl;
    }
}