package com.example.sellerhelp.appuser.service;

import com.example.sellerhelp.appuser.dto.CreateDealerDto;
import com.example.sellerhelp.appuser.dto.DealerDto;
import com.example.sellerhelp.appuser.dto.LinkedCustomerDto;
import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.Role;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.RoleRepository;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.constant.ActiveStatus;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.exception.ConflictException;
import com.example.sellerhelp.exception.ResourceNotFoundException;
import com.example.sellerhelp.order.entity.CustomerDealerMapping;
import com.example.sellerhelp.order.repository.CustomerDealerMappingRepository;
import com.example.sellerhelp.security.SecurityService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealerService {

    private final UserRepository userRepository;
    private final CustomerDealerMappingRepository customerDealerMappingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final SecurityService securityService;

    @Transactional
    public DealerDto createDealer(CreateDealerDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("A user with this email already exists.");
        }

        Role dealerRole = roleRepository.findByName(UserRole.DEALER)
                .orElseThrow(() -> new IllegalStateException("this role not found in database. Please seed roles."));

        User dealerUser = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dealerRole)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        // Save, flush, and refresh to get the database-generated userId
        User savedDealer = userRepository.saveAndFlush(dealerUser);
        entityManager.refresh(savedDealer);

        return toDto(savedDealer);
    }

    public Page<DealerDto> getAllDealers(PageableDto pageReq) {
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("name").ascending());
        Page<User> dealerPage = userRepository.findByRole_Name(UserRole.DEALER, pageable);
        return dealerPage.map(this::toDto);
    }

    public DealerDto getDealerById(String userId) {
        User distributor = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + userId));

        if (distributor.getRole().getName() != UserRole.DEALER) {
            throw new ResourceNotFoundException("User found, but is not a Distributor.");
        }

        return toDto(distributor);
    }

    @Transactional
    public void suspendDealer(String userId) {
        // We can reuse the existing service method from UserService for this
        userRepository.updateUserStatus(userId, ActiveStatus.INACTIVE);
    }

    @Transactional
    public void approveDealer(String userId) {
        // A simple "approve" action is just activating them
        userRepository.updateUserStatus(userId, ActiveStatus.ACTIVE);
    }

    /**
     * Retrieves a paginated list of all customers linked to the currently authenticated dealer.
     * @param pageReq Pagination information.
     * @return A page of simplified customer details.
     */
    public Page<LinkedCustomerDto> getMyLinkedCustomers(PageableDto pageReq) {
        User currentDealer = securityService.getCurrentUser();

        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("createdAt").descending());

        // Use the repository method we planned
        Page<CustomerDealerMapping> mappingsPage = customerDealerMappingRepository.findByDealer(currentDealer, pageable);

        return mappingsPage.map(this::toLinkedCustomerDto);
    }

    private LinkedCustomerDto toLinkedCustomerDto(CustomerDealerMapping mapping) {
        User customer = mapping.getCustomer();
        return LinkedCustomerDto.builder()
                .userId(customer.getUserId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .firstPurchaseDate(mapping.getCreatedAt())
                .build();
    }

    // In a future phase, we would add:
    // public Page<CustomerDto> getCustomersByDistributor(String userId, PageableDto pageReq)

    private DealerDto toDto(User user) {
        return DealerDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}