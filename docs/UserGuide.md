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
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

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

Format: `add r/ROLE n/NAME [p/PHONE] [e/EMAIL]`

**Role and name are required.** Students may provide phone, email, both, or neither; guardians must provide at
least one contact method.

Examples:

| To add... | Enter |
| --- | --- |
| A student with phone and email | `add r/student n/Alex Tan p/91234567 e/alex@example.com` |
| A student without contact details | `add r/student n/Ben Lim` |
| A guardian with email only | `add r/guardian n/Mei Tan e/mei@example.com` |
| A student whose name contains `/` | `add r/student n/"Ravi s/o Kumar" p/98765432` |

Use lowercase `add` and prefixes (`r/`, `n/`, `p/`, `e/`). Parameters may appear in any order, once each.
Omit an optional field's prefix rather than leaving its value empty, as in `p/`.

On success, the contact is saved and selected, and its details appear in the detail panel. Guardians and active
students are shown, clearing any `find` filter. Former students remain hidden. The command box clears and stays
focused. The first example reports
`Added contact: Alex Tan [Student].`

#### Field rules

Surrounding whitespace is removed from field values. Names and emails keep their capitalization.

**Role (`r/`)**: `student` or `guardian`, ignoring letter case.

**Name (`n/`)**: A non-blank name of **1–70 characters**, allowing letters and numbers (including non-English
scripts), spaces, apostrophes (`'` or `’`), hyphens, periods, and slashes. Consecutive spaces become one space
before the length check. Enclose the **entire name** in double quotes if it contains `/`, even for `n/"AC/DC"`.
The enclosing quotes are not saved.

**Phone (`p/`)**: At least **3 digits from `0–9`**, with a maximum of **100 characters**. Other numerals are allowed
as text but do not count. Labels, multiple numbers, spaces, and punctuation are accepted, for example
`p/(HP) +65 1234 5678 1111-3333 (Office)`. The `+` may appear anywhere. Do not include `@`, `/`, or control
characters such as tabs and line breaks.

**Email (`e/`)**: At most **254 characters**. TutorRoster checks the format, not whether the mailbox exists:

* Exactly one `@`, a non-empty part on each side, and no internal whitespace.
* Before `@`: English letters (`A–Z`, `a–z`), digits (`0–9`), and `.`, `_`, `%`, `+`, or `-`.
  The local part is limited to **64 characters**. Punctuation must appear singly between letters or digits,
  as in `alex+tuition@example.com`.
* After `@`: at least two non-empty labels separated by periods, as in `example.com`. Labels allow English
  letters, digits, and single hyphens between letters or digits. Each label is limited to **63 characters**.
  The final label needs at least two characters.

Common email mistakes:

| Invalid input | Reason |
| --- | --- |
| `alex @example.com` | Internal whitespace. |
| `@example.com` | Missing part before `@`. |
| `alex@.com` or `alex@example..com` | Empty domain label. |
| `alex@example` or `alex@example.c` | Missing second label or final label too short. |
| `.alex@example.com` or `alex..tan@example.com` | Invalid punctuation placement. |

#### Duplicate contacts and warnings

Checks include **all saved contacts**, including former students and contacts hidden by a search or list filter.
These rules apply to contacts with the **same role and name**, ignoring name capitalization:

| Condition, checked in this order | Result |
| --- | --- |
| Phone and email both match, with at least one contact method present | Rejected: `This contact already exists.` |
| Phone matches after spaces and hyphens are removed | Added with a phone warning. |
| Email matches, ignoring capitalization | Added with an email warning. |
| Phone is missing from both contacts, or email is missing from both | Added with a general warning. |
| None of the above | Added without a warning. |

Exact duplicates ignore email capitalization and treat two omitted values as matching. **Internal phone spaces and
hyphens remain significant for rejection**: `p/9123 4567` and `p/91234567` produce a warning instead.

Two students named `Ben Lim` with no phone or email are both saved, with a general warning for the second.
Different roles or names trigger no duplicate checks. Only the highest-priority warning is shown, after the success
message; **a warning does not prevent saving**.

#### Correcting an unsuccessful add

Missing required fields, empty values, repeated parameters, and unknown prefixes such as `a/15` are rejected.
A missing role or name error identifies the field and shows the command format. Other invalid values receive a
specific reason, such as `Email address cannot exceed 254 characters.` Correct the indicated problem and try again.

For example, `add r/guardian n/Mei Tan` reports
`A guardian must have at least one contact method: phone or email.` Supply `p/PHONE` or `e/EMAIL`.

On failure, contacts, selection, and saved data stay unchanged; the command remains for correction.
For saving errors, see [Saving the data](#saving-the-data).

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
  Unlike `add`, `edit` accepts embedded slashes without quotes, for example `edit 1 n/AC/DC`.
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
**Add**    | `add r/ROLE n/NAME [p/PHONE] [e/EMAIL]` <br> e.g., `add r/student n/James Ho p/22224444 e/jamesho@example.com`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find NAME`<br> e.g., `find alex`
**Link**   | `link s/STUDENT_INDEX g/GUARDIAN_INDEX`<br> e.g., `link s/1 g/2`
**List**   | `list`
**Help**   | `help`
