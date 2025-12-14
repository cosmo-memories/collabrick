package nz.ac.canterbury.seng302.homehelper.entity.user;

import jakarta.persistence.*;

/**
 * Entity class to describe the authorities a user can have
 * Copied straight from Spring Security Handout
 */
@Entity
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authority_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column()
    private String role;

    protected Authority() {
        // JPA empty constructor
    }

    public Authority(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
