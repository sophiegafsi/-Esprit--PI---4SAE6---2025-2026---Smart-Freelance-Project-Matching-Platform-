package tn.esprit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devops")
public class DevOpsController {

    @GetMapping("/sentry-test")
    public String triggerSentryError() {
        throw new IllegalStateException("Sentry test exception");
    }
}
