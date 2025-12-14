package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;

import java.util.List;

/**
 * Entity class representing a room object.
 */
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "renovation_id", nullable = false)
    private Renovation renovation;

    @ManyToMany
    @JoinTable(
            name = "task_room",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id"))
    private List<Task> tasks;

    /**
     * JPA required no-args constructor
     */
    protected Room() {
    }

    /**
     * Creates a new Room object.
     *
     * @param renovation The renovation this room belongs to.
     * @param name       The name of the room.
     */
    public Room(Renovation renovation, String name) {
        this.renovation = renovation;
        this.name = name;
    }

    /**
     * Gets the ID of the room.
     *
     * @return The room ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of a room
     *
     * @param l the id to set for the room
     */
    public void setId(long l) {
        this.id = l;
    }

    /**
     * Gets the name of the room.
     *
     * @return The room name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the renovation that this room is for.
     *
     * @return The renovation.
     */
    public Renovation getRenovation() {
        return renovation;
    }

    /**
     * Sets the renovation that this room is for.
     *
     * @param renovation The renovation.
     */
    public void setRenovation(Renovation renovation) {
        this.renovation = renovation;
        if (renovation != null && !renovation.getRooms().contains(this)) {
            renovation.addRoom(this);
        }
    }

    /**
     * Gets the tasks that this room is associated with
     *
     * @return the list of tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", renovation=" + renovation.getId() +
                '}';
    }
}
