package nz.ac.canterbury.seng302.homehelper.integration.controller.auth;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LogoutIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockHttpSession authenticationSession;

    @BeforeEach
    public void setup() throws Exception {
        String testUserPassword = "Secure_Pass2";
        String encryptedPassword = passwordEncoder.encode(testUserPassword);
        User testUser = new User("Jane", "Smith", "jane.smith@example.com", encryptedPassword, encryptedPassword);
        testUser.setActivated(true);
        userRepository.save(testUser);

        // create auth session
        MvcResult mvcResult = mockMvc.perform(post("/do_login")
                        .with(csrf())
                        .param("email", testUser.getEmail())
                        .param("password", testUserPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        authenticationSession = (MockHttpSession) mvcResult.getRequest().getSession();
        assertNotNull(authenticationSession);
        assertFalse(authenticationSession.isInvalid());
    }

    @Test
    void testLogout_WhenLogoutWithNoSession_ThenRedirectToLoginPage() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testLogout_WhenLogoutWithInvalidSession_ThenRedirectToLoginPage() throws Exception {
        // manually invalidate session
        authenticationSession.invalidate();
        assertTrue(authenticationSession.isInvalid());

        mockMvc.perform(get("/logout").session(authenticationSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // make sure the session is still invalid
        assertTrue(authenticationSession.isInvalid());
    }

    @Test
    void testLogout_WhenLogout_ThenSessionShouldInvalidate() throws Exception {
        mockMvc.perform(get("/logout")
                        .session(authenticationSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertTrue(authenticationSession.isInvalid());
    }

    @Test
    void testLogout_WhenConsecutiveLogout_ThenAllShouldSucceed() throws Exception {
        // logout 5 times
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/logout")
                            .session(authenticationSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
            assertTrue(authenticationSession.isInvalid());
        }
    }

    @Test
    void testLogout_WhenLogoutAndAccessProtectedPageWithOldSession_ThenShouldRedirectToLoginPage() throws Exception {
        mockMvc.perform(get("/logout")
                        .session(authenticationSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/userPage")
                        .session(authenticationSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/register", "/forgotten-password", "/verification"})
    void testLogout_WhenLogoutAndAccessPublicPages_ThenShouldAllowAccess(String path) throws Exception {
        mockMvc.perform(get("/logout")
                        .session(authenticationSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get(path)
                        .session(authenticationSession))
                .andExpect(status().isOk());
    }

    // UserAdvice
    @Test
    void testLogout_WhenLogout_ThenUserIsNotAddedToModel() throws Exception {
        mockMvc.perform(get("/")
                        .session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isAuthenticated", true));

        mockMvc.perform(get("/logout")
                        .session(authenticationSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/")
                        .session(authenticationSession))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("user"))
                .andExpect(model().attribute("isAuthenticated", false));

    }
}
