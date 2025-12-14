package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RenovationLocationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    private Renovation renovation;

    @BeforeEach
    void setUpDatabase() {
        User user = new User("John", "Test", "john@dude.com", "Abc123!!", "Abc123!!");
        userRepository.save(user);
    }

    @Test
    void createRenovation_ValidAddress_Successful() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        MvcResult result = mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Renovation createdRenovation = renovationRepository.findByNameAndUser("Test Renovation", user).get(0);
        assertEquals("/renovation/" + createdRenovation.getId(), result.getResponse().getRedirectedUrl());
    }

    @Test
    void createRenovation_ValidAddress_AllFieldsEmpty() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        MvcResult result = mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "")
                        .param("country", "")
                        .param("postcode", "")
                        .param("city", "")
                        .param("suburb", "")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Renovation createdRenovation = renovationRepository.findByNameAndUser("Test Renovation", user).get(0);
        assertEquals("/renovation/" + createdRenovation.getId(), result.getResponse().getRedirectedUrl());
    }

    @Test
    void createRenovation_ValidAddressWithEmptySuburb_Successful() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        MvcResult result = mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Renovation createdRenovation = renovationRepository.findByNameAndUser("Test Renovation", user).get(0);
        assertEquals("/renovation/" + createdRenovation.getId(), result.getResponse().getRedirectedUrl());
    }

    @Test
    void createRenovation_InvalidAddress_StreetContainsInvalidCharacters() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123! Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("streetError", "Street address contains invalid characters."));
    }

    @Test
    void createRenovation_InvalidAddress_StreetEmpty() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("streetError", "Street address cannot be empty."));
    }

    @Test
    void createRenovation_InvalidAddress_SuburbContainsInvalidCharacters() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere!")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("suburbError", "Suburb contains invalid characters."));
    }

    @Test
    void createRenovation_InvalidAddress_CityContainsInvalidCharacters() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "Christchurch!")
                        .param("suburb", "Cashmere")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("cityError", "City contains invalid characters."));
    }

    @Test
    void createRenovation_InvalidAddress_CityEmpty() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022")
                        .param("city", "")
                        .param("suburb", "Cashmere")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("cityError", "City cannot be empty."));
    }

    @Test
    void createRenovation_InvalidAddress_PostcodeContainsInvalidCharacters() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "8022!")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("postcodeError", "Postcode contains invalid characters."));
    }

    @Test
    void createRenovation_InvalidAddress_PostcodeEmpty() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand")
                        .param("postcode", "")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("postcodeError", "Postcode cannot be empty."));
    }

    @Test
    void createRenovation_InvalidAddress_CountryContainsInvalidCharacters() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "New Zealand!")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("countryError", "Country contains invalid characters."));
    }

    @Test
    void createRenovation_InvalidAddress_CountryEmpty() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "")
                        .param("postcode", "8022")
                        .param("city", "Christchurch")
                        .param("suburb", "Cashmere")
                        .param("region", "Somewhere")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("countryError", "Country cannot be empty."));
    }

    @Test
    void createRenovation_InvalidAddress_FieldCheck() throws Exception {
        List<User> users = userRepository.findByEmail("john@dude.com");
        User user = users.get(0);
        Assertions.assertTrue(renovationRepository.findByNameAndUser("Test Renovation", user).isEmpty());

        mockMvc.perform(post("/myRenovations/newRenovation")
                        .param("name", "Test Renovation")
                        .param("description", "Test Description")
                        .param("roomName[]", "Test Room")
                        .param("streetAddress", "123 Fake Street")
                        .param("country", "")
                        .param("postcode", "")
                        .param("city", "")
                        .param("suburb", "")
                        .param("region", "")
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(view().name("pages/renovation/createEditRenovationPage"))
                .andExpect(model().attribute("fieldError", "When providing an address, all fields except Suburb and Region are required."));
    }

}
