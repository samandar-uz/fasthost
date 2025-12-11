package org.example.fasthost.repository;

import org.example.fasthost.entity.Orders;
import org.example.fasthost.entity.Users;
import org.example.fasthost.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    // Foydalanuvchining barcha buyurtmalarini olish
    List<Orders> findByUserOrderByCreateTimeDesc(Users user);

    // Foydalanuvchining faol buyurtmalarini olish
    List<Orders> findByUserAndStatus(Users user, OrderStatus status);

    // Muddati tugagan buyurtmalarni topish
    @Query("SELECT o FROM Orders o WHERE o.endTime < :now AND o.status = 'ACTIVE'")
    List<Orders> findExpiredOrders(LocalDateTime now);

    // Foydalanuvchining faol hostinglarini sanash
    long countByUserAndStatus(Users user, OrderStatus status);

    // Payment ID orqali topish
    Optional<Orders> findByPaymentId(String paymentId);

    // Domen nomi orqali topish
    Optional<Orders> findByDomainName(String domainName);

    // Oxirgi N kunlik buyurtmalar
    @Query("SELECT o FROM Orders o WHERE o.createTime >= :fromDate ORDER BY o.createTime DESC")
    List<Orders> findRecentOrders(LocalDateTime fromDate);
}