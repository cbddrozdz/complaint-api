package pl.cbdd.complaintapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class GeoLocationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GeoLocationResponse geoLocationResponse;

    @InjectMocks
    private GeoLocationService geoLocationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCountryWhenIpIsValid() {
        doReturn("Poland").when(geoLocationResponse).getCountry();
        when(restTemplate.getForObject(anyString(), eq(GeoLocationResponse.class))).thenReturn(geoLocationResponse);

        String country = geoLocationService.getCountryByIp("127.0.0.1");

        assertEquals("Poland", country);
    }

    @Test
    void shouldReturnUnknownWhenNoCountryInResponse() {
        doReturn(null).when(geoLocationResponse).getCountry();
        when(restTemplate.getForObject(anyString(), eq(GeoLocationResponse.class))).thenReturn(geoLocationResponse);

        String country = geoLocationService.getCountryByIp("127.0.0.1");

        assertEquals("Unknown", country);
    }

    @Test
    void shouldReturnFallbackCountryWhenRestTemplateFails() {
        when(restTemplate.getForObject(anyString(), eq(GeoLocationResponse.class)))
                .thenThrow(new RuntimeException("API failure"));

        String country = geoLocationService.fallbackCountry("127.0.0.1", new RuntimeException());

        assertEquals("Fallback Country", country);
    }
}