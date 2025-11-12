package gde.gde_search.controller;

import gde.gde_search.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-error")
public class ErrorTestController {

    @GetMapping
    public ResponseEntity<String> testError(@RequestParam String code) {
        switch (code) {
            case "400":
                throw new BadRequestException("Bad request test");
            case "404":
                throw new ResourceNotFoundException("Resource not found test");
            case "409":
                throw new ConflictException("Conflict test");
            case "422":
                throw new UnprocessableEntityException("Unprocessable entity test");
            case "500":
                throw new InternalServerException("Internal server error test");
            case "503":
                throw new ServiceUnavailableException("Service unavailable test");
            case "502":
                throw new BadGatewayException("Bad gateway test");
            default:
                return ResponseEntity.ok("No error");
        }
    }
}