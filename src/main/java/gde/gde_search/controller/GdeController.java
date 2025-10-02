package gde.gde_search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GdeController {

    @GetMapping("/")
    public String root() {
        return "hello world";
    }

}
