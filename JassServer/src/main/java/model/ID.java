package model;


/**
 * @author Amaury Combes
 */
abstract class ID {

    private long id;

    public ID() {
        // Default constructor required for calls to DataSnapshot.getValue
    }

    public ID(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null || other.getClass() != this.getClass()) {
            return false;
        }

        ID otherID = (ID) other;
        return this.id == otherID.id;
    }

    @Override
    public String toString() {
        return Long.toString(id);
    }

}
