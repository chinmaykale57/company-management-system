package com.example.sellerhelp.tool.service;

import com.example.sellerhelp.appuser.entity.User;
import com.example.sellerhelp.appuser.repository.UserRepository;
import com.example.sellerhelp.constant.ToolNature;
import com.example.sellerhelp.constant.UserRole;
import com.example.sellerhelp.factory.entity.Factory;
import com.example.sellerhelp.factory.entity.UserFactoryMapping;
import com.example.sellerhelp.notification.service.EmailService;
import com.example.sellerhelp.tool.dto.CreateToolRequestDto;
import com.example.sellerhelp.tool.dto.ToolIssuanceDto;
import com.example.sellerhelp.tool.dto.ToolRequestDto;
import com.example.sellerhelp.tool.dto.ToolRequestItemDto;
import com.example.sellerhelp.tool.entity.Tool;
import com.example.sellerhelp.tool.entity.ToolRequest;
import com.example.sellerhelp.tool.entity.ToolRequestMapping;
import com.example.sellerhelp.tool.repository.ToolRepository;
import com.example.sellerhelp.tool.repository.ToolRequestMappingRepository;
import com.example.sellerhelp.tool.repository.ToolRequestRepository;
import com.example.sellerhelp.constant.ToolRequestStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
@Transactional
public class ToolRequestService {

    private final ToolRequestRepository toolRequestRepository;
    private final ToolRequestMappingRepository toolRequestMappingRepository;
    private final ToolRepository toolRepository;
    private final ToolService toolService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final EmailService emailService;


    /**
     * Approves a tool request and automatically triggers the issuance of the tools.
     * This is the new, streamlined, single-action method.
     * @param toolRequestId The ID of the tool request to approve.
     * @return A list of the newly created tool issuance records.
     */
    public List<ToolIssuanceDto> approveAndIssueToolRequest(Long toolRequestId) {
        ToolRequest request = toolRequestRepository.findById(toolRequestId)
                .orElseThrow(() -> new NoSuchElementException("Tool Request not found with ID: " + toolRequestId));

        if (request.getStatus() != ToolRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved. Current status: " + request.getStatus());
        }

        request.setStatus(ToolRequestStatus.APPROVED);

        List<ToolIssuanceDto> issuances = toolService.issueToolsForRequest(toolRequestId); // This marks it FULFILLED

        // --- NEW NOTIFICATION LOGIC ---
        User worker = request.getWorker();
        String subject = "Your Tool Request has been Approved: " + request.getRequestNumber();
        String body = String.format(
                "<p>Hello %s,</p>" +
                        "<p>Good news! Your tool request <b>%s</b> has been approved and the tools have been issued to you.</p>" +
                        "<p>Please check your 'My Tools' section in the application for details and return dates.</p>",
                worker.getName(),
                request.getRequestNumber()
        );
        emailService.sendEmail(worker.getEmail(), subject, body);

        return issuances;
    }


    // and for a supervisor to reject a request.
    public void rejectToolRequest(Long toolRequestId, String comment) {
        ToolRequest request = toolRequestRepository.findById(toolRequestId)
                .orElseThrow(() -> new NoSuchElementException("Tool Request not found with ID: " + toolRequestId));

        if (request.getStatus() != ToolRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected.");
        }

        request.setStatus(ToolRequestStatus.REJECTED);
        request.setComment(comment);
        toolRequestRepository.save(request);

        // --- NEW NOTIFICATION LOGIC ---
        User worker = request.getWorker();
        String subject = "Your Tool Request has been Rejected: " + request.getRequestNumber();
        String body = String.format(
                "<p>Hello %s,</p>" +
                        "<p>Your tool request <b>%s</b> has been rejected.</p>" +
                        "<p><b>Reason:</b> %s</p>",
                worker.getName(),
                request.getRequestNumber(),
                comment
        );
        emailService.sendEmail(worker.getEmail(), subject, body);
    }

    /**
     * Creates a new tool request on behalf of the currently logged-in worker.
     * @param dto The request body containing the list of tools and quantities.
     * @return A DTO representing the newly created request.
     */
    @Transactional
    public ToolRequestDto createToolRequest(CreateToolRequestDto dto) {
        User worker = getCurrentUser();

        UserFactoryMapping userMapping = worker.getFactoryMappings().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot create tool request: Worker is not assigned to a factory."));
        Factory factory = userMapping.getFactory();

        ToolRequest toolRequest = ToolRequest.builder()
                .factory(factory)
                .worker(worker)
                .nature(ToolNature.FRESH)
                .status(ToolRequestStatus.PENDING)
                .comment(dto.getComment())
                .build();

        ToolRequest savedRequest = toolRequestRepository.saveAndFlush(toolRequest);
        entityManager.refresh(savedRequest);

        List<ToolRequestMapping> mappings = new ArrayList<>();
        for (ToolRequestItemDto item : dto.getTools()) {
            Tool tool = toolRepository.findByToolId(item.getToolId())
                    .orElseThrow(() -> new NoSuchElementException("Tool not found with ID: " + item.getToolId()));

            ToolRequestMapping mapping = ToolRequestMapping.builder()
                    .request(savedRequest)
                    .tool(tool)
                    .quantityRequested(item.getQuantity())
                    .build();
            mappings.add(mapping);
        }

        toolRequestMappingRepository.saveAll(mappings);
        savedRequest.setToolRequestMappings(mappings);

        // --- NEW NOTIFICATION LOGIC ---
        // Find the supervisors for this factory to notify them.
        List<User> supervisors = userRepository.findUsersByFactoryAndRole(factory, UserRole.CHIEF_SUPERVISOR);
        for (User supervisor : supervisors) {
            String subject = "New Tool Request Pending Approval: " + savedRequest.getRequestNumber();
            String body = String.format(
                    "<p>Hello %s,</p>" +
                            "<p>A new tool request from worker <b>%s</b> is waiting for your approval.</p>" +
                            "<p>Request Number: <b>%s</b></p>" +
                            "<p>Please log in to the SellerHelp application to review the details.</p>",
                    supervisor.getName(),
                    worker.getName(),
                    savedRequest.getRequestNumber()
            );
            emailService.sendEmail(supervisor.getEmail(), subject, body);
        }
        // --- END NOTIFICATION LOGIC ---

        return toToolRequestDto(savedRequest);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in the database."));
    }

    private ToolRequestDto toToolRequestDto(ToolRequest request) {
        List<ToolRequestItemDto> items = request.getToolRequestMappings().stream()
                .map(mapping -> {
                    ToolRequestItemDto itemDto = new ToolRequestItemDto();
                    itemDto.setToolId(mapping.getTool().getToolId());
                    itemDto.setQuantity(mapping.getQuantityRequested());
                    return itemDto;
                }).toList();

        return ToolRequestDto.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .workerName(request.getWorker().getName())
                .factoryId(request.getFactory().getFactoryId())
                .nature(request.getNature())
                .status(request.getStatus())
                .comment(request.getComment())
                .createdAt(request.getCreatedAt())
                .tools(items)
                .build();
    }
}