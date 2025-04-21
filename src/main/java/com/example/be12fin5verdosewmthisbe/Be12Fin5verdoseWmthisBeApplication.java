package com.example.be12fin5verdosewmthisbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class Be12Fin5verdoseWmthisBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Be12Fin5verdoseWmthisBeApplication.class, args);
    }

}
