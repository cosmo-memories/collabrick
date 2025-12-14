package nz.ac.canterbury.seng302.homehelper.integration.controller.auth;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.auth.VerificationTokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import nz.ac.canterbury.seng302.homehelper.validation.user.UserValidation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserLocationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private UserRepository userRepository;

    @SpyBean
    private VerificationTokenRepository verificationTokenRepository;

    @MockBean
    private EmailService emailService;

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 456 Bogus Road, Old Zealand, 1234, Auckland, Avondale",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, Christchurch, ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', '', '', ''"
    })
    void submitRegistration_ValidLocation_UserCreatedSuccessfully(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verification"));

        verify(userRepository).save(any(User.class));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123! Fake Street, New Zealand, 8022, Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123@ Fake Street, New Zealand, 8022, Christchurch, ''"
    })
    void submitRegistration_StreetInvalidCharacters_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("streetError", is(LocationValidation.STREET_INVALID_CHARACTERS_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', New Zealand, 8022, Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', New Zealand, 8022, Christchurch, ''"
    })
    void submitRegistration_StreetEmpty_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("streetError", is(LocationValidation.STREET_EMPTY_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, Christchurch, C&shmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, Christchurch, Cashmere_"
    })
    void submitRegistration_SuburbInvalidCharacters_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("suburbError", is(LocationValidation.SUBURB_INVALID_CHARACTERS_MESSAGE))));
    }


    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, Christ_church, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, Christ%church, ''"
    })
    void submitRegistration_CityInvalidCharacters_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("cityError", is(LocationValidation.CITY_INVALID_CHARACTERS_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, '', Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022, '', ''"
    })
    void submitRegistration_CityEmpty_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("cityError", is(LocationValidation.CITY_EMPTY_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022?, Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, 8022!, Christchurch, ''"
    })
    void submitRegistration_PostcodeInvalidCharacters_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("postcodeError", is(LocationValidation.POSTCODE_INVALID_CHARACTERS_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, '', Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, '', Christchurch, ''"
    })
    void submitRegistration_PostcodeEmpty_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("postcodeError", is(LocationValidation.POSTCODE_EMPTY_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New*Zealand, 8022, Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New^Zealand, 8022, Christchurch, ''"
    })
    void submitRegistration_CountryInvalidCharacters_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("countryError", is(LocationValidation.COUNTRY_INVALID_CHARACTERS_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, '', 8022, Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, '', 8022, Christchurch, ''"
    })
    void submitRegistration_CountryEmpty_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("countryError", is(LocationValidation.COUNTRY_EMPTY_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123! Fake Street, New_Zealand, 8$22, Chr|stchurch, C@shmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123? Fake Street, New/Zealand, 8\\22, Chri$tchurch, ''"
    })
    void submitRegistration_ManyInvalidCharacters_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("streetError", is(LocationValidation.STREET_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("cityError", is(LocationValidation.CITY_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("postcodeError", is(LocationValidation.POSTCODE_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("countryError", is(LocationValidation.COUNTRY_INVALID_CHARACTERS_MESSAGE))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, '', '', '', ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, New Zealand, '', '', ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, '', 8022, '', ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, '', '', Christchurch, ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, 123 Fake Street, '', '', '', Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', New Zealand, '', '', ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', New Zealand, 8022, '', ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', New Zealand, '', Christchurch, ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', New Zealand, '', '', Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', 8022, '', ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', 8022, Christchurch, ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', 8022, '', Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', '', Christchurch, ''",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', '', Christchurch, Cashmere",
            "John, Test, john@testing.com, Abc123!!, Abc123!!, '', '', '', '', Cashmere"
    })
    void submitRegistration_ManyEmptyFields_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("fieldError", is("When providing an address, all fields except Suburb and Region are required."))));
    }

    @ParameterizedTest
    @CsvSource({
            "John, Test, john@testing.com, Abc123!!, Abc123, 123! Fake Street, New_Zealand, 8$22, Chr|stchurch, C@shmere",
            "John, Test, john.com, Abc123!!, Abc123, 123? Fake Street, New/Zealand, 8\\22, Chri$tchurch, ''"
    })
    void submitRegistration_UserAndLocationInvalid_UserNotCreated(
            String firstName, String lastName, String email, String password, String confirmPassword,
            String streetAddress, String country, String postcode, String city, String suburb
    ) throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fname", firstName)
                        .param("lname", lastName)
                        .param("email", email)
                        .param("password", password)
                        .param("retypePassword", confirmPassword)
                        .param("streetAddress", streetAddress)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("suburb", suburb))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors",
                        hasProperty("streetError", is(LocationValidation.STREET_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("cityError", is(LocationValidation.CITY_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("postcodeError", is(LocationValidation.POSTCODE_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("countryError", is(LocationValidation.COUNTRY_INVALID_CHARACTERS_MESSAGE))))
                .andExpect(model().attribute("errors",
                        hasProperty("passwordError", is(UserValidation.PASSWORD_RETYPE_NO_MATCH))));
    }

}
