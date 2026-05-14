package io.vobc.vobc_back.controller.web3;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class Web3TestController {

    @ResponseBody
    @GetMapping("/web3/test")
    public Map<String, Object> test(Authentication authentication) {
        return Map.of(
                "message", "authenticated",
                "principal", authentication.getPrincipal(),
                "authorities", authentication.getAuthorities()
        );
    }

}
