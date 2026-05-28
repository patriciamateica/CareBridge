package com.carebridge.backend.user;

import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.user.model.Role;
import com.carebridge.backend.user.model.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.carebridge.backend.user.UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private com.carebridge.backend.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private UserMapper userMapper;

    private RegisterRequest request;
    private User savedUser;
    private Role patientRole;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        request = new RegisterRequest(
            "Alice", "Smith", "alice@example.com", "securepass", "0744999888"
        );

        patientRole = new Role("PATIENT");

        savedUser = new User();
        savedUser.setId(userId);
        savedUser.setFirstName("Alice");
        savedUser.setLastName("Smith");
        savedUser.setEmail("alice@example.com");
        savedUser.setPhoneNumber("0744999888");
        savedUser.addRole(patientRole);
    }

    @Test
    void register_ShouldReturnCreatedUser() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(savedUser);
        // Create proper UserDto with all required fields
        UserDto expectedDto = new UserDto(
            userId,
            null, // activationNumber
            "Alice",
            "Smith",
            "alice@example.com",
            "0744999888",
            null, // dateOfBirth
            null, // residentialAddress
            null, // nationality
            java.util.Set.of("PATIENT"), // roles
            java.util.Set.of(), // permissions
            com.carebridge.backend.user.UserStatus.ACTIVE,
            true // isActive
        );
        when(userMapper.mapToDto(savedUser)).thenReturn(expectedDto);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Alice"))
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUsers_ShouldReturnPaginatedList() throws Exception {
        UserDto expectedDto = new UserDto(
            userId,
            null,
            "Alice",
            "Smith",
            "alice@example.com",
            "0744999888",
            null,
            null,
            null,
            java.util.Set.of("PATIENT"),
            java.util.Set.of(),
            com.carebridge.backend.user.UserStatus.ACTIVE,
            true
        );
        when(userService.getUsersPaginated(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(savedUser)));
        when(userMapper.mapToDto(savedUser)).thenReturn(expectedDto);

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }
}
