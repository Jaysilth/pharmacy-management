package com.pharmacy.pharmacy_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI pharmacyOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("Pharmacy Management System");
        contact.setEmail("support@pharmacy.com");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Pharmacy Management System API")
                .version("1.0.0")
                .description("Production-ready REST API for Pharmacy Management System. " +
                        "This API provides endpoints for managing medicines, inventory, " +
                        "and sales transactions in a pharmacy/POS environment. " +
                        "Inspired by Odoo Pharmacy/POS module architecture.")
                .contact(contact)
                .license(license)
                .termsOfService("Terms of Service URL");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}