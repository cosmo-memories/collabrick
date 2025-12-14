package nz.ac.canterbury.seng302.homehelper.entity.user;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity class for a user to be put into the database
 * Note the @link{Entity} annotation required for declaring this as a persistence entity
 */
@Entity(name = "RENOVATION_USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fname;

    @Column(nullable = false)
    private String lname;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveUpdate> liveUpdates = new ArrayList<>();

    @Column //(nullable = false)
    private String image = "images/PlaceholderIcon.png";
    @Embedded
    private Location location = new Location();
    @Column
    private LocalDateTime createdTimestamp;

    @Column
    private boolean activated;

    @Transient
    private String retypePassword;

    @Column()
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Authority> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<Renovation> renovations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<RenovationMember> memberships = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentlyAccessedRenovation> recentlyAccessedRenovations = new ArrayList<>();

    @Column
    private boolean allowBrickAIChatAccess = true;

    /**
     * Empty constructor for JPA
     */
    public User() {
    }

    /**
     * Constructor for the user entity
     *
     * @param fname          first name of user
     * @param lname          last name of user
     * @param email          emal of user
     * @param password       password of user (hashed if it comes from the DB)
     * @param retypePassword retyped password from user
     */
    public User(String fname, String lname, String email, String password, String retypePassword) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.retypePassword = retypePassword;
        this.image = "images/PlaceholderIcon.png";
        userRoles = new ArrayList<>();
        this.createdTimestamp = LocalDateTime.now();
    }

    public User(String fname, String lname, String email) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
    }

    public User(long id, String fname, String lname) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
    }

    /**
     * Grants a user a given authority
     * Copied from Spring Security Handout
     *
     * @param authority the authority we want to give to a user
     */
    public void grantAuthority(String authority) {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        userRoles.add(new Authority(authority));
    }

    /**
     * Gets a user's given authorities
     * Copied from Spring Security Handout
     *
     * @return list of authorities to give
     */
    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (this.userRoles != null) {
            this.userRoles.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority.getRole())));
        }
        return authorities;
    }

    /**
     * Gets the id of a user
     *
     * @return the id of the user
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of a user
     *
     * @param id the id to set for a user
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the first and last name of the user
     *
     * @return the users first and last name
     */
    public String getFullName() {
        if (lname != null & !Objects.equals(lname, "")) {
            return fname + " " + lname;
        }
        return fname;
    }

    /**
     * Gets the first name of the user
     *
     * @return the users first name
     */
    public String getFname() {
        return fname;
    }

    /**
     * Sets the users first name
     *
     * @param fname the first name to set for the user
     */
    public void setFname(String fname) {
        this.fname = fname;
    }

    /**
     * Gets the last name of a user
     *
     * @return the last name of the user
     */
    public String getLname() {
        return lname;
    }

    /**
     * Sets the last name for the user
     *
     * @param lname the last name to set for the user
     */
    public void setLname(String lname) {
        this.lname = lname;
    }

    /**
     * Gets the email of the user
     *
     * @return the users email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email for the user
     *
     * @param email the email to set the user to
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the password of the user
     *
     * @return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the user
     *
     * @param password the password to set the user to
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the time the user was created
     *
     * @return the time stamp of when the user was created
     */
    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Sets the created time stamp for the user
     *
     * @param createdTimestamp the created time stamp to set to
     */
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /**
     * Gets the retype of the password of the user
     *
     * @return the retype of the password of the user
     */
    public String getRetypePassword() {
        return retypePassword;
    }

    /**
     * Sets the retyped password for the user
     *
     * @param retypePassword the retyped password of the user to set
     */
    public void setRetypePassword(String retypePassword) {
        this.retypePassword = retypePassword;
    }

    /**
     * Gets the profile image filename of the user
     *
     * @return the filename of the users profile image
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the users profile image
     *
     * @param image the image filename to set for the user
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the renovations included with this user
     *
     * @return A list of renovations
     */
    public List<Renovation> getRenovations() {
        return renovations;
    }

    /**
     * Adds a new renovation to this user
     *
     * @param renovation the renovation to add
     */
    public void addRenovation(Renovation renovation) {
        if (!renovations.contains(renovation)) {
            renovations.add(renovation);
            renovation.setOwner(this);
        }
    }

    /**
     * Gets the users activation status
     *
     * @return boolean of if the user is active
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Sets the user to activated
     *
     * @param activated boolean to set the users activation status to
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public Set<RenovationMember> getMemberships() {
        return memberships;
    }

    public boolean isAllowBrickAIChatAccess() {
        return allowBrickAIChatAccess;
    }

    public void setAllowBrickAIChatAccess(boolean allowBrickAIChatAccess) {
        this.allowBrickAIChatAccess = allowBrickAIChatAccess;
    }
}

