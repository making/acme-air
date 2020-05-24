package com.acmeair;

import com.acmeair.entities.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.util.CollectionUtils.toMultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"logging.level.org.hibernate.SQL=INFO"})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AcmeairApplicationTests {
    private static final String USER_ID = "uid0@email.com";
    private static final String USER_PASSWORD = "password";

    private final TestRestTemplate restTemplate;

    AcmeairApplicationTests(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @BeforeEach
    void init() {
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity("/rest/api/loader/loadSmall", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody()).contains("Sample data loaded.");
    }

    @Test
    void testLoginLogout() throws Exception {
        final String sessionCookie = login(USER_ID, USER_PASSWORD);
        logout(sessionCookie);
    }

    @Test
    void testUpdateCustomer() throws Exception {
        final String sessionCookie = login(USER_ID, USER_PASSWORD);
        try {
            Customer customer = getCustomer(USER_ID, sessionCookie);
            final String address = "apt. " + System.currentTimeMillis();
            // update & check address info;
            customer.getAddress().setStreetAddress2(address);
            final Customer updatedCustomer = updateCustomer(USER_ID, sessionCookie, customer);
            assertThat(updatedCustomer.getAddress().getStreetAddress2()).isEqualTo(address);
            // double check if address info was updated
            customer = getCustomer(USER_ID, sessionCookie);
            assertThat(customer.getAddress().getStreetAddress2()).isEqualTo(address);
        } finally {
            logout(sessionCookie);
        }
    }

    @Test
    void testQueryFlights() throws Exception {
        final String sessionCookie = login(USER_ID, USER_PASSWORD);
        try {
            final JsonNode node = queryFlights(sessionCookie, "JFK", "CDG");
            assertThat(node.get("tripLegs").asInt()).isEqualTo(2);
            assertThat(node.get("tripFlights").isArray()).isTrue();
            assertThat(node.get("tripFlights").get(0).get("flightsOptions").size()).isGreaterThan(0);
            assertThat(node.get("tripFlights").get(1).get("flightsOptions").size()).isGreaterThan(0);
        } finally {
            logout(sessionCookie);
        }
    }

    private String login(String username, String password) throws Exception {
        final ResponseEntity<String> responseEntity = this.restTemplate.postForEntity("/rest/api/login",
                toMultiValueMap(Map.of("login", List.of(username), "password", List.of(password))),
                String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody()).contains("logged in");
        return responseEntity.getHeaders().getFirst(SET_COOKIE);
    }

    private void logout(String sessionCookie) throws Exception {
        final ResponseEntity<String> responseEntity = this.restTemplate.exchange("/rest/api/login/logout", GET,
                new HttpEntity<>(new HttpHeaders(toMultiValueMap(Map.of(COOKIE, List.of(sessionCookie))))),
                String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody()).contains("logged out");
    }

    private Customer getCustomer(String id, String sessionCookie) throws Exception {
        final ResponseEntity<Customer> responseEntity = this.restTemplate.exchange("/rest/api/customer/byid/{id}", GET,
                new HttpEntity<>(new HttpHeaders(toMultiValueMap(Map.of(COOKIE, List.of(sessionCookie))))),
                Customer.class, id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        return responseEntity.getBody();
    }

    private Customer updateCustomer(String id, String sessionCookie, Customer customer) throws Exception {
        final ResponseEntity<Customer> responseEntity = this.restTemplate.exchange("/rest/api/customer/byid/{id}", POST,
                new HttpEntity<>(customer, new HttpHeaders(toMultiValueMap(Map.of(COOKIE, List.of(sessionCookie))))),
                Customer.class, id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        return responseEntity.getBody();
    }

    private JsonNode queryFlights(String sessionCookie, String fromAirport, String toAirport) throws Exception {
        final ResponseEntity<JsonNode> responseEntity = this.restTemplate.exchange("/rest/api/flights/browseflights", POST,
                new HttpEntity<>(toMultiValueMap(Map.of("fromAirport", List.of(fromAirport), "toAirport", List.of(toAirport), "oneWay", List.of(false))),
                        new HttpHeaders(toMultiValueMap(Map.of(CONTENT_TYPE, List.of(APPLICATION_FORM_URLENCODED_VALUE, COOKIE), COOKIE, List.of(sessionCookie))))),
                JsonNode.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        return responseEntity.getBody();
    }
}
