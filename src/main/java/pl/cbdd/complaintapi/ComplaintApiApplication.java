package pl.cbdd.complaintapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport
public class ComplaintApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComplaintApiApplication.class, args);
	}

}
