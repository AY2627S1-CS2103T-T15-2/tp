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

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar tutorroster.jar`.<br>
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

* Contacts are always displayed in alphabetical order of name, ignoring capitalization.
* Contacts with the same name are displayed with guardians before students.
* Accented letters are ordered with their unaccented forms, so `Élise Tan` is displayed between `Edward Lim` and
  `Felix Ng`. Letters without a separate accent mark are also ordered with the letters they are based on: `ø` with
  `o`, `ł` with `l`, `æ` with `ae` and `ß` with `ss`. A name without accents is displayed before the same
  name with accents.
* Index numbers used by commands such as `edit` and `delete` refer to this displayed order.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`

### Adding a contact: `add`

Adds a student or guardian contact.

Format: `add r/ROLE n/NAME [p/PHONE_NUMBER] [e/EMAIL]`

`r/ROLE` and `n/NAME` are required. `ROLE` is `student` or `guardian`, ignoring letter case. The parameters may
appear in any order, but each may appear only once. If multiple supplied values are invalid, errors are checked in the
fixed parameter order `r/ROLE`, `n/NAME`, `p/PHONE`, then `e/EMAIL`, regardless of the order entered.
An empty `r/` is rejected with `Empty role parameter: Role cannot be empty when r/ is specified. Use student or guardian.`
An unsupported role is rejected with `Invalid role. Role must be student or guardian.`
If `r/` is omitted, the error starts with `Missing required field: r/ROLE.` If `n/` is omitted, it starts with
`Missing required field: n/NAME.` In either case, that line is followed by the same usage text:

    add: Adds a student or guardian contact.
    Parameters: r/ROLE n/NAME [p/PHONE] [e/EMAIL]
    Students may omit phone and email; guardians must provide at least one.
    Example: add r/student n/John Doe p/98765432 e/johnd@example.com

Repeating any parameter is rejected with `Each of r/, n/, p/, and e/ may appear only once.`
Phone and email are optional fields, subject to the guardian requirement below.

Phone is optional. Omit `p/` to save a contact without a phone number; the contact displays `Not provided`.
Supplying `p/` with an empty or whitespace-only value is rejected with:
`Empty phone parameter: Phone cannot be empty when p/ is specified.`

A guardian must have at least one contact method. Adding a guardian with neither `p/` nor `e/` is rejected with:
`A guardian must have at least one contact method: phone or email.`
For students, phone and email are independently optional. A student can have both, either one, or neither.

The phone field accepts contact details with at least 3 digits from `0-9` and a maximum of 100 characters.
Other numeral characters may appear as text, but do not count towards the minimum. You can include multiple
numbers, labels, spaces and punctuation, for example `p/(HP) +65 1234 5678 1111-3333 (Office)`. The `+` character
has no position restriction. The `@` character is reserved for email addresses, and `/` is reserved for command
prefixes. Control characters such as tabs and line breaks are not accepted; surrounding whitespace is trimmed. Invalid
phone values report the relevant reason: `Phone number must contain at least 3 digits (0-9).`,
`Phone details cannot exceed 100 characters.`, or
`Phone number cannot contain control characters, '@', or '/'.`

For example, `add p/12 n/Alex Tan r/teacher` reports the invalid role first even though `p/` appears first in the command.

Email is optional. Omit `e/` to save a contact without an email; the contact displays `Not provided`.
Supplying `e/` with an empty or whitespace-only value is rejected with:
`Empty email parameter: Email cannot be empty when e/ is specified.`

An email must have exactly one `@`, a non-empty local part, and at least two non-empty domain labels
separated by periods. The final domain label must contain at least two characters. Surrounding whitespace is
trimmed; internal whitespace is rejected.
The email address may contain at most 254 characters after trimming surrounding whitespace. Longer values report
`Email address cannot exceed 254 characters.`
The local part (before `@`) is limited to 64 characters, and each domain label (between periods) to 63.
Exceeding these limits reports `Email local part cannot exceed 64 characters.` or
`Email domain labels cannot exceed 63 characters.`, respectively.
The local part allows ASCII letters, digits, periods, underscores, percentage signs, plus signs and hyphens.
Domain labels allow ASCII letters, digits and hyphens. Capitalization is preserved for display.
Invalid input reports the specific reason, such as `Email address cannot contain whitespace.` or
`Email domain labels cannot be empty.`

| Accepted email | Rejected email | Reason for rejection |
| --- | --- | --- |
| `alex.tan@example.com` | `alex@example` | Fewer than two domain labels |
| `alex+tuition@example.com` | `alex@example.c` | Final domain label has fewer than two characters |
| `Alex_Tan%25@Example.COM` | `.alex@example.com` | Local part starts with a special character |
| `alex%school@example.com` | `alex..tan@example.com` | Consecutive special characters in the local part |
| `alex-tan@example.com` | `alex-@example.com` | Local part ends with a special character |
| `alex@example.com` | `alex@-example.com` | Domain label starts with a hyphen |

The local part accepts `%` in addition to the original `+`, `_`, `.`, and `-` characters, while special
characters cannot start or end the local part or appear consecutively. Domain labels retain the original
alphanumeric and hyphen placement restrictions, and the final label must contain at least two characters.
Validation checks the address format; it does not verify that a mailbox exists.

Contacts are exact duplicates when their role, name, phone and email all match. Name and email comparisons ignore
capitalization; phone details are compared after trimming surrounding whitespace, while internal spaces and hyphens
remain significant. Two omitted values match. Exact duplicates with at least one contact method are rejected with
`This contact already exists.` A student with neither a phone number nor an email address is not rejected as an exact
duplicate based on role and name alone.

When a contact has the same role and name as an existing contact, Add may display a possible-duplicate warning and
still add the contact. A matching phone after spaces and hyphens are removed produces a phone warning. A matching
email produces an email warning. If either contact method is missing from both contacts, and neither of those more
specific warnings applies, a general warning is shown. A contact with the same role and name but different phone and
email values receives no warning when neither value matches under these rules. Contacts with different roles or names
do not trigger these warnings. The normal success message is displayed together with any warning.

Examples:

* `add r/student n/Alex Tan p/91234567 e/alex@example.com` followed by `add r/student n/alex tan p/91234567 e/ALEX@example.com`
  is rejected as an exact duplicate because name and email capitalization are ignored.
* `add r/student n/Alex Tan p/9123 4567` followed by `add r/student n/Alex Tan p/91234567` is added with a phone
  warning. The phone values are similar after spaces are removed, but internal spaces are significant for exact duplicates.
* Two contacts with the same role and name and the same phone after spaces and hyphens are removed receive the phone
  warning if they are not exact duplicates.
* Two contacts with the same role and name and the same email but different phone values receive the email warning.
* Two students with the same role and name and no phone or email receive a general warning; both are added.

An unknown parameter such as `a/15` is rejected with `Invalid parameter. Valid parameters are r/ROLE, n/NAME, p/PHONE,
and e/EMAIL.` If an unquoted slash occurs in the name, the message also says to enclose the entire name in double
quotes. For example, use `add r/student n/"AC/DC"` or `add r/student n/"Ravi s/o Kumar"`. For Add, every slash in a
name must be quoted, including an embedded slash such as the one in `AC/DC`. Edit has its own parsing behavior and
continues to accept unquoted embedded slashes.

After a contact is added, TutorRoster shows all contacts, even if the list was filtered by an earlier `find`, and
selects the new contact so that its details appear in the detail panel.

Examples:
* `add r/student n/John Doe p/98765432 e/johnd@example.com`
* `add r/student n/Alex Tan p/91234567` saves a contact without an email.
* `add r/student n/Alex Tan p/91234567 e/alex+tuition@example.com` saves a contact with an email.
* `add r/guardian n/Mei Tan e/mei@example.com` saves a guardian without a phone number.
* `add r/student n/Ben Lim` saves a student without a phone number or email.
* `add r/guardian n/Betsy Crowe e/betsycrowe@example.com p/1234567`

### Listing all contacts: `list`

Shows a list of all contacts in TutorRoster.

Format: `list`

* Any text after `list` is rejected; for example, `list 3` shows `Invalid list command. Use: list`.
* The result shows the number of contacts listed; for example, `Listed 6 contacts`.
* If there are no saved contacts, `No contacts found.` is shown.
* Each contact is shown with its index, name and role, phone number, and email address. Students also show
  their linked guardian. For example:
  ```
  1. Alex Tan [Student]
  Phone: 91234567
  Email: alex@example.com
  Guardian: Mei Tan
  ```
* If a student has no linked guardian, `Guardian: None` is shown.
* If a contact has no phone number or email address, `Not provided` is shown instead; for example, `Phone: Not provided`.

### Editing a contact: `edit`

Edits an existing contact.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`

* Edits the contact at the specified `INDEX`. The index refers to the index number shown in the displayed contact list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* After a successful edit, the edited contact is selected and its details are shown in the details panel.
* Omitting `e/` preserves the existing email, including its absence. Supplying an email uses the same validation
  as `add`. An empty `e/` is rejected.
* `e/NONE` removes the existing email. `NONE` ignores letter case, so `e/none` also works.
* Omitting `p/` preserves the existing phone number, including its absence. Supplying a phone number uses the same
  validation as `add`. An empty `p/` is rejected.
* `p/NONE` removes the existing phone number. As with `e/NONE`, letter case is ignored.
* A guardian must keep at least one contact method. An edit that would leave a guardian with neither a phone number
  nor an email is rejected with `A guardian must have at least one contact method: phone or email.`
  A student can have both removed.
* A name containing a word that ends in `/`, such as `s/o`, must be wrapped in double quotes. Anything inside the
  quotes is treated as part of the name, so `edit 1 n/"Randy p/e ratio"` sets the name to `Randy p/e ratio` without
  reading `p/` as a phone. Unquoted, `edit 1 n/Ravi s/o Kumar` is rejected because `s/` is read as a parameter.
* Each of `n/`, `p/` and `e/` may appear only once. Any other parameter, such as `r/` or `a/`, is rejected with
  `Unknown parameter. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`. The role of a contact cannot be edited.
* A missing index, or extra text before the first parameter, is rejected with
  `Invalid edit command. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`.
* An index that is not a positive whole number is rejected with
  `The contact index must be a positive integer shown in the list.` Leading zeroes are accepted, so `02` means 2.
  An index larger than the displayed list is rejected with `No contact exists at index INDEX.`
* An edit that would make the contact identical to another contact is rejected with
  `This edit would create a duplicate contact.`
* If the edited contact has the same role and name as another contact, and the two also share a phone number,
  an email address, or a missing phone number or email, the edit still goes ahead but a warning is shown below the
  success message, as with `add`. For example, renaming a student to the name of another student with the same phone
  number but a different email shows `Warning: Another contact with the same role and name, and a phone number
  matching after ignoring spaces and hyphens exists. This contact may be a duplicate, but it was updated.`

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st contact to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower` Edits the name of the 2nd contact to be `Betsy Crower`.
*  `edit 2 e/NONE` Removes the email of the 2nd contact.
*  `edit 2 p/NONE` Removes the phone number of the 2nd contact.
*  `edit 3 n/"Ravi s/o Kumar"` Edits the name of the 3rd contact to be `Ravi s/o Kumar`.

### Locating contacts by name: `find`

Finds contacts whose names contain the given `NAME`.

Format: `find NAME`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Partial names match; for example, `Han` matches `Hans`.
* `NAME` is matched as a whole, so word order matters; for example, `Hans Bo` matches `Hans Bonnie` but not `Bo Hans`.
* Leading and trailing spaces are ignored, and consecutive spaces are treated as one space.
* Curly and straight apostrophes are treated as the same; for example, `o'brien` matches `O’Brien`.
* Accents are ignored; for example, `elise` matches `Élise`, `élise` matches `Elise`, and `soren` matches `Søren`.
* The search considers only names.
* `NAME` cannot be blank; `find` with no name shows `Invalid find command. Use: find NAME`.
* The result shows the number of matches; for example, `Found 2 contacts matching: li.`
* If no contact matches, `No contacts found matching: NAME` is shown.
* Matching contacts are shown in the same format as `list`.

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
* Students with the same name remain separate contacts, even if both have no phone or email.
  Use the intended student's displayed index; linking one does not change the other student's guardian.
* Linking to a different guardian replaces the previous relationship. Linking to the same guardian again is rejected.
* A successful command selects the student and shows the guardian's current name, phone and email in the detail panel.
  The command box stays focused so you can immediately enter the next command.
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

![A linked student selected with the guardian's contact details displayed](images/StudentGuardianLink.png)

### Deleting a contact: `delete`

Deletes the specified contact from TutorRoster.

Format: `delete INDEX`

* Deletes the contact at the specified `INDEX`.
* The index refers to the index number shown in the displayed contact list.
* The index **must be a positive integer** 1, 2, 3, ...
* A value that is not a positive integer is rejected with
  `The contact index must be a positive integer shown in the list.`
* An index outside the displayed list is rejected with `No contact exists at index INDEX.`

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

Clears all contacts from TutorRoster.

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
**Add**    | `add r/ROLE n/NAME [p/PHONE_NUMBER] [e/EMAIL]` <br> e.g., `add r/student n/James Ho p/22224444 e/jamesho@example.com`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find NAME`<br> e.g., `find alex`
**Link**   | `link s/STUDENT_INDEX g/GUARDIAN_INDEX`<br> e.g., `link s/1 g/2`
**List**   | `list`
**Help**   | `help`
