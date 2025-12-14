package nz.ac.canterbury.seng302.homehelper.unit.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Tag;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.TagException;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.ProfanityService;
import nz.ac.canterbury.seng302.homehelper.service.renovation.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TagServiceTests {
    private final TagRepository tagRepository = Mockito.mock(TagRepository.class);
    private final RenovationRepository renovationRepository = Mockito.mock(RenovationRepository.class);
    private final ProfanityService profanityService = Mockito.mock(ProfanityService.class);

    private TagService tagService;

    @BeforeEach
    void setup() {
        tagService = new TagService(tagRepository, renovationRepository, profanityService);
    }

    @Test
    void testSaveTag_EmptyTag_ThrowsTagException() {
        Renovation renovation = Mockito.mock(Renovation.class);
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("", renovation)));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_TagToBig_ThrowsTagException() {
        Renovation renovation = Mockito.mock(Renovation.class);
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("123456789123456789123456789123456", renovation)));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_OnlySpecialCharsTag_ThrowsTagException() {
        Renovation renovation = Mockito.mock(Renovation.class);
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("/?>", renovation)));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_OnlyNumsTag_ThrowsTagException() {
        Renovation renovation = Mockito.mock(Renovation.class);
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("123456", renovation)));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_NumsAndSpecialCharTag_ThrowsTagException() {
        Renovation renovation = Mockito.mock(Renovation.class);
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("1234?!>", renovation)));
        assertEquals("Tag must contain at least one letter and have a maximum length of 32 characters", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_DuplicateTag_ThrowsTagException() throws TagException {
        Renovation renovation = Mockito.mock(Renovation.class);
        when(renovation.getTags()).thenReturn(List.of(new Tag("Tag1", renovation)));
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("Tag1", renovation)));
        assertEquals("Can't create a duplicate tag.", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_FiveTagsExist_ThrowsRenovationException() {
        Renovation renovation = Mockito.mock(Renovation.class);
        when(renovation.getTags()).thenReturn(List.of(new Tag("Tag1", renovation), new Tag("Tag2", renovation),
                new Tag("Tag3", renovation), new Tag("Tag4", renovation), new Tag("Tag5", renovation)));
        TagException tagException = assertThrows(TagException.class, () -> tagService.save(new Tag("Tag6", renovation)));
        assertEquals("Renovations can't have more than 5 tags.", tagException.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void testSaveTag_ValidTag_TagIsSaved() {
        Renovation renovation = Mockito.mock(Renovation.class);
        when(profanityService.containsProfanity(any())).thenReturn(false);
        assertDoesNotThrow(() -> tagService.save(new Tag("Tag1", renovation)));
        verify(renovationRepository, times(1)).save(any());
    }

    @Test
    void testSaveTag_ValidTag32Chars_TagIsSaved() {
        Renovation renovation = Mockito.mock(Renovation.class);
        when(profanityService.containsProfanity(any())).thenReturn(false);
        assertDoesNotThrow(() -> tagService.save(new Tag("qwertyuiopasdfghjklzxcvbnmqwerty", renovation)));
        verify(renovationRepository, times(1)).save(any());
    }

    @Test
    void testSaveTag_ValidTag1Chars_TagIsSaved() {
        Renovation renovation = Mockito.mock(Renovation.class);
        when(profanityService.containsProfanity(any())).thenReturn(false);
        assertDoesNotThrow(() -> tagService.save(new Tag("A", renovation)));
        verify(renovationRepository, times(1)).save(any());
    }
}