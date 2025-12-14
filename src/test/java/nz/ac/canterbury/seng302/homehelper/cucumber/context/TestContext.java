// Got really stuck trying to figure out how to pass a context between Cucumber tests that are using the same step definitions in differe files
// ChatGPT Generated this to be able to pass users/renovations created in different files to other files for cucumber tests

package nz.ac.canterbury.seng302.homehelper.cucumber.context;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

@Component
public class TestContext {
    private long userId;
    private long renovationId;
    private MockHttpSession session;

    private MvcResult result;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public RenovationRepository renovationRepository;

    @Autowired
    public TagRepository tagRepository;

    /**
     * Gets the user set for a test context
     *
     * @return a user
     */
    public User getUser() {
        return userRepository.findUserById(userId).stream().findFirst().orElse(null);
    }

    /**
     * Sets a user for a test context
     *
     * @param user the user for the test context
     */
    public void setUser(User user) {
        this.userId = user.getId();
    }

    /**
     * Gets the renovation set for a test context
     *
     * @return a renovation
     */
    public Renovation getRenovation() {
        return renovationRepository.findById(renovationId).orElse(null);
    }

    /**
     * Sets a renovation for a test context
     *
     * @param renovation the renovation for the test context
     */
    public void setRenovation(Renovation renovation) {
        this.renovationId = renovation.getId();
    }

    /**
     * Set a MvcResult for a test context
     *
     * @param result The MvcResult for the test context
     */
    public void setResult(MvcResult result) {
        this.result = result;
    }

    /**
     * Gets the MvcResult for a test context
     *
     * @return MvcResult
     */
    public MvcResult getResult() {
        return result;
    }

    /**
     * Set Http Session
     *
     * @param session MockHttpSession
     */
    public void setSession(MockHttpSession session) {
        this.session = session;
    }

    /**
     * Get Http Session
     *
     * @return MockHttpSession
     */
    public MockHttpSession getSession() {
        return session;
    }
}
