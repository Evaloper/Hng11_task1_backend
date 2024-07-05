package com.evaloper.HNG_task_one.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@AllArgsConstructor
public class LocationServiceConfig {
    private final String apiUrl="http://api.weatherapi.com/v1/current.json";
    private final String urlForIPInfo="https://ipinfo.io/";
    private final String apiKey="5b90f5a61de044acadf220910240407";
    private final String token="7c56f3bae681de";
}
