package com.br.api.lanchonete.controllers;

import com.br.api.lanchonete.domain.stock.*;
import com.br.api.lanchonete.services.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * Register a generic stock movement
     */
    @PostMapping("/movement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMovementResponseDTO> registerMovement(
            @Valid @RequestBody StockMovementRequestDTO dto) {
        try {
            StockMovementResponseDTO response = stockService.registerMovement(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register a stock entry
     */
    @PostMapping("/entry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMovementResponseDTO> registerEntry(
            @RequestBody Map<String, Object> request) {
        try {
            String productId = (String) request.get("productId");
            Integer quantity = (Integer) request.get("quantity");
            String reason = (String) request.get("reason");

            if (productId == null || quantity == null || reason == null) {
                return ResponseEntity.badRequest().build();
            }

            StockMovementResponseDTO response = stockService.registerEntry(productId, quantity, reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register a stock exit
     */
    @PostMapping("/exit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMovementResponseDTO> registerExit(
            @RequestBody Map<String, Object> request) {
        try {
            String productId = (String) request.get("productId");
            Integer quantity = (Integer) request.get("quantity");
            String reason = (String) request.get("reason");

            if (productId == null || quantity == null || reason == null) {
                return ResponseEntity.badRequest().build();
            }

            StockMovementResponseDTO response = stockService.registerExit(productId, quantity, reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register a stock adjustment
     * Receives the FINAL desired quantity and calculates the necessary adjustment
     */
    @PostMapping("/adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMovementResponseDTO> registerAdjustment(
            @RequestBody Map<String, Object> request) {
        try {
            String productId = (String) request.get("productId");
            Integer targetQuantity = (Integer) request.get("targetQuantity");
            String reason = (String) request.get("reason");

            if (productId == null || targetQuantity == null || reason == null) {
                return ResponseEntity.badRequest().build();
            }

            StockMovementResponseDTO response = stockService.registerAdjustment(productId, targetQuantity, reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register a stock loss
     */
    @PostMapping("/loss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMovementResponseDTO> registerLoss(
            @RequestBody Map<String, Object> request) {
        try {
            String productId = (String) request.get("productId");
            Integer quantity = (Integer) request.get("quantity");
            String reason = (String) request.get("reason");

            if (productId == null || quantity == null || reason == null) {
                return ResponseEntity.badRequest().build();
            }

            StockMovementResponseDTO response = stockService.registerLoss(productId, quantity, reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current stock of a product with detailed breakdown
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrentStockDTO> getCurrentStock(@PathVariable String productId) {
        try {
            CurrentStockDTO stock = stockService.getCurrentStock(productId);
            return ResponseEntity.ok(stock);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List all movements for a product
     */
    @GetMapping("/movements/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockMovementResponseDTO>> listMovementsByProduct(
            @PathVariable String productId) {
        try {
            List<StockMovementResponseDTO> movements = 
                stockService.listMovementsByProduct(productId);
            return ResponseEntity.ok(movements);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List all movements
     */
    @GetMapping("/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockMovementResponseDTO>> listAllMovements() {
        List<StockMovementResponseDTO> movements = stockService.listAllMovements();
        return ResponseEntity.ok(movements);
    }

    /**
     * List movements by type
     */
    @GetMapping("/movements/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockMovementResponseDTO>> listMovementsByType(
            @PathVariable String type) {
        try {
            MovementType typeEnum = MovementType.valueOf(type.toUpperCase());
            List<StockMovementResponseDTO> movements = 
                stockService.listMovementsByType(typeEnum);
            return ResponseEntity.ok(movements);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
