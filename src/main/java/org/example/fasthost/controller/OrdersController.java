package org.example.fasthost.controller;

import lombok.RequiredArgsConstructor;
import org.example.fasthost.entity.Orders;
import org.example.fasthost.entity.Users;
import org.example.fasthost.entity.dto.OrderCreateRequest;
import org.example.fasthost.entity.dto.OrderCreateResponse;
import org.example.fasthost.entity.dto.Response;
import org.example.fasthost.service.OrdersService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    /**
     * Foydalanuvchining buyurtmalari ro'yxati (View)
     */
    @GetMapping
    public String ordersPage(@AuthenticationPrincipal Users user, Model model) {
        Response<List<Orders>> response = ordersService.getUserOrders(user);

        if (response.isSuccess()) {
            model.addAttribute("orders", response.getData());
        } else {
            model.addAttribute("error", response.getMessage());
        }

        return "orders";
    }

    /**
     * Buyurtma tafsilotlari sahifasi
     */
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Integer id,
                               @AuthenticationPrincipal Users user,
                               Model model,
                               RedirectAttributes redirect) {
        Response<Orders> response = ordersService.getOrderDetails(id, user);

        if (response.isSuccess()) {
            model.addAttribute("order", response.getData());
            return "order-details";
        } else {
            redirect.addFlashAttribute("error", response.getMessage());
            return "redirect:/orders";
        }
    }

    /**
     * Yangi buyurtma yaratish (API)
     */
    @PostMapping("/create")
    @ResponseBody
    public Response<OrderCreateResponse> createOrder(@RequestBody OrderCreateRequest request,
                                                     @AuthenticationPrincipal Users user) {
        return ordersService.createOrder(request, user);
    }

    /**
     * Yangi buyurtma yaratish (Form)
     */
    @PostMapping("/create-form")
    public String createOrderForm(@ModelAttribute OrderCreateRequest request,
                                  @AuthenticationPrincipal Users user,
                                  RedirectAttributes redirect) {
        Response<OrderCreateResponse> response = ordersService.createOrder(request, user);

        if (response.isSuccess()) {
            redirect.addFlashAttribute("success", "Buyurtma yaratildi! To'lovga o'ting.");
            redirect.addFlashAttribute("paymentUrl", response.getData().getPaymentUrl());
            return "redirect:/orders/" + response.getData().getOrderId();
        } else {
            redirect.addFlashAttribute("error", response.getMessage());
            return "redirect:/hosting";
        }
    }

    /**
     * To'lovni tasdiqlash (Callback URL)
     */
    @PostMapping("/confirm-payment")
    @ResponseBody
    public Response<Void> confirmPayment(@RequestParam String paymentId) {
        return ordersService.confirmPayment(paymentId);
    }

    /**
     * Buyurtmani bekor qilish
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id,
                              @AuthenticationPrincipal Users user,
                              RedirectAttributes redirect) {
        Response<Void> response = ordersService.cancelOrder(id, user);

        if (response.isSuccess()) {
            redirect.addFlashAttribute("success", response.getMessage());
        } else {
            redirect.addFlashAttribute("error", response.getMessage());
        }

        return "redirect:/orders";
    }

    /**
     * Buyurtmani uzaytirish
     */
    @PostMapping("/{id}/extend")
    public String extendOrder(@PathVariable Integer id,
                              @RequestParam Integer months,
                              @AuthenticationPrincipal Users user,
                              RedirectAttributes redirect) {
        Response<Void> response = ordersService.extendOrder(id, months, user);

        if (response.isSuccess()) {
            redirect.addFlashAttribute("success", response.getMessage());
        } else {
            redirect.addFlashAttribute("error", response.getMessage());
        }

        return "redirect:/orders/" + id;
    }

    /**
     * Buyurtmalar ro'yxati (REST API)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public Response<List<Orders>> getUserOrdersApi(@AuthenticationPrincipal Users user) {
        return ordersService.getUserOrders(user);
    }

    /**
     * Buyurtma tafsilotlari (REST API)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Response<Orders> getOrderDetailsApi(@PathVariable Integer id,
                                               @AuthenticationPrincipal Users user) {
        return ordersService.getOrderDetails(id, user);
    }
}