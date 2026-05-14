package com.pharmacy.pharmacy_management;

/**
 * Main application entry point for the Pharmacy Management System.
 * 
 * This class serves as the starting point for the Spring Boot application.
 * It contains the main() method which is the entry point for any Java application.
 * 
 * The @SpringBootApplication annotation is a convenience annotation that combines:
 * - @Configuration: Marks the class as a source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration mechanism
 * - @ComponentScan: Enables component scanning in the current package and sub-packages
 * 
 * scanBasePackages is set to "com.pharmacy.pharmacy_management" to ensure only
 * classes within this package and its sub-packages are scanned for components.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Pharmacy Management System.
 * This class initializes and starts the Spring Boot application context.
 */
@SpringBootApplication(scanBasePackages = "com.pharmacy.pharmacy_management")
public class PharmacyManagementApplication {

    /**
     * Main method - Application entry point.
     * 
     * This is the first method that gets executed when the application starts.
     * It delegates to the Spring Boot's SpringApplication class to:
     * 1. Create an ApplicationContext (Spring container)
     * 2. Automatically configure the application based on classpath and properties
     * 3. Start the embedded server (Tomcat by default)
     * 4. Scan for and register all Spring components, services, and controllers
     * 
     * @param args Command line arguments passed to the application
     *              These can be used to override default application properties
     */
	public static void main(String[] args) {
        // SpringApplication.run() is a static method that launches the Spring Boot application.
        // It takes two parameters:
        // - The primary source configuration class (this class, which has @SpringBootApplication)
        // - Command-line arguments (args)
        // 
        // The method returns the running ApplicationContext which can be used to access beans
        // but typically we don't need to use the return value in the main method.
		SpringApplication.run(PharmacyManagementApplication.class, args);
	}

}
