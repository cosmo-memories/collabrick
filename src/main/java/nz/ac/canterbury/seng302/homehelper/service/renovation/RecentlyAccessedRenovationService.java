package nz.ac.canterbury.seng302.homehelper.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.RecentlyAccessedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RecentlyAccessedRenovationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for recently accessed renovations
 */
@Service
public class RecentlyAccessedRenovationService {

    private final RecentlyAccessedRenovationRepository recentlyAccessedRenovationRepository;

    /**
     * Constructor
     * @param recentlyAccessedRenovationRepository the recently accessed renovation repository
     */
    @Autowired
    public RecentlyAccessedRenovationService(RecentlyAccessedRenovationRepository recentlyAccessedRenovationRepository) {
        this.recentlyAccessedRenovationRepository = recentlyAccessedRenovationRepository;
    }

    /**
     * Create or update a recently accessed renovation object based on if it already exists or not from a user and a renovation
     * @param renovation the renovation that is being accessed
     * @param user the user that is accessing the renovation
     */
    public void createOrUpdateRecentlyAccessedRenovation(Renovation renovation, User user) {
        RecentlyAccessedRenovation recentlyAccessedRenovation = recentlyAccessedRenovationRepository.findByRenovationAndUser(renovation, user).orElse(null);
        if (recentlyAccessedRenovation != null) {
            recentlyAccessedRenovation.updateTimeAccessed();
        } else {
            recentlyAccessedRenovation = new RecentlyAccessedRenovation(user, renovation);
        }
        recentlyAccessedRenovationRepository.save(recentlyAccessedRenovation);
    }

    /**
     * Calls the method to delete the non-members from recently accessed renovation
     * @param renovationId the renovation to delete the non-members from
     */
    public void deleteNonMemberAccessesFromPrivateRenovation(long renovationId, boolean isPublic) {
        if (!isPublic) {
            recentlyAccessedRenovationRepository.deleteNonMembersFromPrivateRenovation(renovationId);
        }
    }

    /**
     * Calls the method to delete all recently accessed entries from a renovation
     * @param renovationId the id of the renovation to delete from
     */
    public void deleteAllRenovationAccessEntriesForRenovation(long renovationId) {
        recentlyAccessedRenovationRepository.deleteAllRenovationAccessEntriesForRenovation(renovationId);
    }

    /**
     * Calls the method to delete a users recently accessed entry from a private renovation
     * @param renovationId the id of the renovation to delete from
     * @param userId       the id of the user
     */
    public void deleteUserRenovationAccessEntryForPrivateRenovation(long renovationId, long userId) {
        recentlyAccessedRenovationRepository.deleteUserRenovationAccessEntryForPrivateRenovation(renovationId, userId);
    }

    /**
     * Gets last three accessed renovation of a user
     *
     * @param user to get accessed renovation of
     * @return List of RecentlyAccessedRenovation objects
     */
    public List<RecentlyAccessedRenovation> getRecentlyAccessedRenovationsForUser(User user){
        return recentlyAccessedRenovationRepository.findTop3ByUserOrderByTimeAccessedDesc(user);
    }
}
