package nz.ac.canterbury.seng302.homehelper.repository.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Room;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Room repository accessor using Spring's @link{CrudRepository}.
 */
public interface RoomRepository extends CrudRepository<Room, Long> {

    /**
     * Finds a room by its exact name (case-insensitive) and its renovation id.
     *
     * @param name         The name of the room.
     * @param renovationId The id of the renovation.
     * @return An optional containing the room if found, otherwise empty.
     */
    @Query("Select r FROM Room r WHERE LOWER(r.name) = Lower(:name) AND r.renovation.id = :renovationId")
    Optional<Room> findByNameAndRenovation(String name, Long renovationId);

    /**
     * Finds a room by its id
     *
     * @param id The id of the room
     * @return An option containing the room if found, otherwise empty.
     */
    @Query("Select r FROM Room r WHERE r.id = :id")
    Optional<Room> findByRoomId(Long id);
}
