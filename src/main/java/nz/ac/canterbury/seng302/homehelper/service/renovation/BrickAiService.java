package nz.ac.canterbury.seng302.homehelper.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.chat.ChatChannel;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.chat.ChatChannelDetailsException;
import nz.ac.canterbury.seng302.homehelper.repository.chat.ChatChannelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Contains service methods for the BrickAI user account and chat channels.
 * Each user has a private channel on each of their renovations containing only them and the BrickAI account.
 */
@Service
public class BrickAiService {

    public static final String BRICK_AI_USER_EMAIL = "brickai@brickaimail.com";
    private final UserRepository userRepository;
    private final ChatChannelRepository chatChannelRepository;

    @Autowired
    public BrickAiService(UserRepository userRepository, ChatChannelRepository chatChannelRepository) {
        this.userRepository = userRepository;
        this.chatChannelRepository = chatChannelRepository;
    }

    /**
     * Creates the AI user account. For use with DataInitializer and test scenarios only.
     */
    public void createAiUser() {
        User brickAi = new User("BrickAI", "", BRICK_AI_USER_EMAIL, "", "");
        brickAi.setImage("images/BrickAI.png");
        userRepository.save(brickAi);
    }

    /**
     * Gets the AI user account.
     * @return      AI User object
     */
    public User getAiUser() {
        return userRepository.findByEmail(BRICK_AI_USER_EMAIL)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ai user not found"));
    }

    /**
     * Checks if the given User is the AI User.
     * @param user      User object
     * @return          Boolean result
     */
    public boolean isAiUser(User user) {
        return user.getEmail().equals(BRICK_AI_USER_EMAIL);
    }

    /**
     * Create a new private chat channel on the given renovation between the given user and BrickAI.
     * Does not perform channel name validation, so we can have multiple AI channels with the same name.
     * @param renovation        Renovation object
     * @param user              User object
     * @return                  New ChatChannel
     */
    public ChatChannel createAiChannel(Renovation renovation, User user) throws ChatChannelDetailsException {
        if (renovation.getMembers().stream().noneMatch(member -> member.getUser().equals(user))) {
            throw new ChatChannelDetailsException("User with ID " + user.getId() + " is not a member of renovation ID " + renovation.getId());
        }
        if (getAiChannel(renovation, user).isPresent()) {
            throw new ChatChannelDetailsException("User with ID " + user.getId() + " already has an AI chat channel for renovation ID " + renovation.getId());
        }
        ChatChannel chatChannel = new ChatChannel("brickAI", renovation);
        try {
            User brickAi = getAiUser();
            chatChannel.getMembers().add(brickAi);
            chatChannel.getMembers().add(user);
        } catch (IllegalStateException ignored) {}
        return chatChannelRepository.save(chatChannel);
    }

    /**
     * Finds the AI chat channel for the given user on the given renovation, if it exists.
     *
     * @param renovation    Renovation object
     * @param user          User object
     * @return              Optional<ChatChannel>
     */
    public Optional<ChatChannel> getAiChannel(Renovation renovation, User user) {
        return chatChannelRepository.findAiChannel(renovation, user);
    }
}

