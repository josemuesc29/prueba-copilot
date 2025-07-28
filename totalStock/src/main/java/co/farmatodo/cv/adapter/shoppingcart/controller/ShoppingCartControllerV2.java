package co.farmatodo.cv.adapter.shoppingcart.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ShoppingCartControllerV2 {

    @GetMapping("_ah/warmup")
    public String rootWarmup() {
        log.info("method: warmup(INFO)");
        return "Root warmup success.";
    }

}
