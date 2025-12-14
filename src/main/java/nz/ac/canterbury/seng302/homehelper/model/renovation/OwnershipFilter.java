package nz.ac.canterbury.seng302.homehelper.model.renovation;

public enum OwnershipFilter {
    ALL(0),
    OWNED_BY_ME(1),
    SHARED_WITH_ME(2);

    private final int code;

    OwnershipFilter(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
