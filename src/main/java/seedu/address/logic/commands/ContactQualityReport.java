package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/** Produces suggestions for an ordered snapshot of the displayed contacts, using their displayed indexes. */
final class ContactQualityReport {
    private final List<Person> contacts;

    ContactQualityReport(List<Person> contacts) {
        this.contacts = List.copyOf(requireNonNull(contacts));
    }

    String format(CheckCommand.Section section) {
        requireNonNull(section);
        if (contacts.isEmpty()) {
            return "No displayed contacts to check. Use list or find to display contacts first.";
        }
        List<String> sections = new ArrayList<>();
        if (section == CheckCommand.Section.ALL || section == CheckCommand.Section.GUARDIANS) {
            sections.add(formatSection("Guardian links", guardianSuggestions()));
        }
        if (section == CheckCommand.Section.ALL || section == CheckCommand.Section.DETAILS) {
            sections.add(formatSection("Contact and student details", detailSuggestions()));
        }
        if (section == CheckCommand.Section.ALL || section == CheckCommand.Section.DUPLICATES) {
            sections.add(formatSection("Possible duplicates", duplicateSuggestions()));
        }
        return "Data-quality report: " + contacts.size() + " displayed contact(s).\n"
                + "Suggestions only: missing optional data and shared details may be intentional.\n"
                + String.join("\n\n", sections) + "\nNo contacts were changed.";
    }

    private List<String> guardianSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (int index = 0; index < contacts.size(); index++) {
            Person person = contacts.get(index);
            if (person.getRole() == Role.STUDENT && person.getGuardianId().isEmpty()) {
                suggestions.add(describe(index) + ": no guardian linked.");
            }
        }
        return suggestions;
    }

    private List<String> detailSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (int index = 0; index < contacts.size(); index++) {
            Person person = contacts.get(index);
            List<String> missing = new ArrayList<>();
            if (person.getPhone().isEmpty() && person.getEmail().isEmpty()) {
                missing.add("phone or email");
            }
            if (person.getRole() == Role.STUDENT) {
                if (person.getLevel().isEmpty()) {
                    missing.add("level");
                }
                if (person.getSubject().isEmpty()) {
                    missing.add("subject");
                }
            }
            if (!missing.isEmpty()) {
                suggestions.add(describe(index) + ": consider adding " + String.join(", ", missing) + ".");
            }
        }
        return suggestions;
    }

    private List<String> duplicateSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (int first = 0; first < contacts.size(); first++) {
            for (int second = first + 1; second < contacts.size(); second++) {
                Person candidate = contacts.get(first);
                Person other = contacts.get(second);
                String reason = duplicateReason(DuplicateWarning.classify(candidate, List.of(other)));
                if (reason != null) {
                    suggestions.add(describe(first) + " and " + describe(second) + ": "
                            + reason + ".");
                }
            }
        }
        return suggestions;
    }

    private String duplicateReason(DuplicateWarning warning) {
        return switch (warning) {
            case PHONE -> "same role and name; phone matches ignoring spaces and hyphens";
            case EMAIL -> "same role and name; email matches ignoring letter case";
            case GENERAL -> "same role and name; both lack a phone number or both lack an email address";
            case NONE -> null;
        };
    }

    private String describe(int index) {
        Person person = contacts.get(index);
        String former = person.isFormerStudent() ? " [Former]" : "";
        return (index + 1) + ": " + person.getName() + " [" + person.getRole() + "]" + former;
    }

    private String formatSection(String heading, List<String> suggestions) {
        return heading + " (" + suggestions.size() + "):\n"
                + (suggestions.isEmpty() ? "No suggestions." : String.join("\n", suggestions));
    }
}
