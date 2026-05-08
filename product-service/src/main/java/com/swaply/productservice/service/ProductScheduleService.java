package com.swaply.productservice.service;

import com.swaply.productservice.repository.jpa.ProductRepository;
import com.swaply.productservice.utils.enums.ProductStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductScheduleService {

    private final ProductRepository productRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Baku")
    @Transactional
    public void checkAndExpireProducts() {
        log.info("Product status yoxlama prosesi başladı...");

        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            int updatedCount = productRepository.expireOldProducts(
                    thirtyDaysAgo,
                    ProductStatus.EXPIRED
            );

            log.info("Uğurlu bitdi. {} ədəd product EXPIRED statusuna keçirildi.", updatedCount);

        } catch (Exception e) {
            log.error("Status yeniləmə zamanı xəta:", e);
        }
    }

}
