package com.br.api.lanchonete.services;

import com.br.api.lanchonete.domain.stock.*;
import com.br.api.lanchonete.domain.product.Product;
import com.br.api.lanchonete.domain.user.User;
import com.br.api.lanchonete.exceptions.InsufficientStockException;
import com.br.api.lanchonete.repositories.StockMovementRepository;
import com.br.api.lanchonete.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockMovementRepository movementRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Register a stock movement
     * Validates business rules before registering
     */
    @Transactional
    public StockMovementResponseDTO registerMovement(StockMovementRequestDTO dto) {
        // Find the product
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate if it won't generate negative stock
        if (dto.movementType() == MovementType.EXIT || 
            dto.movementType() == MovementType.LOSS) {
            validateAvailableStock(product.getId(), dto.quantity());
        }

        // Get authenticated user
        User user = getAuthenticatedUser();

        // Create the movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(dto.movementType());
        movement.setQuantity(dto.quantity());
        movement.setResponsibleUser(user);
        movement.setReason(dto.reason());
        movement.setOriginType(dto.originType());
        movement.setOriginId(dto.originId());
        movement.setDateTime(LocalDateTime.now());

        // Save the movement
        StockMovement saved = movementRepository.save(movement);

        return mapToResponseDTO(saved);
    }

    /**
     * Register a stock entry
     */
    @Transactional
    public StockMovementResponseDTO registerEntry(String productId, Integer quantity, String reason) {
        StockMovementRequestDTO dto = new StockMovementRequestDTO(
                productId,
                MovementType.ENTRY,
                quantity,
                reason,
                "MANUAL",
                null
        );
        return registerMovement(dto);
    }

    /**
     * Register a stock exit
     */
    @Transactional
    public StockMovementResponseDTO registerExit(String productId, Integer quantity, String reason) {
        StockMovementRequestDTO dto = new StockMovementRequestDTO(
                productId,
                MovementType.EXIT,
                quantity,
                reason,
                "MANUAL",
                null
        );
        return registerMovement(dto);
    }

    /**
     * Register a stock adjustment (positive or negative)
     */
    @Transactional
    public StockMovementResponseDTO registerAdjustment(String productId, Integer targetQuantity, String reason) {
        // Calculate the difference between current stock and desired
        Integer currentStock = calculateCurrentStock(productId);
        Integer difference = targetQuantity - currentStock;

        if (difference == 0) {
            throw new RuntimeException("Stock is already at the desired value: " + targetQuantity);
        }

        StockMovementRequestDTO dto = new StockMovementRequestDTO(
                productId,
                MovementType.ADJUSTMENT,
                difference,
                reason,
                "INVENTORY_ADJUSTMENT",
                null
        );
        return registerMovement(dto);
    }

    /**
     * Register a stock loss
     */
    @Transactional
    public StockMovementResponseDTO registerLoss(String productId, Integer quantity, String reason) {
        StockMovementRequestDTO dto = new StockMovementRequestDTO(
                productId,
                MovementType.LOSS,
                quantity,
                reason,
                "MANUAL",
                null
        );
        return registerMovement(dto);
    }

    /**
     * Calculate current stock of a product based on movements
     */
    public Integer calculateCurrentStock(String productId) {
        Integer stock = movementRepository.calculateCurrentStock(productId);
        return stock != null ? stock : 0;
    }

    /**
     * Get current stock with detailed breakdown
     */
    public CurrentStockDTO getCurrentStock(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Integer currentStock = calculateCurrentStock(productId);
        Integer totalEntries = movementRepository.calculateTotalEntries(productId);
        Integer totalExits = movementRepository.calculateTotalExits(productId);
        Integer totalAdjustments = movementRepository.calculateTotalAdjustments(productId);
        Integer totalLosses = movementRepository.calculateTotalLosses(productId);

        return new CurrentStockDTO(
                product.getId(),
                product.getName(),
                currentStock,
                totalEntries != null ? totalEntries : 0,
                totalExits != null ? totalExits : 0,
                totalAdjustments != null ? totalAdjustments : 0,
                totalLosses != null ? totalLosses : 0
        );
    }

    /**
     * List all movements for a product
     */
    public List<StockMovementResponseDTO> listMovementsByProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return movementRepository.findByProductOrderByDateTimeDesc(product)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * List all movements
     */
    public List<StockMovementResponseDTO> listAllMovements() {
        return movementRepository.findAllByOrderByDateTimeDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * List movements by type
     */
    public List<StockMovementResponseDTO> listMovementsByType(MovementType type) {
        return movementRepository.findByMovementTypeOrderByDateTimeDesc(type)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validate if there is sufficient stock available
     */
    private void validateAvailableStock(String productId, Integer requestedQuantity) {
        Integer currentStock = calculateCurrentStock(productId);
        
        if (currentStock < requestedQuantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                    currentStock, requestedQuantity)
            );
        }
    }

    /**
     * Get authenticated user from security context
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("User not authenticated");
    }

    /**
     * Map entity to response DTO
     */
    private StockMovementResponseDTO mapToResponseDTO(StockMovement movement) {
        return new StockMovementResponseDTO(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getDateTime(),
                movement.getResponsibleUser().getName(),
                movement.getReason(),
                movement.getOriginType(),
                movement.getOriginId()
        );
    }
}
