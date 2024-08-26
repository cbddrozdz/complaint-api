package pl.cbdd.complaintapi.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "geoLocationService", fallbackMethod = "fallbackCountry")
    @Retry(name = "geoLocationServiceRetry")
    public String getCountryByIp(String ip) {
        String url = UriComponentsBuilder.fromHttpUrl("http://ip-api.com/json/" + ip)
                .queryParam("fields", "country")
                .toUriString();

        GeoLocationResponse response = restTemplate.getForObject(url, GeoLocationResponse.class);
        return response != null && response.getCountry() != null ? response.getCountry() : "Unknown";
    }

    public String fallbackCountry(String ip, Throwable t) {
        return "Fallback Country";
    }
}

@Setter
@Getter
class GeoLocationResponse {
    private String country;
}