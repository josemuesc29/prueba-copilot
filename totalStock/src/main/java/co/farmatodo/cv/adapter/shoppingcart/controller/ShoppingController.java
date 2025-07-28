package co.farmatodo.cv.adapter.shoppingcart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;

@Slf4j
@RestController
@RequestMapping("/shopping-cart/v1")
public class ShoppingController {

  @GetMapping("/")
  public String root() {
    log.trace("method: root(TRACE)");
    log.debug("method: root(DEBUG)");
    log.info("method: root(INFO)");
    log.warn("method: root(WARN)");
    log.error("method: root(ERROR)");
    return "Root success.";
  }

  @GetMapping("/ping")
  public String ping() {
    log.info("method: ping()");
    return "Ping success.";
  }

}
