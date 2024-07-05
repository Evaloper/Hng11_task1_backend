package com.evaloper.HNG_task_one.controller;

import com.evaloper.HNG_task_one.response.HngResponse;
import com.evaloper.HNG_task_one.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class HngController {
    @Autowired
    private LocationService locationService;

    @GetMapping("/hello")
    public ResponseEntity<HngResponse> greeting(@RequestParam(name = "visitor_name") String visitor_name, HttpServletRequest request){
        return locationService.greetings(visitor_name, request);
    }
}

