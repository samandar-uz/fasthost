package org.example.fasthost.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fasthost.entity.Orders;
import org.example.fasthost.entity.Tariffs;
import org.example.fasthost.entity.Users;
import org.example.fasthost.entity.dto.OrderCreateRequest;
import org.example.fasthost.entity.dto.OrderCreateResponse;
import org.example.fasthost.entity.dto.Response;
import org.example.fasthost.entity.enums.OrderStatus;
import org.example.fasthost.repository.OrdersRepository;
import org.example.fasthost.repository.TariffsRepository;
import org.example.fasthost.repository.UsersRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final TariffsRepository tariffsRepository;
    private final UsersRepository usersRepository;

    /* =========================
       CREATE ORDER
       ========================= */
    @Transactional
    public Response<OrderCreateResponse> createOrder(OrderCreateRequest request, Users user) {

        Tariffs tariff = tariffsRepository.findById(request.getTariffId())
                .orElse(null);

        if (tariff == null || !Boolean.TRUE.equals(tariff.getActive())) {
            return Response.error("Tarif topilmadi yoki faol emas");
        }

        int days = request.getDurationMonths();
        if (days <= 0 || days > 1095) {
            return Response.error("Noto‘g‘ri muddat");
        }

        BigDecimal totalPrice = calculatePrice(tariff.getPrice(), days);

        if (user.getBalance().compareTo(totalPrice) < 0) {
            return Response.error("Balansingizda yetarli mablag‘ yo‘q");
        }

        Orders order = Orders.builder()
                .user(user)
                .tariff(tariff)
                .durationDays(days)
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .login(generateLogin())
                .passwordHash("TEMP") // realda encoder bilan
                .build();

        ordersRepository.save(order);

        OrderCreateResponse response = new OrderCreateResponse(
                order.getId(),
                totalPrice,
                order.getEndTime()
        );

        log.info("Order created: id={}, user={}, tariff={}",
                order.getId(), user.getEmail(), tariff.getName());

        return Response.success(response);
    }

    public Response<List<Orders>> getUserOrders(Users user) {

        List<Orders> orders = ordersRepository
                .findByUserOrderByCreateTimeDesc(user);

        return Response.success(orders);
    }

    /* =========================
       EXTEND ORDER (DAYS)
       ========================= */
    @Transactional
    public Response<Void> extendOrder(Long orderId, int additionalDays, Users user) {

        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (order == null) {
            return Response.error("Buyurtma topilmadi");
        }

        if (!order.getUser().getId().equals(user.getId())) {
            return Response.error("Ruxsat yo‘q");
        }

        if (order.getStatus() != OrderStatus.ACTIVE) {
            return Response.error("Faqat faol buyurtma uzaytiriladi");
        }

        BigDecimal price = calculatePrice(order.getTariff().getPrice(), additionalDays);

        if (user.getBalance().compareTo(price) < 0) {
            return Response.error("Balans yetarli emas");
        }

        user.setBalance(user.getBalance().subtract(price));
        usersRepository.save(user);

        order.setDurationDays(order.getDurationDays() + additionalDays);
        order.setEndTime(order.getEndTime().plusDays(additionalDays));
        order.setTotalPrice(order.getTotalPrice().add(price));

        ordersRepository.save(order);

        return Response.success();
    }

    /* =========================
       EXPIRE CHECK (CRON)
       ========================= */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireOrders() {

        List<Orders> expired =
                ordersRepository.findExpiredOrders(LocalDateTime.now());

        for (Orders order : expired) {
            order.setStatus(OrderStatus.EXPIRED);
        }

        ordersRepository.saveAll(expired);

        log.info("Expired orders count: {}", expired.size());
    }

    /* =========================
       HELPERS
       ========================= */

    private BigDecimal calculatePrice(BigDecimal dailyPrice, int days) {
        BigDecimal price = dailyPrice.multiply(BigDecimal.valueOf(days));

        if (days >= 365) {
            price = price.multiply(BigDecimal.valueOf(0.85));
        } else if (days >= 180) {
            price = price.multiply(BigDecimal.valueOf(0.90));
        } else if (days >= 90) {
            price = price.multiply(BigDecimal.valueOf(0.95));
        }

        return price;
    }

    private String generateLogin() {
        return "host_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
