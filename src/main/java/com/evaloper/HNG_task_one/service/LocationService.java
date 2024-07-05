package com.evaloper.HNG_task_one.service;


import com.evaloper.HNG_task_one.config.LocationServiceConfig;
import com.evaloper.HNG_task_one.response.HngResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Configuration
public class LocationService {
    private final LocationServiceConfig locationServiceConfig;




    public ResponseEntity<HngResponse> greetings(String visitor_name, HttpServletRequest request)  {
        String clearName = removeSurroundingQuotes(visitor_name);
        String clientIp = getClientIpAddress(request);
        String city = getIPInfoFromCity(clientIp);
        WeatherData weatherData = getWeatherData(city);
        double temp = weatherData.getTemperatureC();

        HngResponse visitor = new HngResponse();
        visitor.setClient_ip(clientIp);
        visitor.setLocation(city);
        visitor.setGreeting("Hello, " + clearName + "! , " + "the temperature is " + temp + " degrees Celsius in " + city);

        return ResponseEntity.ok(visitor);
    }

    private String removeSurroundingQuotes(String input) {
        Pattern pattern = Pattern.compile("^\"(.*)\"$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return input;
    }

    public String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = getIpFromHeaders(request);
        return ipAddress != null ? ipAddress : request.getRemoteAddr();
    }

    private String getIpFromHeaders(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Forwarded",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return null;
    }

    private String getIPInfoFromCity(String ipAddress) {
        String apiUrl = locationServiceConfig.getUrlForIPInfo() + ipAddress + "/json?token=" + locationServiceConfig.getToken();

        // Use Apache HttpClient 5 to send HTTP GET request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonResponse);

                    // Return the city from the IPinfo response
                    return jsonNode.path("city").asText("Unknown City");
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "Unknown City";
    }

    public WeatherData getWeatherData(String city) {
        WebClient webClient = WebClient.builder()
                .baseUrl(locationServiceConfig.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String apiUrl = "?key=" + locationServiceConfig.getApiKey() + "&q=" + city + "&aqi=no";

        try {
            String response = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Blocking to convert Mono to actual response (consider using non-blocking in real apps)

            JSONObject jsonResponse = new JSONObject(response);
            String locationName = jsonResponse.getJSONObject("location").getString("name");
            double temperatureC = jsonResponse.getJSONObject("current").getDouble("temp_c");

            return new WeatherData(locationName, temperatureC);

        } catch (WebClientResponseException e) {
            System.err.println("WebClient Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
}
