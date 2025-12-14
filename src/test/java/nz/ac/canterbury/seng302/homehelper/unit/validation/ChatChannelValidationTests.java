package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.validation.chat.ChatChannelValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.when;

class ChatChannelValidationTests {

    private Renovation renovation;
    private ChatChannelRepository chatChannelRepository;

    @BeforeEach
    void setUp() {
        renovation = new Renovation("Kitchen Reno", "Kitchen");
        chatChannelRepository = Mockito.mock(ChatChannelRepository.class);
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.ChatChannelTestData#validChannelNames")
    void validateChannelName_givenValidChannelName_returnEmptyString(String channelName) {
        Assertions.assertEquals("", ChatChannelValidation.validateChannelName(channelName, chatChannelRepository, renovation));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.ChatChannelTestData#invalidChannelNames")
    void validateChannelName_givenInvalidChannelName_returnErrorMessage(String channelName) {
        Assertions.assertEquals(ChatChannelValidation.INVALID_CHANNEL_NAME_MESSAGE, ChatChannelValidation.validateChannelName(channelName, chatChannelRepository, renovation));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.ChatChannelTestData#emptyChannelNames")
    void validateChannelName_givenEmptyChannelName_returnErrorMessage(String channelName) {
        Assertions.assertEquals(ChatChannelValidation.CHANNEL_NAME_EMPTY_MESSAGE, ChatChannelValidation.validateChannelName(channelName, chatChannelRepository, renovation));
    }

    @ParameterizedTest
    @MethodSource("nz.ac.canterbury.seng302.homehelper.data.ChatChannelTestData#tooLongChannelNames")
    void validateChannelName_givenTooLongChannelName_returnErrorMessage(String channelName) {
        Assertions.assertEquals(ChatChannelValidation.CHANNEL_NAME_TOO_LONG_MESSAGE, ChatChannelValidation.validateChannelName(channelName, chatChannelRepository, renovation));
    }

    @Test
    void validateChannelName_nameAlreadyInUse_returnErrorMessage() {
        when(chatChannelRepository.findByNameAndRenovation(
                Mockito.eq("renovation"),
                Mockito.eq(renovation)
        )).thenReturn(Optional.of(Mockito.mock(nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel.class)));

        String result = ChatChannelValidation.validateChannelName("renovation", chatChannelRepository, renovation);

        Assertions.assertEquals(ChatChannelValidation.CHANNEL_NAME_ALREADY_EXISTS_MESSAGE, result);
    }

}
