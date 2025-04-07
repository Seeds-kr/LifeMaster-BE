package com.example.LifeMaster_BE.Payment.PayPal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    @PostMapping("/create")
    public ResponseEntity<String> create() {
        String orderId = payPalService.createOrder();
        return ResponseEntity.ok(orderId);
    }

    @PostMapping("/capture/{orderId}")
    public ResponseEntity<String> capture(@PathVariable String orderId) {
        String result = payPalService.captureOrder(orderId);
        return ResponseEntity.ok(result);
    }
}


