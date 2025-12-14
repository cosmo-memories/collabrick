package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.key.TagKey;

import java.util.Objects;

/**
 * Tag class; tags are a text string associated with a renovation
 * Primary Key for the Tag table is composite consisting of tag text + renovation ID
 */
@Entity
@IdClass(TagKey.class)
public class Tag {

    @Id
    private String tag;

    @Id
    @ManyToOne
    @JoinColumn(name = "Renovation", nullable = false)
    private Renovation renovation;

    /**
     * Empty JPA constructor
     */
    public Tag() {
    }

    /**
     * Constructor with parameters
     *
     * @param tag        Tag string
     * @param renovation Renovation to attach to
     */
    public Tag(String tag, Renovation renovation) {
        this.tag = tag;
        this.renovation = renovation;
    }

    // Getters and Setters:

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
        if (renovation != null && !renovation.getTags().contains(this)) {
            renovation.addTag(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag1 = (Tag) o;
        return Objects.equals(tag, tag1.tag) && Objects.equals(renovation, tag1.renovation);
    }
}
