package com.example.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.example.be12fin5verdosewmthisbe.order.model",
        "com.example.be12fin5verdosewmthisbe.store.model",
        "com.example.be12fin5verdosewmthisbe.menu_management.menu.model",
        "com.example.be12fin5verdosewmthisbe.menu_management.option.model",
        "com.example.be12fin5verdosewmthisbe.user.model",
        "com.example.be12fin5verdosewmthisbe.menu_management.category.model",
        "com.example.be12fin5verdosewmthisbe.menu_management.menu.model",
        "com.example.be12fin5verdosewmthisbe.inventory.model",
        "com.example.be12fin5verdosewmthisbe.market_management.market.model"
})

public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

}
