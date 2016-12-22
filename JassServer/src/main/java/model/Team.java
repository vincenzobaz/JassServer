package model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a team. It contains the ID's of all team members.
 */
public class Team {

    // List containing the ID of the team members
    private List<Player.PlayerID> members;

    public Team() {
    }

    /**
     * Public constructor for Team
     *
     * @param memberIDs A set containing the ID's of the player
     * @throws IllegalArgumentException If the set of ID's is empty
     */
    public Team(List<Player.PlayerID> memberIDs) throws IllegalArgumentException {
        if (memberIDs.size() == 0) {
            throw new IllegalArgumentException("The team cannot have zero member");
        }
        ArrayList<Player.PlayerID> tmpMembers = new ArrayList<>();
        for (Player.PlayerID m : memberIDs) {
            if (tmpMembers.contains(m)) {
                throw new IllegalArgumentException("The team cannot have multiple time the same member");
            } else {
                tmpMembers.add(m);
            }
        }
        this.members = Collections.unmodifiableList(tmpMembers);
    }

    /**
     * Getter for the number of players in the team
     *
     * @return Returns the number of players in the team
     */
    public int getNumberOfMembers() {
        return members.size();
    }

    /**
     * Getter for the players
     *
     * @return Returns a list of the members of the team
     */
    public List<Player.PlayerID> getMembers() {
        return Collections.unmodifiableList(members);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() != this.getClass()) {
            return false;
        }
        return ((Team) o).members.containsAll(this.members) && this.members.containsAll(((Team) o).members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(members);
    }
}
