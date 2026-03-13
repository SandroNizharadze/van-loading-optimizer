package com.vanopt.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OptimizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/optimizations";

    private static final String VALID_REQUEST = """
            {
              "maxVolume": 15,
              "availableShipments": [
                { "name": "Parcel A", "volume": 5, "revenue": 120 },
                { "name": "Parcel B", "volume": 10, "revenue": 200 },
                { "name": "Parcel C", "volume": 3, "revenue": 80 },
                { "name": "Parcel D", "volume": 8, "revenue": 160 }
              ]
            }
            """;

    @Test
    void postValidRequestShouldReturn200WithCorrectResult() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.totalRevenue").value(320.0))
                .andExpect(jsonPath("$.totalVolume").value(15.0))
                .andExpect(jsonPath("$.selectedShipments", hasSize(2)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void postMissingMaxVolumeShouldReturn400() throws Exception {
        String body = """
                {
                  "availableShipments": [
                    { "name": "A", "volume": 5, "revenue": 100 }
                  ]
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input"));
    }

    @Test
    void postEmptyShipmentsShouldReturn400() throws Exception {
        String body = """
                { "maxVolume": 10, "availableShipments": [] }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input"));
    }

    @Test
    void postNegativeVolumeShouldReturn400() throws Exception {
        String body = """
                {
                  "maxVolume": 10,
                  "availableShipments": [
                    { "name": "A", "volume": -5, "revenue": 100 }
                  ]
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdShouldReturnPreviousResult() throws Exception {
        MvcResult postResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(postResult.getResponse().getContentAsString());
        String requestId = json.get("requestId").asText();

        mockMvc.perform(get(BASE_URL + "/" + requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.totalRevenue").value(320.0));
    }

    @Test
    void getNonExistentIdShouldReturn404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void getAllShouldReturnListOfResults() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void postWhenNoShipmentFitsShouldReturnEmptyResult() throws Exception {
        String body = """
                {
                  "maxVolume": 1,
                  "availableShipments": [
                    { "name": "A", "volume": 5, "revenue": 100 },
                    { "name": "B", "volume": 10, "revenue": 200 }
                  ]
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedShipments", hasSize(0)))
                .andExpect(jsonPath("$.totalRevenue").value(0.0))
                .andExpect(jsonPath("$.totalVolume").value(0.0));
    }
}
