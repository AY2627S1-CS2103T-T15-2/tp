---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TutorRoster User Guide

TutorRoster is a **desktop application for managing student and guardian contacts through typed commands**, with a graphical contact list and a panel for the selected contact's details.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T15-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your TutorRoster.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   The application opens with your saved contacts, or an empty list if there is no data file yet.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add r/student n/John Doe p/98765432 e/johnd@example.com` : Adds a student named `John Doe`.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `edit INDEX [n/NAME] [p/PHONE]` can be used as `edit 1 n/John Doe` or as `edit 1 p/91234567`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

<box type="info" seamless>

**Contact order:**<br>

* Contacts are always displayed in alphabetical order of name, ignoring capitalisation.
* Contacts with the same name are displayed with guardians before students.
* Index numbers used by commands such as `edit` and `delete` refer to this displayed order.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`

### Adding a person: `add`

Adds a person to the address book.

Format: `add r/ROLE n/NAME p/PHONE_NUMBER [e/EMAIL]`

`ROLE` is `student` or `guardian`, ignoring letter case.

Email is optional. Omit `e/` to save a contact without an email; the contact displays `Not provided`.
Supplying `e/` with an empty or whitespace-only value is rejected with:
`Empty email parameter: Email cannot be empty when e/ is specified.`

An email must have exactly one `@`, a non-empty local part, and at least two non-empty domain labels
separated by periods. The final domain label must contain at least two characters. Surrounding whitespace is
trimmed; internal whitespace is rejected.
The local part allows ASCII letters, digits, periods, underscores, percentage signs, plus signs and hyphens.
Domain labels allow ASCII letters, digits and hyphens. Capitalization is preserved for display.
Invalid values report:
`Invalid email address: Email must contain one @ symbol, a non-empty local part, and at least two non-empty domain labels separated by periods, without spaces.`

| Accepted email | Rejected email |
| --- | --- |
| `alex.tan@example.com` | `alex@example` |
| `alex+tuition@example.com` | `alex @example.com` |
| `Alex_Tan%25@Example.COM` | `@example.com` |
| `alex%school@example.com` | `alex@.com` |
| `alex+tuition@example.com` | `.alex@example.com` |
| `alex+tuition@example.com` | `alex@example..com` |

The local part accepts `%` in addition to the original `+`, `_`, `.`, and `-` characters, while special
characters cannot start or end the local part or appear consecutively. Domain labels retain the original
alphanumeric and hyphen placement restrictions, and the final label must contain at least two characters.
Validation checks the address format; it does not verify that a mailbox exists.

Contacts are duplicates when their role, name, phone and email all match. Name and email comparisons ignore
capitalization. Two omitted emails match; an omitted email differs from a supplied email. Otherwise, identical
contacts with different roles are allowed.

Examples:
* `add r/student n/John Doe p/98765432 e/johnd@example.com`
* `add r/student n/Alex Tan p/91234567` saves a contact without an email.
* `add r/student n/Alex Tan p/91234567 e/alex+tuition@example.com` saves a contact with an email.
* `add r/guardian n/Betsy Crowe e/betsycrowe@example.com p/1234567`

### Listing all contacts: `list`

Shows a list of all contacts in the address book.

Format: `list`

* Any text after `list` is rejected; for example, `list 3` shows `Invalid list command. Use: list`.
* The result shows the number of contacts listed; for example, `Listed 6 contacts`.
* If there are no saved contacts, `No contacts found.` is shown.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Omitting `e/` preserves the existing email, including its absence. Supplying an email uses the same validation
  as `add`. An empty `e/` is rejected.
* `e/NONE` removes the existing email. Only the exact value `NONE` in capital letters does this; `e/none` is
  treated as an invalid email.
* A name containing a `/` can be wrapped in double quotes, as in `add`. Anything inside the quotes is treated as
  part of the name, so `edit 1 n/"Randy p/e ratio"` sets the name to `Randy p/e ratio` without reading `p/` as a phone.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower` Edits the name of the 2nd person to be `Betsy Crower`.
*  `edit 2 e/NONE` Removes the email of the 2nd person.
*  `edit 3 n/"Ravi s/o Kumar"` Edits the name of the 3rd person to be `Ravi s/o Kumar`.

### Locating contacts by name: `find`

Finds contacts whose names contain the given `NAME`.

Format: `find NAME`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Partial names match; for example, `Han` matches `Hans`.
* `NAME` is matched as a whole, so word order matters; for example, `Hans Bo` matches `Hans Bonnie` but not `Bo Hans`.
* Leading and trailing spaces are ignored, and consecutive spaces are treated as one space.
* The search considers only names.
* `NAME` cannot be blank; `find` with no name shows `Invalid find command. Use: find NAME`.
* The result shows the number of matches; for example, `Found 2 contacts matching: li.`
* If no contact matches, `No contacts found matching: NAME` is shown.

Examples:
* `find ale` returns `Alex Yeoh`
* `find li` returns `Charlotte Oliveiro` and `David Li`

### Linking a student to a guardian: `link`

Format: `link s/STUDENT_INDEX g/GUARDIAN_INDEX`

* Run `list` or `find NAME` first and use the indexes in the **currently displayed list**.
* Both contacts must be visible. If a search hides either contact, run `list` before linking.
* `s/` must identify a student and `g/` must identify a guardian.
* Indexes must be positive whole numbers. Leading zeroes are accepted: `02` means `2`.
* Parameters may appear in either order; each must appear exactly once. Command words and prefixes are case-sensitive.
* Each student can have one guardian; a guardian can be linked to several students.
* Linking to a different guardian replaces the previous relationship. Linking to the same guardian again is rejected.
* A successful command selects the student and shows the guardian's current name, phone and email in the detail panel.
* The relationship is saved automatically and survives edits, reordering and restarting the application.

Examples, assuming Alex Tan is student `1`, Mei Tan is guardian `2`, and Sarah Lim is guardian `3`:

* `link s/1 g/2` shows `Linked student Alex Tan to guardian Mei Tan.`
* `link g/2 s/1` has the same effect, but is rejected if that relationship already exists: `Alex Tan is already linked to Mei Tan.`
* `link s/1 g/3` replaces Mei Tan and shows `Changed Alex Tan’s guardian from Mei Tan to Sarah Lim.`

Common errors:

| Input problem | Result |
|---|---|
| Missing parameter or extra text | `Invalid link command. Use: link s/STUDENT_INDEX g/GUARDIAN_INDEX` |
| Unknown parameter | `Unknown parameter. Use: link s/STUDENT_INDEX g/GUARDIAN_INDEX` |
| Repeated parameter | `Each of s/ and g/ may appear only once.` |
| Invalid student index | `Student index must be a positive integer shown in the list.` |
| Invalid guardian index | `Guardian index must be a positive integer shown in the list.` |
| Student index outside the displayed list | `No contact exists at student index INDEX.` |
| Guardian index outside the displayed list | `No contact exists at guardian index INDEX.` |
| Wrong student role | `Student index must refer to a student.` |
| Wrong guardian role | `Guardian index must refer to a guardian.` |

Failed link commands leave the contacts, relationships, selected contact, detail panel and saved data unchanged.

### Deleting a contact: `delete`

Deletes the specified contact from TutorRoster.

Format: `delete INDEX`

* Deletes the contact at the specified `INDEX`.
* The index refers to the index number shown in the displayed contact list.
* The index **must be a positive integer** 1, 2, 3, ...

Deleting an unlinked contact does not remove any student-guardian relationships. Deleting a linked student removes
that student's relationship while retaining the guardian. Deleting a guardian clears the relationships of all students
linked to that guardian while retaining those student contacts.

The success message reports how many relationships were removed, when applicable:

* Deleting an unlinked contact: `Deleted contact: Alex Tan [Student].`
* Deleting a linked student: `Deleted contact: Alex Tan [Student]. Removed 1 student-guardian relationship.`
* Deleting a guardian linked to one student: `Deleted contact: Mei Tan [Guardian]. Removed 1 student-guardian relationship.`
* Deleting a guardian linked to two students: `Deleted contact: Mei Tan [Guardian]. Removed 2 student-guardian relationships.`

Students without a guardian show `Guardian: None` in the detail panel.

Examples:
* `list` followed by `delete 2` deletes the 2nd contact in the displayed contact list.
* `find Betsy` followed by `delete 1` deletes the 1st contact in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TutorRoster automatically saves successful `add`, `edit`, `delete`, `clear` and `link` commands. You do not need to save manually. Read-only commands do not write the data file.

If saving fails, the command reports `Changes could not be saved. No changes were made.` Contacts, relationships and the previously saved data remain unchanged.

### Editing the data file

TutorRoster data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If the data file is invalid, TutorRoster starts with an empty list and disables data-changing commands for that session. It displays `Data changes are disabled because the data file could not be loaded. Fix the file and restart TutorRoster.` The invalid file is preserved. Back up the file before editing it, fix the error and restart the application.<br>
Contacts have permanent `id` values, and a student's optional `guardianId` must refer to an existing guardian's `id`. Keep IDs unique and preserve them when editing contact details. Invalid IDs, repeated JSON fields, duplicate contacts and invalid guardian references make the file invalid. Older valid files without IDs are accepted; IDs are assigned on loading and included in the next successful save.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous TutorRoster home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/ROLE n/NAME p/PHONE_NUMBER e/EMAIL` <br> e.g., `add r/student n/James Ho p/22224444 e/jamesho@example.com`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find NAME`<br> e.g., `find alex`
**Link**   | `link s/STUDENT_INDEX g/GUARDIAN_INDEX`<br> e.g., `link s/1 g/2`
**List**   | `list`
**Help**   | `help`
