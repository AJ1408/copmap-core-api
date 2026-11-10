package in.copmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CopMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(CopMapApplication.class, args);
    }
}
