package com.autotrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndTrackingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanLoginAndRegisterVehicleLocation() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@autotrack.local",
                                  "password": "Admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        String vehiclesResponse = mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode vehicles = objectMapper.readTree(vehiclesResponse);
        long vehicleId = vehicles.get(0).get("id").asLong();

        mockMvc.perform(post("/api/tracking/vehicles/{vehicleId}/locations", vehicleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": -12.046374,
                                  "longitude": -77.042793,
                                  "speed": 55.5,
                                  "heading": 145.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId))
                .andExpect(jsonPath("$.speed").value(55.5));
    }
}
