package nz.ac.canterbury.seng302.homehelper.service.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.RenovationMember;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.exceptions.renovation.InvalidPermissionsException;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.InvitationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationMemberRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static nz.ac.canterbury.seng302.homehelper.model.renovation.RenovationMemberRole.OWNER;

@Service
public class RenovationMemberService {
    private final RenovationMemberRepository renovationMemberRepository;
    private final InvitationService invitationService;

    @Autowired
    public RenovationMemberService(RenovationMemberRepository renovationMemberRepository, RenovationService renovationService, InvitationService invitationService) {
        this.renovationMemberRepository = renovationMemberRepository;
        this.invitationService = invitationService;
    }

    @Transactional
    public void saveRenovationMember(RenovationMember renovationMember) {
        renovationMemberRepository.save(renovationMember);
    }

    /**
     * Checks whether the given user is a member of the specified renovation.
     *
     * @param user       the user to check
     * @param renovation the renovation to check against
     * @return true if the user is a member
     * @throws NoResourceFoundException if the user is not a member
     */
    public boolean checkMembership(User user, Renovation renovation) throws NoResourceFoundException {
        if (renovationMemberRepository.checkMembership(user, renovation).isEmpty()) {
            throw new NoResourceFoundException(HttpMethod.GET, "Renovation Member not found");
        }
        return true;
    }

    /**
     * Deletes a renovation member from a renovation.
     * This method ensures the renovation owner cannot be removed.
     *
     * @param renovationMember The renovation member to be removed.
     * @throws InvalidPermissionsException If the renovation member has the OWNER role.
     */
    @Transactional
    public void deleteRenovationMember(RenovationMember renovationMember) throws InvalidPermissionsException {
        if(renovationMember.getRole() == OWNER){
            throw new InvalidPermissionsException("Cannot remove owner");
        }
        Renovation renovation = renovationMember.getRenovation();
        renovation.removeMember(renovationMember.getUser());
        invitationService.deleteInvitation(renovation, renovationMember.getUser());
        renovationMemberRepository.delete(renovationMember);
    }

    /**
     * Gets the RenovationMember Entity
     *
     * @param user user to be found
     * @param renovation renovation the user should be part of
     * @return RenovationMember Entity else an empty object
     */
    public RenovationMember getRenovationMember(User user, Renovation renovation) {
        return renovationMemberRepository.findByRenovationAndUser(renovation, user);
    }


}
