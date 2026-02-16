package freelink.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")

public class DemoController {
    @GetMapping
    @PreAuthorize("hasRole('client')")
    public String hello(){
        return "hello first demo";
    } @GetMapping("/hello2")
    @PreAuthorize("hasRole('admin')")
    public String hello2(){
        return "hello first demo Admin";
    }
}
