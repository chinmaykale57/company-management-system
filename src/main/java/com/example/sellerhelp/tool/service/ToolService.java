package com.example.sellerhelp.tool.service;

import com.example.sellerhelp.appuser.dto.PageableDto;
import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.constant.ToolIssuanceStatus;
import com.example.sellerhelp.constant.ToolRequestStatus;
import com.example.sellerhelp.exception.BadRequestException;
import com.example.sellerhelp.exception.ConflictException;
import com.example.sellerhelp.exception.ResourceNotFoundException;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.entity.UserFactoryMapping;
import com.example.sellerhelp.factory.repository.FactoryRepository;
import com.example.sellerhelp.notification.service.EmailService;
import com.example.sellerhelp.security.SecurityService;
import com.example.sellerhelp.tool.dto.*;
import com.example.sellerhelp.tool.entity.*;
import com.example.sellerhelp.tool.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final ToolStockRepository toolStockRepository;
    private final FactoryRepository factoryRepository;
    private final ToolRequestRepository toolRequestRepository;
    private final ToolIssuanceRepository toolIssuanceRepository;
    private final ToolReturnRepository toolReturnRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final SecurityService securityService;
    private final EmailService emailService;

    // --- CATEGORY MANAGEMENT ---
    @Transactional
    public ToolCategoryDto createToolCategory(ToolCategoryDto dto) {
        if (toolCategoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("A tool category with this name already exists.");
        }
        ToolCategory category = ToolCategory.builder().name(dto.getName()).description(dto.getDescription()).build();
        return toToolCategoryDto(toolCategoryRepository.save(category));
    }

    public List<ToolCategoryDto> getAllToolCategories() {
        return toolCategoryRepository.findAll().stream().map(this::toToolCategoryDto).toList();
    }

    // --- MASTER TOOL MANAGEMENT ---
    @Transactional
    public ToolDto createTool(CreateToolDto dto) {
        if (toolRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("A tool with this name already exists.");
        }

        ToolCategory category = toolCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Tool category not found with ID: " + dto.getCategoryId()));

        Tool tool = Tool.builder()
                .name(dto.getName())
                .category(category)
                .imageUrl(dto.getImageUrl())
                .isPerishable(dto.getIsPerishable())
                .isExpensive(dto.getIsExpensive())
                .threshold(dto.getThreshold())
                .build();

        // Save and flush to execute the INSERT statement, which fires the database trigger
        Tool savedTool = toolRepository.saveAndFlush(tool);

        entityManager.refresh(savedTool);

        return toToolDto(savedTool);
    }

    public Page<ToolDto> getAllMasterTools(PageableDto pageReq) {
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("name").ascending());
        return toolRepository.findAll(pageable).map(this::toToolDto);
    }

    // --- FACTORY STOCK MANAGEMENT ---
    @Transactional
    public ToolStockDto addStockToFactory(String factoryId, AddToolStockDto dto) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));

        Tool tool = toolRepository.findByToolId(dto.getToolId())
                .orElseThrow(() -> new NoSuchElementException("Tool not found with ID: ".concat(dto.getToolId())));

        // Try to find the existing stock record
        Optional<ToolStock> existingStockOpt = toolStockRepository.findByFactoryAndTool(factory, tool);

        if (existingStockOpt.isPresent()) {
            // --- CASE 1: STOCK ALREADY EXISTS ---
            // Use the safe, direct UPDATE query we created before.
            ToolStock existingStock = existingStockOpt.get();
            toolStockRepository.incrementStock(existingStock.getId(), dto.getQuantity());
        } else {
            // --- CASE 2: THIS IS A NEW STOCK ENTRY ---
            // Use the safe, direct NATIVE INSERT query.
            // This completely bypasses the save() method and all entity lifecycle events.
            toolStockRepository.insertNewStock(factory.getId(), tool.getId(), dto.getQuantity());
        }

        // After either updating or inserting, we MUST refetch the record to get the latest state for our DTO.
        ToolStock updatedStock = toolStockRepository.findByFactoryAndTool(factory, tool)
                .orElseThrow(() -> new IllegalStateException("Critical error: Stock record not found after insert/update."));

        return toToolStockDto(updatedStock);
    }

    /**
     * Retrieves the tools currently issued to the currently authenticated worker.
     * The worker is identified via their security token.
     * @param pageReq Pagination information.
     * @return A page of the worker's active tool issuances.
     */
    public Page<ToolIssuanceDto> getMyIssuedTools(PageableDto pageReq) {
        User worker = securityService.getCurrentUser(); // Use the security service

        // We can reuse the logic from getToolsByWorker, but it's cleaner to have a dedicated method.
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("returnDate").ascending());
        List<ToolIssuanceStatus> activeStatuses = List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED, ToolIssuanceStatus.EXTENSION_REQUESTED);

        return toolIssuanceRepository.findByWorkerAndStatusIn(worker, activeStatuses, pageable)
                .map(this::toToolIssuanceDto);
    }

    @Transactional
    public void initiateReturn(Long issuanceId) {
        User worker = securityService.getCurrentUser();
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tool issuance record not found with ID: " + issuanceId));

        // Validation: Ensure the worker owns this issuance
        if (!issuance.getWorker().getId().equals(worker.getId())) {
            throw new BadRequestException("You can only initiate returns for tools issued to you.");
        }

        // Validation: Ensure the tool is in a returnable state
        if (issuance.getStatus() != ToolIssuanceStatus.ISSUED && issuance.getStatus() != ToolIssuanceStatus.EXTENDED) {
            throw new ConflictException("Cannot return a tool that is not currently in your possession.");
        }

        issuance.setStatus(ToolIssuanceStatus.RETURN_PENDING);
        toolIssuanceRepository.save(issuance);
        // In a real system, this would also send a notification to the supervisor.
    }

    public Page<ToolStockDto> getToolsByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new NoSuchElementException("Factory not found with ID: " + factoryId));
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize());
        return toolStockRepository.findByFactory(factory, pageable).map(this::toToolStockDto);
    }


    // --- TOOL ISSUANCE (CHIEF SUPERVISOR) ---
    @Transactional
    public List<ToolIssuanceDto> issueToolsForRequest(Long toolRequestId) {
        User issuer = getCurrentUser();
        ToolRequest request = toolRequestRepository.findById(toolRequestId)
                .orElseThrow(() -> new NoSuchElementException("Tool Request not found with ID: " + toolRequestId));

        if (request.getStatus() != ToolRequestStatus.APPROVED) {
            throw new IllegalStateException("Cannot issue tools for a request that is not approved.");
        }

        List<ToolIssuance> issuances = new ArrayList<>();
        for (ToolRequestMapping mapping : request.getToolRequestMappings()) {
            Tool tool = mapping.getTool();
            Long requestedQuantity = mapping.getQuantityRequested();

            ToolStock stock = toolStockRepository.findByFactoryAndTool(request.getFactory(), tool)
                    .orElseThrow(() -> new IllegalStateException("Tool stock not found for " + tool.getName() + " in this factory."));

            if (stock.getAvailableQuantity() < requestedQuantity) {
                throw new IllegalStateException("Insufficient stock for " + tool.getName() + ". Available: " + stock.getAvailableQuantity() + ", Requested: " + requestedQuantity);
            }

            stock.setAvailableQuantity(stock.getAvailableQuantity() - requestedQuantity);
            stock.setIssuedQuantity(stock.getIssuedQuantity() + requestedQuantity);
            toolStockRepository.save(stock);

            ToolIssuance issuance = ToolIssuance.builder()
                    .factory(request.getFactory())
                    .request(request)
                    .worker(request.getWorker())
                    .issuer(issuer)
                    .tool(tool)
                    .quantity(requestedQuantity)
                    .status(ToolIssuanceStatus.ISSUED)
                    .returnDate(LocalDateTime.now().plusDays(7))
                    .build();
            issuances.add(issuance);
        }

        request.setStatus(ToolRequestStatus.FULFILLED);
        toolRequestRepository.save(request);

        return toolIssuanceRepository.saveAll(issuances).stream().map(this::toToolIssuanceDto).toList();
    }

    // --- TOOL RETURN & CONFISCATION (CHIEF SUPERVISOR) ---
    @Transactional
    public void returnTool(Long issuanceId, Long fitQuantity, Long unfitQuantity) {
        User returnProcessor = getCurrentUser();
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new NoSuchElementException("Tool issuance record not found with ID: " + issuanceId));

        if (issuance.getStatus() != ToolIssuanceStatus.ISSUED && issuance.getStatus() != ToolIssuanceStatus.EXTENDED) {
            throw new IllegalStateException("Cannot return a tool that is not currently issued or extended.");
        }

        Long totalReturned = fitQuantity + unfitQuantity;
        if (!totalReturned.equals(issuance.getQuantity())) {
            throw new IllegalArgumentException("The sum of fit and unfit quantities (" + totalReturned + ") must equal the issued quantity (" + issuance.getQuantity() + ").");
        }

        ToolStock stock = toolStockRepository.findByFactoryAndTool(issuance.getFactory(), issuance.getTool())
                .orElseThrow(() -> new IllegalStateException("Critical error: Tool stock record disappeared for an issued tool."));

        stock.setIssuedQuantity(stock.getIssuedQuantity() - issuance.getQuantity());
        stock.setAvailableQuantity(stock.getAvailableQuantity() + fitQuantity);
        stock.setTotalQuantity(stock.getTotalQuantity() - unfitQuantity);
        toolStockRepository.save(stock);

        ToolReturn toolReturn = ToolReturn.builder()
                .toolIssuance(issuance)
                .fitQuantity(fitQuantity)
                .unfitQuantity(unfitQuantity)
                .updatedBy(returnProcessor)
                .returnedAt(LocalDateTime.now())
                .build();
        toolReturnRepository.save(toolReturn);

        issuance.setStatus(ToolIssuanceStatus.RETURNED);
        issuance.setReturnedAt(LocalDateTime.now());
        toolIssuanceRepository.save(issuance);
    }

    @Transactional
    public void confiscateTool(Long issuanceId) {
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new NoSuchElementException("Tool issuance record not found with ID: " + issuanceId));

        if (issuance.getStatus() != ToolIssuanceStatus.ISSUED && issuance.getStatus() != ToolIssuanceStatus.EXTENDED) {
            throw new IllegalStateException("This tool has already been returned or processed.");
        }

        if (issuance.getReturnDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot confiscate a tool that is not yet overdue.");
        }

        returnTool(issuanceId, 0L, issuance.getQuantity());

        issuance.setStatus(ToolIssuanceStatus.CONFISCATED);
        toolIssuanceRepository.save(issuance);
    }

    // --- WORKER-CENTRIC VIEWS ---
    public Page<ToolIssuanceDto> getToolsByWorker(String workerId, PageableDto pageReq) {
        User worker = userRepository.findByUserId(workerId)
                .orElseThrow(() -> new NoSuchElementException("Worker not found with ID: " + workerId));
        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("returnDate").ascending());

        List<ToolIssuanceStatus> statuses = List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED, ToolIssuanceStatus.EXTENSION_REQUESTED);

        return toolIssuanceRepository.findByWorkerAndStatusIn(worker, statuses, pageable).map(this::toToolIssuanceDto);
    }


    /**
     * Allows a WORKER to request an extension for a tool they have been issued.
     * @param issuanceId The ID of the tool issuance record.
     */
    @Transactional
    public void requestExtension(Long issuanceId) {
        User worker = securityService.getCurrentUser();
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tool issuance record not found with ID: " + issuanceId));

        // --- VALIDATION ---
        // 1. Check if the current user is the worker the tool was issued to.
        if (!issuance.getWorker().getId().equals(worker.getId())) {
            throw new BadRequestException("You can only request extensions for tools issued to you.");
        }
        // 2. Check if the tool is in a state that allows for an extension request.
        if (issuance.getStatus() != ToolIssuanceStatus.ISSUED && issuance.getStatus() != ToolIssuanceStatus.EXTENDED) {
            throw new ConflictException("Cannot request extension for a tool that has been returned, confiscated, or already has a pending request.");
        }
        // 3. Optional: Prevent requests for already overdue tools.
        if (issuance.getReturnDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot request an extension for an overdue tool.");
        }

        // --- LOGIC ---
        issuance.setStatus(ToolIssuanceStatus.EXTENSION_REQUESTED);
        toolIssuanceRepository.save(issuance);
    }

    /**
     * Allows a CHIEF_SUPERVISOR to approve or deny an extension request.
     * @param issuanceId The ID of the tool issuance record.
     * @param dto The request body with the approval decision.
     */
    @Transactional
    public ToolIssuanceDto processExtensionRequest(Long issuanceId, ApproveExtensionDto dto) {
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tool issuance record not found with ID: " + issuanceId));

        // --- VALIDATION ---
        if (issuance.getStatus() != ToolIssuanceStatus.EXTENSION_REQUESTED) {
            throw new BadRequestException("This issuance does not have a pending extension request.");
        }

        // --- LOGIC ---
        if (dto.getApproved()) {
            // Extend the return date (e.g., by another 7 days from the original due date)
            issuance.setReturnDate(issuance.getReturnDate().plusDays(7));
            issuance.setStatus(ToolIssuanceStatus.EXTENDED);
        } else {
            // If denied, simply revert the status back to ISSUED.
            issuance.setStatus(ToolIssuanceStatus.ISSUED);
            // Here you could trigger a notification to the worker.
        }

        return toToolIssuanceDto(toolIssuanceRepository.save(issuance));
    }

    /**
     * Retrieves a paginated list of all tools that are currently overdue
     * for the factory of the currently logged-in supervisor.
     * @param pageReq Pagination information.
     * @return A page of ToolIssuanceDto objects representing the overdue tools.
     */
    public Page<ToolIssuanceDto> getOverdueTools(PageableDto pageReq) {
        User supervisor = securityService.getCurrentUser();

        Factory factory = supervisor.getFactoryMappings().stream()
                .map(UserFactoryMapping::getFactory)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("You are not assigned to a factory."));

        Pageable pageable = PageRequest.of(pageReq.getPage(), pageReq.getSize(), Sort.by("returnDate").ascending());
        List<ToolIssuanceStatus> activeStatuses = List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED);

        // --- THIS IS THE FIX ---
        // Call the new, explicit query method.
        Page<ToolIssuance> overdueIssuances = toolIssuanceRepository.findOverdueTools(
                factory,
                LocalDateTime.now(),
                activeStatuses,
                pageable
        );

        return overdueIssuances.map(this::toToolIssuanceDto);
    }


    // --- HELPER & DTO METHODS ---
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    private ToolCategoryDto toToolCategoryDto(ToolCategory category) {
        return ToolCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private ToolIssuanceDto toToolIssuanceDto(ToolIssuance issuance) {
        return ToolIssuanceDto.builder()
                .issuanceId(issuance.getId())
                .toolName(issuance.getTool().getName())
                .workerName(issuance.getWorker().getName())
                .issuerName(issuance.getIssuer() != null ? issuance.getIssuer().getName() : null)
                .status(issuance.getStatus())
                .issuedAt(issuance.getIssuedAt())
                .returnDate(issuance.getReturnDate())
                .build();
    }

    private ToolDto toToolDto(Tool tool) {
        return ToolDto.builder()
                .toolId(tool.getToolId())
                .name(tool.getName())
                .categoryName(tool.getCategory() != null ? tool.getCategory().getName() : "Uncategorized")
                .imageUrl(tool.getImageUrl())
                .isPerishable(tool.getIsPerishable())
                .isExpensive(tool.getIsExpensive())
                .threshold(tool.getThreshold())
                .createdAt(tool.getCreatedAt())
                .build();
    }

    private ToolStockDto toToolStockDto(ToolStock stock) {
        return ToolStockDto.builder()
                .toolId(stock.getTool().getToolId())
                .toolName(stock.getTool().getName())
                .factoryId(stock.getFactory().getFactoryId())
                .totalQuantity(stock.getTotalQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .issuedQuantity(stock.getIssuedQuantity())
                .lastUpdatedAt(stock.getLastUpdatedAt())
                .build();
    }
}