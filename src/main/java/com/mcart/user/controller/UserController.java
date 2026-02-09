package com.mcart.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/signup")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello. Everything's gonna be ok!");
    }
}
