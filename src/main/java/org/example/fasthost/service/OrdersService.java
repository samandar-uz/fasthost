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

    /**
     * Yangi buyurtma yaratish
     */
    @Transactional
    public Response<OrderCreateResponse> createOrder(OrderCreateRequest request, Users user) {
        try {
            // Tarifni tekshirish
            Tariffs tariff = tariffsRepository.findById(request.getTariffId())
                    .orElse(null);

            if (tariff == null || !tariff.getActive()) {
                return Response.error("Tarif topilmadi yoki faol emas");
            }

            // Narxni hisoblash
            Double totalPrice = calculatePrice(tariff.getPrice(), request.getDurationMonths());

            // Balansni tekshirish
            if (user.getBalance().doubleValue() < totalPrice) {
                return Response.error("Balansingizda yetarli mablag' yo'q");
            }

            // Domen nomini tekshirish (agar berilgan bo'lsa)
            if (request.getDomainName() != null && !request.getDomainName().isEmpty()) {
                if (ordersRepository.findByDomainName(request.getDomainName()).isPresent()) {
                    return Response.error("Bu domen nomi allaqachon band");
                }
            }

            // Database ma'lumotlarini generatsiya qilish
            String dbName = generateDatabaseName();
            String dbUser = generateDatabaseUser();
            String dbPassword = generateDatabasePassword();

            // Buyurtma yaratish
            Orders order = Orders.builder()
                    .user(user)
                    .tariff(tariff)
                    .durationMonths(request.getDurationMonths())
                    .totalPrice(totalPrice)
                    .status(OrderStatus.PENDING)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now().plusMonths(request.getDurationMonths()))
                    .domainName(request.getDomainName())
                    .databaseName(dbName)
                    .databaseUser(dbUser)
                    .databasePassword(dbPassword)
                    .cronJobsUsed(0)
                    .build();

            ordersRepository.save(order);

            // To'lov URL'ini generatsiya qilish (Payme/Click/Payze)
            String paymentId = generatePaymentId();
            order.setPaymentId(paymentId);
            ordersRepository.save(order);

            String paymentUrl = generatePaymentUrl(paymentId, totalPrice);

            log.info("Yangi buyurtma yaratildi: Order ID={}, User={}, Tariff={}",
                    order.getId(), user.getEmail(), tariff.getName());

            OrderCreateResponse responseData = new OrderCreateResponse(
                    order.getId(),
                    totalPrice,
                    paymentUrl,
                    paymentId
            );

            return Response.<OrderCreateResponse>builder()
                    .success(true)
                    .message("Buyurtma muvaffaqiyatli yaratildi")
                    .data(responseData)
                    .build();

        } catch (Exception e) {
            log.error("Buyurtma yaratishda xatolik: {}", e.getMessage());
            return Response.error("Buyurtma yaratishda xatolik yuz berdi");
        }
    }

    /**
     * To'lovni tasdiqlash
     */
    @Transactional
    public Response<Void> confirmPayment(String paymentId) {
        try {
            Orders order = ordersRepository.findByPaymentId(paymentId)
                    .orElse(null);

            if (order == null) {
                return Response.error("Buyurtma topilmadi");
            }

            if (order.getStatus() == OrderStatus.ACTIVE) {
                return Response.error("Buyurtma allaqachon faollashtirilgan");
            }

            // Foydalanuvchi balansidan ayirish
            Users user = order.getUser();
            user.setBalance(user.getBalance().subtract(
                    java.math.BigDecimal.valueOf(order.getTotalPrice())
            ));
            usersRepository.save(user);

            // Buyurtmani faollashtirish
            order.setStatus(OrderStatus.ACTIVE);
            ordersRepository.save(order);

            log.info("To'lov tasdiqlandi: Order ID={}, Payment ID={}",
                    order.getId(), paymentId);

            return Response.<Void>builder()
                    .success(true)
                    .message("To'lov muvaffaqiyatli tasdiqlandi")
                    .build();

        } catch (Exception e) {
            log.error("To'lovni tasdiqlashda xatolik: {}", e.getMessage());
            return Response.error("To'lovni tasdiqlashda xatolik yuz berdi");
        }
    }

    /**
     * Buyurtmani bekor qilish
     */
    @Transactional
    public Response<Void> cancelOrder(Integer orderId, Users user) {
        try {
            Orders order = ordersRepository.findById(orderId).orElse(null);

            if (order == null) {
                return Response.error("Buyurtma topilmadi");
            }

            if (!order.getUser().getId().equals(user.getId())) {
                return Response.error("Bu buyurtma sizga tegishli emas");
            }

            if (order.getStatus() == OrderStatus.ACTIVE) {
                return Response.error("Faol buyurtmani bekor qilish mumkin emas");
            }

            order.setStatus(OrderStatus.CANCELED);
            ordersRepository.save(order);

            log.info("Buyurtma bekor qilindi: Order ID={}, User={}",
                    orderId, user.getEmail());

            return Response.<Void>builder()
                    .success(true)
                    .message("Buyurtma bekor qilindi")
                    .build();

        } catch (Exception e) {
            log.error("Buyurtmani bekor qilishda xatolik: {}", e.getMessage());
            return Response.error("Buyurtmani bekor qilishda xatolik yuz berdi");
        }
    }

    /**
     * Foydalanuvchining buyurtmalarini olish
     */
    public Response<List<Orders>> getUserOrders(Users user) {
        try {
            List<Orders> orders = ordersRepository.findByUserOrderByCreateTimeDesc(user);

            return Response.<List<Orders>>builder()
                    .success(true)
                    .data(orders)
                    .build();

        } catch (Exception e) {
            log.error("Buyurtmalarni olishda xatolik: {}", e.getMessage());
            return Response.error("Buyurtmalarni olishda xatolik yuz berdi");
        }
    }

    /**
     * Buyurtma tafsilotlarini olish
     */
    public Response<Orders> getOrderDetails(Integer orderId, Users user) {
        try {
            Orders order = ordersRepository.findById(orderId).orElse(null);

            if (order == null) {
                return Response.error("Buyurtma topilmadi");
            }

            if (!order.getUser().getId().equals(user.getId())) {
                return Response.error("Bu buyurtma sizga tegishli emas");
            }

            return Response.<Orders>builder()
                    .success(true)
                    .data(order)
                    .build();

        } catch (Exception e) {
            log.error("Buyurtma tafsilotlarini olishda xatolik: {}", e.getMessage());
            return Response.error("Buyurtma tafsilotlarini olishda xatolik yuz berdi");
        }
    }

    /**
     * Muddati tugagan buyurtmalarni tekshirish va yangilash
     * Har kuni soat 00:00 da ishga tushadi
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkExpiredOrders() {
        log.info("Muddati tugagan buyurtmalarni tekshirish boshlandi");

        List<Orders> expiredOrders = ordersRepository.findExpiredOrders(LocalDateTime.now());

        for (Orders order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            ordersRepository.save(order);

            log.info("Buyurtma muddati tugadi: Order ID={}, User={}",
                    order.getId(), order.getUser().getEmail());
        }

        log.info("Jami {} ta buyurtma muddati tugadi", expiredOrders.size());
    }

    /**
     * Buyurtmani uzaytirish
     */
    @Transactional
    public Response<Void> extendOrder(Integer orderId, Integer additionalMonths, Users user) {
        try {
            Orders order = ordersRepository.findById(orderId).orElse(null);

            if (order == null) {
                return Response.error("Buyurtma topilmadi");
            }

            if (!order.getUser().getId().equals(user.getId())) {
                return Response.error("Bu buyurtma sizga tegishli emas");
            }

            if (order.getStatus() != OrderStatus.ACTIVE) {
                return Response.error("Faqat faol buyurtmalarni uzaytirish mumkin");
            }

            // Qo'shimcha narxni hisoblash
            Double extensionPrice = calculatePrice(order.getTariff().getPrice(), additionalMonths);

            // Balansni tekshirish
            if (user.getBalance().doubleValue() < extensionPrice) {
                return Response.error("Balansingizda yetarli mablag' yo'q");
            }

            // Balansdan ayirish
            user.setBalance(user.getBalance().subtract(
                    java.math.BigDecimal.valueOf(extensionPrice)
            ));
            usersRepository.save(user);

            // Muddatni uzaytirish
            order.setEndTime(order.getEndTime().plusMonths(additionalMonths));
            order.setDurationMonths(order.getDurationMonths() + additionalMonths);
            order.setTotalPrice(order.getTotalPrice() + extensionPrice);
            ordersRepository.save(order);

            log.info("Buyurtma uzaytirildi: Order ID={}, Additional Months={}",
                    orderId, additionalMonths);

            return Response.<Void>builder()
                    .success(true)
                    .message("Buyurtma muvaffaqiyatli uzaytirildi")
                    .build();

        } catch (Exception e) {
            log.error("Buyurtmani uzaytirishda xatolik: {}", e.getMessage());
            return Response.error("Buyurtmani uzaytirishda xatolik yuz berdi");
        }
    }

    // Helper metodlar
    private Double calculatePrice(Double basePrice, Integer months) {
        // Chegirma tizimi: 3 oy - 5%, 6 oy - 10%, 12 oy - 15%
        double discount = 0;
        if (months >= 12) {
            discount = 0.15;
        } else if (months >= 6) {
            discount = 0.10;
        } else if (months >= 3) {
            discount = 0.05;
        }

        return basePrice * months * (1 - discount);
    }

    private String generateDatabaseName() {
        return "db_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateDatabaseUser() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateDatabasePassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generatePaymentId() {
        return "PAY_" + UUID.randomUUID().toString();
    }

    private String generatePaymentUrl(String paymentId, Double amount) {
        // Real loyihada bu Payme/Click/Payze URL'i bo'ladi
        return "https://payment.uzhost.uz/pay?id=" + paymentId + "&amount=" + amount;
    }
}