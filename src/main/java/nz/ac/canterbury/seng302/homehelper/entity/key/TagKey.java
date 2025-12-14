package nz.ac.canterbury.seng302.homehelper.entity.key;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key class for Tag table
 * For reference: https://www.baeldung.com/jpa-composite-primary-keys
 */
public class TagKey implements Serializable {

    private String tag;
    private Renovation renovation;

    /**
     * Empty JPA constructor
     */
    public TagKey() {
    }

    /**
     * Constructor with parameters
     *
     * @param tag        Tag string
     * @param renovation Renovation to attach to
     */
    public TagKey(String tag, Renovation renovation) {
        this.tag = tag;
        this.renovation = renovation;
    }

    //Getters and Setters:

    /**
     * Get tag text
     *
     * @return Tag text string
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set tag text
     *
     * @param tag Tag text string
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Get renovation associated with tag
     *
     * @return Renovation object
     */
    public Renovation getRenovation() {
        return renovation;
    }

    /**
     * Set renovation associated with tag
     *
     * @param renovation Renovation object
     */
    public void setRenovation(Renovation renovation) {
        this.renovation = renovation;
    }

    // Below: ChatGPT
    // Apparently these are necessary

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TagKey tagKey = (TagKey) obj;
        return tag.equals(tagKey.tag) && renovation.equals(tagKey.renovation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, renovation);
    }

}
