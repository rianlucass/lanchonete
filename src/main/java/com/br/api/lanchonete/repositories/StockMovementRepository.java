package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.stock.StockMovement;
import com.br.api.lanchonete.domain.stock.MovementType;
import com.br.api.lanchonete.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, String> {

    /**
     * List all movements for a product ordered by date
     */
    List<StockMovement> findByProductOrderByDateTimeDesc(Product product);

    /**
     * List movements for a product by type
     */
    List<StockMovement> findByProductAndMovementTypeOrderByDateTimeDesc(
            Product product, 
            MovementType movementType
    );

    /**
     * List movements in a specific period
     */
    @Query("SELECT m FROM StockMovement m WHERE m.product = :product " +
           "AND m.dateTime BETWEEN :start AND :end ORDER BY m.dateTime DESC")
    List<StockMovement> findByProductAndPeriod(
            @Param("product") Product product,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Calculate current stock based on movements
     * ENTRY and positive ADJUSTMENT add
     * EXIT and LOSS subtract
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN m.movementType = 'ENTRY' THEN m.quantity ELSE 0 END), 0) + " +
           "COALESCE(SUM(CASE WHEN m.movementType = 'ADJUSTMENT' AND m.quantity > 0 THEN m.quantity ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN m.movementType = 'EXIT' THEN m.quantity ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN m.movementType = 'LOSS' THEN m.quantity ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN m.movementType = 'ADJUSTMENT' AND m.quantity < 0 THEN ABS(m.quantity) ELSE 0 END), 0) " +
           "FROM StockMovement m WHERE m.product.id = :productId")
    Integer calculateCurrentStock(@Param("productId") String productId);

    /**
     * Calculate total entries for a product
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
           "WHERE m.product.id = :productId AND m.movementType = 'ENTRY'")
    Integer calculateTotalEntries(@Param("productId") String productId);

    /**
     * Calculate total exits for a product
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
           "WHERE m.product.id = :productId AND m.movementType = 'EXIT'")
    Integer calculateTotalExits(@Param("productId") String productId);

    /**
     * Calculate total adjustments for a product (can be positive or negative)
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
           "WHERE m.product.id = :productId AND m.movementType = 'ADJUSTMENT'")
    Integer calculateTotalAdjustments(@Param("productId") String productId);

    /**
     * Calculate total losses for a product
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
           "WHERE m.product.id = :productId AND m.movementType = 'LOSS'")
    Integer calculateTotalLosses(@Param("productId") String productId);

    /**
     * List all movements ordered by date (most recent first)
     */
    List<StockMovement> findAllByOrderByDateTimeDesc();

    /**
     * List movements by type
     */
    List<StockMovement> findByMovementTypeOrderByDateTimeDesc(MovementType movementType);
}
