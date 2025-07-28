package co.farmatodo.cv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class ShoppingCartApplication {

  @PostConstruct
  void started() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
  }

  public static void main(String[] args) {
    SpringApplication.run(ShoppingCartApplication.class, args);
  }

}
