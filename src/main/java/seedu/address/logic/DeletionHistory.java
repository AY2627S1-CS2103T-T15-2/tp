package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/** Session-only storage for the latest deletion that can be undone. */
public final class DeletionHistory {
    private DeletionSnapshot latest;

    /** Replaces the undo opportunity after a deletion has been saved successfully. */
    public void record(DeletionSnapshot snapshot) {
        latest = requireNonNull(snapshot);
    }

    /** Returns the snapshot without consuming it, allowing retry after a failed save. */
    public Optional<DeletionSnapshot> getLatest() {
        return Optional.ofNullable(latest);
    }

    /** Clears history after successful undo or another successful data-changing command. */
    public void clear() {
        latest = null;
    }
}
