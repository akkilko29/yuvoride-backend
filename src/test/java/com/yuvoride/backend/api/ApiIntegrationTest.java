package com.yuvoride.backend.api;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import com.yuvoride.backend.security.JwtService;
import com.yuvoride.backend.service.OtpCodeGenerator;
import com.yuvoride.backend.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;
    @Autowired
    private JwtService jwtService;
    @MockitoBean
    private RouteService routeService;
    @MockitoBean
    private OtpCodeGenerator otpCodeGenerator;

    @BeforeEach
    void setUp() {
        when(otpCodeGenerator.generate(6)).thenReturn("123456");
        when(routeService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteResult(65.4, 95));
    }

    @Test
    void sendAndVerifyOtpCreatesUser() throws Exception {
        mockMvc.perform(post("/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"+919999999999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        mockMvc.perform(post("/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"+919999999999\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.phone").value("+919999999999"));
    }

    @Test
    void vehiclesArePublicAndSeeded() throws Exception {
        mockMvc.perform(get("/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Rapid"))
                .andExpect(jsonPath("$.data[1].perKmRate").isNumber());
    }

    @Test
    void unauthorizedUsersMeIsRejected() throws Exception {
        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void customerCannotAccessAnotherCustomersRide() throws Exception {
        User alice = userRepository.save(User.builder()
                .name("Alice")
                .phone("++1-555-0044")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build());
        User bob = userRepository.save(User.builder()
                .name("Bob")
                .phone("++1-555-0030")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build());
        VehicleType mini = vehicleTypeRepository.findByNameIgnoreCase("Mini").orElseThrow();
        Ride ride = rideRepository.save(Ride.builder()
                .customer(alice)
                .pickupLatitude(25.59)
                .pickupLongitude(85.13)
                .pickupAddress("Patna")
                .destinationLatitude(26.11)
                .destinationLongitude(85.39)
                .destinationAddress("Muzaffarpur")
                .vehicleType(mini)
                .distanceKm(65.4)
                .durationMinutes(95)
                .estimatedFare(new BigDecimal("1031"))
                .status(RideStatus.SEARCHING_DRIVER)
                .build());

        mockMvc.perform(get("/v1/rides/" + ride.getId())
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(bob)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAndCancelRideUsesServerFare() throws Exception {
        User customer = userRepository.save(User.builder()
                .name("Rider")
                .phone("+919111111111")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build());
        String token = jwtService.generateAccessToken(customer);
        String body = """
                {
                  "pickup": {"latitude": 25.5941, "longitude": 85.1376, "address": "Patna"},
                  "destination": {"latitude": 26.1197, "longitude": 85.3910, "address": "Muzaffarpur"},
                  "vehicleTypeId": %d
                }
                """.formatted(vehicleTypeRepository.findByNameIgnoreCase("Mini").orElseThrow().getId());

        String createJson = mockMvc.perform(post("/v1/rides")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estimatedFare").value(1031))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode created = jsonMapper.readTree(createJson);
        long rideId = created.path("data").path("id").asLong();
        assertThat(created.path("data").path("status").asText()).isEqualTo("REQUESTED");

        mockMvc.perform(post("/v1/rides/" + rideId + "/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed my plan\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RIDE_CANCELLED"));
    }

    @Test
    void cannotChangeRoleViaProfileUpdate() throws Exception {
        User customer = userRepository.save(User.builder()
                .name("Rider")
                .phone("+919222222222")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build());
        mockMvc.perform(patch("/v1/users/me")
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    void googleLoginWithoutConfigFailsClearly() throws Exception {
        mockMvc.perform(post("/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"not-a-real-token\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false));
    }
}
