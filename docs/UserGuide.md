---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TutorRoster User Guide

TutorRoster is a **desktop contact-management application for independent private tutors** managing a small group of
secondary school or junior college students and their guardians. Typed commands help tutors keep contact details
up to date and retrieve them quickly, with a graphical contact list and a panel for the selected contact's details.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T15-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your TutorRoster.
   Use a folder where you have permission to create and update files. Avoid write-protected folders.

1. Open a terminal, run `java -version` to check the Java version, then `cd` to the folder containing the JAR file
   and run `java -jar` followed by the JAR's file name in quotes, for example `java -jar "tutorroster.jar"`
   (use `"[CS2103T-T15-2][TutorRoster].jar"` instead if your copy has that name).
   Double-clicking the JAR might not work on some systems.<br>
   The application opens with your saved contacts, or an empty list if there is no data file yet.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists guardians and active students.

   * `add r/student n/John Doe p/98765432 e/johnd@example.com` : Adds a student named `John Doe`.

   * `delete 3` : Deletes the 3rd contact shown in the current list.
   * `delete 1 3 5` : Deletes the 1st, 3rd and 5th contacts in one operation.

   * `clear confirm` : Permanently deletes all contacts after explicit confirmation.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Command words ignore letter case, so `add`, `ADD` and `AdD` are equivalent.
  Add's parameter prefixes also ignore letter case; use the documented lowercase prefixes for other commands.

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `edit INDEX [n/NAME] [p/PHONE]` can be used as `edit 1 n/John Doe` or as `edit 1 p/91234567`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

* Pasted Unicode space separators, including non-breaking spaces, are treated as ordinary spaces throughout
  commands, including quoted values. Names, levels and subjects follow their usual space rules; surrounding
  email spaces are removed, while spaces inside an email remain invalid. Internal tabs and line breaks are
  still checked under each field's rules.

* Extraneous parameters for commands that take no parameters, such as `help` and `exit`, are ignored.
  `clear` requires the explicit argument `confirm`; all other arguments are rejected.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

<box type="info" seamless>

**Contact order:**<br>

* Contacts are always displayed in alphabetical order of name, ignoring capitalization.
* Contacts with the same name are displayed with guardians before students.
  This includes capitalization variants such as Greek `ΟΣ`, `Οσ` and `ος`, and equivalent Unicode spellings
  of accented letters. The original spelling of each saved name is preserved.
* Accented letters are ordered with their unaccented forms, so `Élise Tan` is displayed between `Edward Lim` and
  `Felix Ng`. Letters without a separate accent mark are also ordered with the letters they are based on: `ø` with
  `o`, `ł` with `l`, `æ` with `ae` and `ß` with `ss`. A name without accents is displayed before the same
  name with accents.
* Index numbers used by commands such as `edit` and `delete` refer to this displayed order.
</box>

### Viewing help: `help`

Opens a command reference listing the available commands and their formats. For full instructions, examples and
field rules, use **Copy URL** to copy the User Guide link and open it in your browser.
The Help window can be resized and scrolled. You can also press **F1** in the main window to open Help.

Format: `help`

![Help window showing the User Guide URL, Copy URL button and command reference](images/helpMessage.png)

*The Help window opens at the top of the command reference. Scroll down to see the remaining commands.*

### Adding a contact: `add`

Adds a student or guardian contact.

Format: `add r/ROLE n/NAME [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]`

**Role and name are required.** Students may have phone, email, both, or neither. Guardians must have at least
one contact method. Level and subject are optional and available **only for students**.

Examples:

| To add... | Enter |
| --- | --- |
| A student with phone and email | `add r/student n/Alex Tan p/91234567 e/alex@example.com` |
| A student with level and subject | `add r/student n/Alex Tan lv/Sec 3 sub/Math` |
| A student without contact details | `add r/student n/Ben Lim` |
| A guardian with email only | `add r/guardian n/Mei Tan e/mei@example.com` |
| A student whose name contains `/` | `add r/student n/"Ravi s/o Kumar" p/98765432` |

The command word and its prefixes (`r/`, `n/`, `p/`, `e/`, `lv/`, `sub/`) accept any letter case, for example
`ADD R/student N/Alex Tan Lv/Sec 3 SuB/Math`. Parameter values retain their capitalization.
Parameters may appear in any order, once each. Omit an optional field's prefix if unused;
an empty prefix such as `p/` is rejected.
Whitespace before a prefix, including pasted tabs or line breaks, separates parameters just like a space.
Whitespace inside a value follows that field's rules; for example, a tab inside a phone number is rejected.
Omitted phone and email values display as `Not provided`.

On success, the contact is saved and selected, and its details appear in the detail panel. Guardians and active
students are shown, clearing any search or list filter. Former students remain hidden. The command box clears and
stays focused. The first example reports `Added contact: Alex Tan [Student].`

#### Field rules

Surrounding whitespace is removed from field values. Capitalization is preserved for names, emails, levels and subjects.

**Role (`r/`)**: `student` or `guardian`, ignoring letter case.

**Name (`n/`)**

* Enter **1 to 70 characters**, including at least one letter or digit.
* Names may contain letters and numbers from non-English scripts, spaces, apostrophes (`'`, `‘`, `’`),
  hyphens, periods, middle dots (`·`), and slashes.
* Surrounding spaces are removed, including inside enclosing quotes. Repeated spaces become one before checking
  the length. Capitalization and punctuation are preserved.

**Names containing `/` must be enclosed in straight double quotes**, for example `n/"Ravi s/o Kumar"`
or `n/"AC/DC"`. The enclosing quotes are not saved.

Text resembling a parameter stays part of a quoted name: `n/"Ravi p/e ratio"` does not supply a phone number.
Names made entirely of punctuation, such as `...` or `-`, are rejected with
`Name must contain at least one letter or digit.`

**Phone (`p/`)**: At least **3 digits from `0-9`**, with a maximum of **100 characters**. Other numerals are allowed
as text but do not count. Labels, multiple numbers, spaces, and punctuation are accepted, for example
`p/(HP) +65 1234 5678 1111-3333 (Office)`. The `+` may appear anywhere. Do not include `@`, `/`, or control
characters such as tabs and line breaks.

**Email (`e/`)**: At most **254 characters**. TutorRoster checks the format, not whether the mailbox exists:

* Exactly one `@`, a non-empty part on each side, and no internal whitespace.
* Before `@`: English letters (`A-Z`, `a-z`), digits (`0-9`), and `.`, `_`, `%`, `+`, or `-`.
  The local part is limited to **64 characters**. Periods cannot appear at the beginning or end, or consecutively.
  Other listed punctuation may appear consecutively or at either end.
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

**Level (`lv/`) and subject (`sub/`)**: Optional fields for students only. Each value must contain **1 to 30 characters**,
including a letter or digit. Allowed characters are letters and digits (including non-English scripts), spaces,
hyphens, periods, plus signs and parentheses. Repeated spaces become one space before the length check.

Subjects also allow `/` and `&`. Quote the entire subject if it contains a slash or prefix-like text, for example
`sub/"Combined Science (Chem/Bio)"`. The quotes are removed before validation and are not saved or counted.
This syntax works in `add`, `edit` and `list`; `sub/English Language & Literature` needs no quotes.
Use unquoted level values, for example `lv/Sec 3`.

`NONE` is reserved, ignoring capitalization. In `edit`, use unquoted `lv/NONE` or `sub/NONE` to remove the field.

#### Duplicate contacts and warnings

Checks include **all saved contacts**, including former students and contacts hidden by a filter. These rules
apply to contacts with the **same role and name**. Exact duplicate rejection ignores name capitalization.
Possible-duplicate warnings also recognize canonically equivalent Unicode spellings (such as composed and
decomposed `É`), straight (`'`), opening-curly (`‘`) and closing-curly (`’`) apostrophes, and repeated or surrounding spaces
in saved names.
Accents and word boundaries remain significant: `Élise` differs from `Elise`, and `Alex Tan` differs from `AlexTan`.
Names are saved as entered after normal command parsing. Level and subject do not affect these checks.

| Condition, checked in this order | Result |
| --- | --- |
| Phone and email both match, with at least one contact method present, and names match ignoring capitalization only | Rejected: `This contact already exists.` |
| Phone matches after spaces and hyphens are removed | Added with a phone warning. |
| Email matches, ignoring capitalization | Added with an email warning. |
| Phone is missing from both contacts, or email is missing from both | Added with a general warning. |
| None of the above | Added without a warning. |

Phone warnings and `check duplicates` also ignore Unicode space separators in phone numbers already saved in the
data file. Those saved phone values are preserved.

Exact duplicates ignore email capitalization and treat two omitted values as matching. **Internal phone spaces and
hyphens remain significant for rejection**: `p/9123 4567` and `p/91234567` produce a warning instead.

Two students named `Ben Lim` with no phone or email are both saved, with a general warning for the second.
Different roles or names trigger no duplicate checks. Only the highest-priority warning is shown, after the success
message; **a warning does not prevent saving**.

#### Correcting an unsuccessful add

Missing required fields, empty values, repeated parameters, and unknown prefixes such as `a/15` are rejected.
A missing role or name error identifies the field and shows the command format. Unknown-parameter errors identify
the invalid prefix and list valid ones; repeated-parameter errors name the repeated prefix. A slash in an unquoted
name also prompts you to quote the entire name.

After parameter syntax checks, field values are checked in the order `r/`, `n/`, `p/`, `e/`, `lv/`, `sub/`.
For example, `add p/12 n/Alex Tan r/teacher` reports the invalid role first.
For guardians, the contact-method requirement is checked after phone and email; any `lv/` or `sub/` is then rejected.

Other errors explain the reason, for example:

* `add r/guardian n/Mei Tan` reports `A guardian must have at least one contact method: phone or email.`
* `add r/guardian n/Mei Tan p/91234567 lv/Sec 3` reports
  `Only students can have a level or subject. Remove lv/ and sub/ when adding a guardian.`
* `add r/student n/Alex Tan p/` reports `Empty phone parameter: Phone cannot be empty when p/ is specified.`
* `add r/student n/Alex Tan sub/---` reports `Subject must contain at least one letter or digit.`

Level and subject errors distinguish excessive length, unsupported characters and values without any letters or
digits. Email errors distinguish unsupported characters from punctuation placement, such as consecutive periods.

Correct the indicated problem and try again. On failure, contacts, selection, and saved data stay unchanged;
the command remains for correction. For saving errors, see [Saving the data](#saving-the-data).

### Listing contacts: `list`

Shows guardians and active students, or only the contacts you choose.

Format: `list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]`

Command | Shows
--------|------
`list` | Guardians and active students
`list r/student` | Active students only
`list r/guardian` | Guardians only
`list st/former` | Former students only
`list st/all` | Every contact, including former students
`list r/student st/all` | All students, active and former
`list lv/Sec 3` | Active students at level Sec 3
`list lv/Sec 3 sub/A-Math` | Active Sec 3 students who take A-Math
`list sub/Physics st/all` | All students who take Physics, active and former

* By default, former students are hidden; see [Marking a student as former](#marking-a-student-as-former-archive).
* `r/ROLE` shows only contacts with that role. `ROLE` must be `student` or `guardian`, and ignores letter case.
* `st/STATUS` chooses which contacts to show by status. `STATUS` must be `active`, `former` or `all`, and ignores
  letter case:
  * `st/active` shows guardians and active students. This is the default, so `list st/active` is the same as `list`.
  * `st/former` shows only former students.
  * `st/all` shows every contact, whatever its status.
* `lv/LEVEL` shows only students at that level, and `sub/SUBJECT` only students taking that subject. Letter case
  and spaces are ignored, so `lv/sec3` matches a student at `Sec 3`. The whole value must match: `sub/Math` does
  not match `A-Math`. The same accented spelling matches across input methods. Accents remain significant, so `Français`
  does not match `Francais`. A student without a level never matches `lv/`, and one without a subject never matches
  `sub/`. `LEVEL` and `SUBJECT` follow the same rules as in `add`, and `NONE` cannot be used.
* Quote a subject that contains text resembling a parameter, as in `add`; for example,
  `list sub/"Art n/Design"`. Without the quotation marks, `n/` is read as an unknown parameter and the command is
  rejected.
* Parameters can be given in any order, and each can appear only once. A contact must match all of them.
  Without `st/`, former students are hidden, so add `st/all` to include them.
* Guardians are never former, so `list r/guardian st/former` is rejected with
  `Guardians cannot be former. Remove r/guardian or st/former.`
* Guardians have no level or subject, so `list r/guardian lv/Sec 3` is rejected with
  `Guardians have no level or subject. Remove r/guardian, or remove lv/ and sub/.`
* Any other text after `list` is rejected; for example, `list 3` shows
  `Invalid list command. Use: list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]`. If you type a role or status
  without its prefix, the message suggests the right command; for example, `list former` shows
  `Invalid list command. Did you mean: list st/former?`
* An invalid role is rejected with `Invalid role. Role must be student or guardian.`, an invalid status with
  `Invalid status. Status must be active, former, or all.`, and repeating a parameter with
  `Each of r/, st/, lv/ and sub/ may appear only once.` An invalid level or subject gets the same error as in
  `add`; for example, `list lv/` shows `Empty level parameter: Level cannot be empty when lv/ is specified.`
* If several parameters are invalid, only the first error is shown, in the order `r/`, `st/`, `lv/`, `sub/`.
* The result shows the number of contacts listed; for example, `Listed 6 contacts` or `Listed 2 former students`.
  When the list hides former students that would otherwise be shown, the result also says how many; for example,
  `Listed 6 contacts. 2 former students hidden. Add st/all to include them.`
* If there are no saved contacts, `No contacts found.` is shown. If every saved contact is a former student,
  `No active contacts found. Use list st/former to see former students.` is shown. If there are no former students,
  `list st/former` shows `No former students found.`
* If no student has the chosen level or subject, `No contacts found.` is shown. If former students would match,
  the result also says how many are hidden; for example,
  `No contacts found. 1 former student hidden. Add st/all to include them.`
* If no contact has the chosen role, `list r/guardian` shows `No guardians found.` and `list r/student` shows
  `No students found.` If every student is former, `list r/student` shows
  `No active students found. Use list r/student st/former to see former students.`
* Each contact is shown with its index, name, a `Student` or `Guardian` badge, phone number, and email address.
  Students also show their linked guardian. For example, Alex Tan has a `Student` badge beside his name and
  the following contact details:
  ```
  1. Alex Tan
  Phone: 91234567
  Email: alex@example.com
  Guardian: Mei Tan
  ```
* If a student has no linked guardian, `Guardian: None` is shown.
* If a contact has no phone number or email address, `Not provided` is shown instead; for example, `Phone: Not provided`.
* If a student has a level or subject, their card shows both on one line, for example
  `Level: Sec 3 | Subject: Math`. A missing value is shown as `Not set`, for example
  `Level: Sec 3 | Subject: Not set`. The line is hidden when both fields are missing.
* Selecting a student shows separate `Level:` and `Subject:` lines in the details panel. Each missing value is shown
  as `Not set`. Guardians do not show level or subject details.
* A former student has a `Former` badge beside the role badge.

### Marking a student as former: `archive`

Marks a student as former while keeping their details and guardian link.

Format: `archive INDEX`

* Use one positive index from the **currently displayed list**. Guardians cannot be archived.
* The current filter is kept. The student disappears from active-only views such as `list`, but remains visible
  with a `Former` badge after `list st/all` or `find NAME`. Their guardian remains in the active list.
* An already-former student is rejected. Success reports `Marked NAME as a former student.`

Example: `find Alex`, then `archive 1`, marks the first search result as former if it is an active student.

**Common archive/unarchive errors:**

| Input | Result |
| --- | --- |
| Missing index | `Missing contact index. Use: archive INDEX` |
| More than one argument | `Only one contact index is allowed. Use: archive INDEX` |
| Zero, negative or non-integer index | `The contact index must be a positive integer shown in the list.` |
| Index beyond the displayed list | `No contact exists at index INDEX.` |
| Guardian target | `Only students can be archived.` for `archive`; `Only students can be unarchived.` for `unarchive`. |
| Already in the requested status | `NAME is already a former student.` for `archive`; `NAME is already an active student.` for `unarchive`. |

For `unarchive`, the usage text shows `unarchive INDEX` instead. Rejected commands leave contacts unchanged.

### Marking a former student as active: `unarchive`

Marks a returning former student as active, keeping their details and guardian link.

Format: `unarchive INDEX`

* First display former students with `list st/former`, `list st/all` or `find NAME`.
* The current filter is kept. A reactivated student disappears from a former-only view, but remains visible
  after `list st/all` or `find NAME`, without the `Former` badge. Level and subject filters still apply.
* Guardians and already-active students are rejected. Index rules are the same as for `archive`.
  Success reports `Marked NAME as an active student.`

Example: `list st/former`, then `unarchive 1`, reactivates the first displayed former student.

### Editing a contact: `edit`

Updates the contact at an index in the **currently displayed list**.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]`

* Use a positive index and supply at least one field. Omitted fields keep their values; empty values are rejected.
  Each parameter may appear only once. The contact's role cannot be edited, and other parameters are rejected.
* Values follow the [field rules](#field-rules). Quote names containing prefix-like words, such as
  `n/"Ravi s/o Kumar"` or `n/"Randy p/e ratio"`. Embedded slashes such as `n/AC/DC` also work without quotes in `edit`.
* Remove optional fields using the following values. `NONE` ignores capitalization, so `e/none` also works.

  | Field | Set or update | Remove |
  | --- | --- | --- |
  | Phone | `p/91234567` | `p/NONE` |
  | Email | `e/alex@example.com` | `e/NONE` |
  | Student level | `lv/Sec 4` | `lv/NONE` |
  | Student subject | `sub/Physics` | `sub/NONE` |

* Guardians must keep a phone number or email address and cannot have their level or subject set or removed.
  Students may have all four optional fields removed.
* A successful edit selects the contact and replaces the current filter with `list st/former` for a former student,
  or `list` for any other contact. The contact keeps its ID, student status and guardian link.
  A successful change clears the opportunity to undo an earlier deletion.
* An unchanged edit, including removing an already-absent field, is rejected with
  `No changes made: the new values are the same as the current ones.` Capitalization-only edits count as changes.
  Rejected edits preserve the current view and deletion undo.
* An exact duplicate is rejected with `This edit would create a duplicate contact.` A possible duplicate is saved
  with a warning, using the shared [duplicate rules](#duplicate-contacts-and-warnings).

**Common errors:**

| Input problem | Result |
| --- | --- |
| Missing index or extra text before the first parameter | `Invalid edit command. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]` |
| Zero, negative or non-integer index | `The contact index must be a positive integer shown in the list.` |
| Index beyond the displayed list | `No contact exists at index INDEX.` |
| No field supplied | `At least one of n/, p/, e/, lv/, or sub/ must be provided.` |
| Repeated parameter | `Each of n/, p/, e/, lv/, and sub/ may appear only once.` |
| Unsupported parameter | `Unknown parameter. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]` |
| Removing a guardian's last contact method | `A guardian must have at least one contact method: phone or email.` |
| Setting or removing a guardian's level or subject | `Only students can have a level or subject. Remove lv/ and sub/ when editing a guardian.` |

Leading zeroes are accepted: `02` means index 2.

**Examples:**

| Command | Effect |
| --- | --- |
| `edit 1 p/91234567 e/johndoe@example.com` | Updates the first displayed contact's phone and email. |
| `edit 2 n/Betsy Crower` | Renames the second displayed contact. |
| `edit 2 e/NONE` | Removes their email, provided a guardian still has a phone. |
| `edit 3 lv/Sec 4 sub/NONE` | Sets a student's level and removes their subject. |
| `edit 3 n/"Ravi s/o Kumar"` | Sets the full name, treating `s/o` as name text. |

### Locating contacts by name: `find`

Finds contacts whose names contain the given `NAME`.

Format: `find NAME`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Partial names match; for example, `Han` matches `Hans`.
* `NAME` is matched as a whole, so word order matters; for example, `Hans Bo` matches `Hans Bonnie` but not `Bo Hans`.
* Leading and trailing spaces are ignored, and consecutive spaces are treated as one space.
* Curly and straight apostrophes are treated as the same; for example, `o'brien` matches `O’Brien`.
* Accents are ignored; for example, `elise` matches `Élise`, `élise` matches `Elise`, and `soren` matches `Søren`.
  Vowel signs in scripts such as Devanagari are part of the name and must match; for example, `किरण` does not match
  `करण`.
* The search considers only names.
* Type the name only, without `n/`. Everything after `find` is treated as part of the name, so `find n/Alex`
  matches only names containing the text `n/Alex`, not every name containing `Alex`.
* Former students are included in the results with a `Former` badge.
* `NAME` cannot be blank; `find` with no name shows `Invalid find command. Use: find NAME`.
* The result shows the number of matches; for example, `Found 2 contacts matching: li.`
* If no contact matches, `No contacts found matching: NAME` is shown.
* Matching contacts are shown in the same format as `list`.

Examples, assuming your contacts include Alex Yeoh, Charlotte Oliveiro and David Li:
* `find ale` returns `Alex Yeoh`
* `find li` returns `Charlotte Oliveiro` and `David Li`

### Viewing a contact's details: `view`

Shows the full details of the specified contact in the details panel.

Format: `view INDEX`

* Selects the contact at the specified `INDEX` in the list.
* The index refers to the index number shown in the displayed contact list.
* The index **must be a positive integer** 1, 2, 3, ...
* A value that is not a positive integer is rejected with
  `The contact index must be a positive integer shown in the list.`
* An index outside the displayed list is rejected with `No contact exists at index INDEX.`
* The details panel shows the contact's name with a `Student` or `Guardian` badge, and a `Former` badge for a former
  student. Below the name, the details panel groups the contact's details into sections:
  * **Details**: the contact's role, phone number and email address.
  * **Tuition** (students only): the student's level and subject.
  * **Guardian** (students only): the linked guardian's name, phone number and email address, or `Guardian: None` if
    no guardian is linked.
* `view` also works on the results of `list st/former`, `list st/all` and `find`. A former student's role is shown as
  `Role: Student [Former]`.
* The displayed list does not change, so you can keep using the same indexes afterwards.
* The result names the contact; for example, `Viewing contact: Alex Tan [Student].`

Examples:
* `list` followed by `view 2` shows the details of the 2nd contact in the displayed contact list.
* `find Alex` followed by `view 1` shows the details of the 1st contact in the results of the `find` command.

### Linking a student to a guardian: `link`

Format: `link s/STUDENT_INDEX g/GUARDIAN_INDEX`

To remove a relationship, use `link s/STUDENT_INDEX g/NONE` (`NONE` ignores case).

* Use indexes from the **currently displayed list**. Both contacts must be visible for linking or replacement;
  only the student must be visible for `g/NONE`. Run `list` or `find NAME` first.
  For former students, use `list st/all` or a shared search such as `find Tan` without reactivating them.
* `s/` must identify a student; `g/` must identify a guardian or contain `NONE`.
  Supply each case-sensitive prefix once, in either order. Indexes must be positive whole numbers;
  leading zeroes are accepted (`02` means `2`).
* Each student can have one guardian; guardians can have several students. A different guardian replaces the
  previous link; linking the same guardian again is rejected. Same-name students remain separate, even without
  phone or email, so use the intended student's index.
* Linking or replacement selects the student and shows the guardian's current name, phone and email.
  The command box keeps focus. Changes save automatically and survive edits, reordering and restarts.
* Removal keeps both contacts and other students' links. The filter remains applied: a visible student is selected
  and shows `Guardian: None`; after `children g/INDEX`, the student disappears and selection clears.
  Use `list` or `list st/all` to return to contacts. An absent link reports `NAME has no guardian linked.`

Examples, assuming Alex Tan is student `1`, Mei Tan is guardian `2`, and Sarah Lim is guardian `3`:

* `link s/1 g/2` shows `Linked student Alex Tan to guardian Mei Tan.`
* `link g/2 s/1` has the same effect; an existing link reports `Alex Tan is already linked to Mei Tan.`
* `link s/1 g/3` replaces Mei Tan and shows `Changed Alex Tan’s guardian from Mei Tan to Sarah Lim.`
* `link s/1 g/NONE` shows `Removed guardian link from Alex Tan.` The removal survives restarting.

Common errors:

| Input problem | Result |
|---|---|
| Missing parameter or extra text | `Invalid link command. Use: link s/STUDENT_INDEX g/GUARDIAN_INDEX or link s/STUDENT_INDEX g/NONE` |
| Unknown parameter | `Unknown parameter. Use: link s/STUDENT_INDEX g/GUARDIAN_INDEX or link s/STUDENT_INDEX g/NONE` |
| Repeated parameter | `Each of s/ and g/ may appear only once.` |
| Invalid student index | `Student index must be a positive integer shown in the list.` |
| Invalid guardian value | `Guardian value must be a positive integer shown in the list, or NONE to remove the guardian link.` |
| Student index outside the displayed list | `No contact exists at student index INDEX.` |
| Guardian index outside the displayed list | `No contact exists at guardian index INDEX.` |
| Wrong student role | `Student index must refer to a student.` |
| Wrong guardian role | `Guardian index must refer to a guardian.` |

Failed link commands leave contacts, relationships, selection, the detail panel and saved data unchanged.

![A linked student selected with the guardian's contact details displayed](images/StudentGuardianLink.png)

### Removing a guardian link: `unlink`

Removes a student's guardian link while keeping both contacts.

Format: `unlink s/STUDENT_INDEX`

* Use the linked student's **currently displayed index**; the guardian can be hidden.
  Active and former students are supported (`list st/former` shows former students).
  Names, contact IDs, tuition details and other students' links stay unchanged.
* Success saves the removal and shows guardian `None`. The filter remains: if the student no longer matches
  (for example, after `children`), they disappear and selection clears; otherwise they are selected.
* Successful `unlink` clears deletion undo and cannot itself be undone with `undo`. Use `link` to restore the link.
* Supply lowercase `s/` once with a positive whole-number index; leading zeroes are accepted.
  Missing/repeated parameters, other parameters and extra text are rejected with the command format.
* An unlinked student reports `NAME has no guardian link to remove.` A guardian target reports
  `Student index must refer to a student.` An out-of-range index reports
  `No contact exists at student index INDEX.` Failed commands and failed saves leave data unchanged.

Example: for linked student Alex Tan at index 1, `unlink s/1` reports
`Removed the guardian link from Alex Tan. Both contacts were kept.`

### Finding a guardian's students: `children`

Shows a guardian's linked students, including former students.

Format: `children g/GUARDIAN_INDEX`

* Run `list r/guardian` or otherwise display the guardian, then use their **current index**.
* Results replace the previous filter with that guardian's active and former students, in the usual alphabetical
  order with former badges. Same-name guardians remain separate because lookup uses permanent identity.
* Contacts, saved data and deletion undo are unchanged.
* Supply lowercase `g/` once with a positive whole-number index; leading zeroes are accepted.
  Missing/repeated parameters, other parameters and extra text are rejected with the command format.
* A student target reports `Guardian index must refer to a guardian.` An out-of-range index reports
  `No contact exists at guardian index INDEX.` These errors leave the view unchanged.
* No linked students produces an empty list and `No students are linked to NAME. Use list to return to contacts.`
* Return with `list` (guardians and active students) or `list st/all` (all contacts).
  Display guardians again before another lookup and use their new indexes.

Example: after `list r/guardian`, if Mei Tan is guardian 1 with two linked students, `children g/1` reports
`Listed 2 students linked to Mei Tan, including former students.` For one match, it uses `1 student`.

### Deleting contacts: `delete`

Format: `delete INDEX [INDEX...]` or `delete n/NAME`

Deletes contacts from the **currently displayed list**. Use `list`, `find NAME` or `list st/former` first to
display your targets.

* **By index:** supply unique positive integers in any order. All refer to the original displayed list;
  earlier deletions do not shift later targets. If any index is invalid, repeated or out of range, nothing is deleted.
* **By name:** supply one non-empty full name, without index targets. Matching ignores capitalization and repeated
  or surrounding spaces; accents, punctuation and word order must match. Partial names do not match. Digits after
  `n/` belong to the name, so `delete n/Alex 2` searches for `Alex 2`.
* If several displayed contacts match a name, the error lists their indexes and roles. Use an index to choose one;
  no contact is deleted automatically.

| Input problem | Error |
| --- | --- |
| Missing target | `Invalid delete command. Use: delete INDEX [INDEX...] or delete n/NAME` |
| Invalid index | `The contact index must be a positive integer shown in the list.` |
| Repeated index | `Each contact index may appear only once.` |
| Index out of range | `No contact exists at index INDEX.` |
| No matching name | `No displayed contact matches name: NAME.` |

Deleting a linked student keeps the guardian. Deleting a guardian keeps all their students, including hidden former
students, and removes their links to that guardian. Unlinked students show `Guardian: None`. Each affected link is
counted once, even when both endpoints are deleted.

Success feedback names each deleted contact and reports removed links, for example:
`Deleted contact: Alex Tan [Student]. Removed 1 student-guardian relationship.` Both formats save automatically;
failed commands or saves leave contacts and saved data unchanged. Use [`undo`](#undoing-the-latest-deletion-undo)
to restore the latest successful deletion.

Examples:

* `list`, then `delete 1 3 5`: delete three displayed contacts together.
* `delete n/  alice   tan`: delete the unique displayed contact named `Alice Tan`.
* `list st/former`, then `delete 1`: delete the first displayed former student.

### Undoing the latest deletion: `undo`

Format: `undo`

Restores the latest single, bulk or name-based deletion with its original IDs, details and guardian links.
The previous filter returns, and a restored contact visible in that view is selected.

* Feedback lists restored names and roles in display order under `Restored 1 contact:` or `Restored N contacts:`.
  Restored links include hidden students' links and are counted once per student.
* Read-only commands and failed commands or saves preserve undo. A failed undo changes nothing and can be retried.
* Another deletion replaces the undo opportunity; any other successful data-changing command clears it.
* **Session-only, one use, no redo:** restarting or successfully undoing clears the history. Confirmed `clear`
  cannot be undone.
* Extra arguments are rejected. With no available history, the error is `No deletion is available to undo.`

Example: `delete 1 3`, followed by `undo`, restores both deleted contacts and their original relationships.

### Reviewing contact data: `check`

Format: `check [guardians|details|duplicates]`

Reviews **displayed contacts only**, preserving data, the current view and deletion undo. Run `list`, `find NAME`
or `list st/former` first to choose the contacts.

| Command | Reports |
| --- | --- |
| `check` | All three sections below |
| `check guardians` | Students without a linked guardian; hidden guardians still count as linked |
| `check details` | Missing student level/subject, or contacts with neither phone nor email |
| `check duplicates` | Possible duplicate pairs, using the [Add warning rules](#duplicate-contacts-and-warnings) |

Results give the displayed contact count and use current indexes, marking former students. **Suggestions are not
proof of incorrect data:** inspect contacts before editing, linking or deleting them. Duplicate checks compare only
displayed pairs; guardians are not expected to have level or subject details.

Section names ignore case. Unknown or multiple sections give `Use check [guardians|details|duplicates].`
An empty view reports that no displayed contacts can be checked.

Example: run `find Tan`, then `check duplicates` to review matching displayed pairs.

### Clearing all entries: `clear`

Format: `clear confirm`

**Permanently deletes every contact and guardian link, including hidden and former students. This cannot be undone.**
Back up your data first if you may need it again.

The exact confirmation word is required, ignoring case (`clear CONFIRM` also works). Bare `clear`, `clear 1` and
`clear confirm 1` are rejected with a warning and the correct format, preserving contacts, the view, saved data
and existing deletion undo.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Successful `add`, `edit`, `delete`, `clear`, `link`, `unlink`, `undo`, `archive` and `unarchive` commands save automatically.
Read-only commands do not save.

A failed save reports `Changes could not be saved. No changes were made.` Contacts, relationships and previously
saved data remain unchanged.

### Editing the data file

Data is stored in `[JAR file location]/data/addressbook.json`; preferences in `[JAR file location]/preferences.json`.
Both paths are relative to the JAR's folder, regardless of the terminal's working directory.
Advanced users can edit the JSON data file directly.

To migrate files from an older version's working directory, close TutorRoster and back up both files.
Copy `data/addressbook.json` into the `data` subfolder beside the JAR and `preferences.json` beside the JAR,
checking for existing destination files before replacing them. Restart afterwards.

<box type="warning" seamless>

**Caution:**
An invalid data file is preserved. TutorRoster starts with an empty list and disables data-changing commands
for the session, showing `Data changes are disabled because the data file could not be loaded. Fix the file and restart TutorRoster.`
followed by `Reason:` and the loading or validation problem (for example, `Only a student can have a level or subject.`).
Blocked commands repeat this explanation. Back up the file, fix the reported problem and restart.<br>
Empty or whitespace-only files are also preserved and identified. Restore a backup if contacts were expected.
For an intentionally empty list, close TutorRoster, delete the empty file and restart; a missing file starts normally.<br>
Punctuation-only names such as `...` are invalid. Close TutorRoster, back up the file and give each affected `name`
at least one letter or digit, then restart. Preserve contact IDs and guardian references; the app does not rename or remove contacts automatically.<br>
Keep permanent `id` values unique and unchanged when editing details. A student's optional `guardianId` must refer
to an existing guardian's `id`. Invalid IDs, repeated JSON fields, duplicate contacts or invalid guardian references
invalidate the file. Older valid files without IDs receive them on loading and include them in the next successful save.
</box>

--------------------------------------------------------------------------------------------------------------------

**Multiple copies and external changes:** If another copy changes the saved data or is saving when this session
tries to save, the command reports `Contact data changed or is being saved by another session. No changes were saved. Close other copies and restart TutorRoster to load the latest data.`
The rejection preserves this session's contacts and the saved file. Close other copies, restart to load the latest
contacts and repeat the command. Read-only commands remain available.

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Transfer your saved contact-data file as follows:

1. Close TutorRoster on both computers before copying any files.
2. Back up `data/addressbook.json` from your existing TutorRoster home folder. If the destination computer already
   has TutorRoster data, back up its `data/addressbook.json` too, because copying will replace those contacts.
3. Download the JAR to your chosen TutorRoster home folder on the destination computer. Create a `data` subfolder
   there if it does not exist. A fresh installation does not create `addressbook.json` until a successful
   data-changing command, so you do not need to add a dummy contact first.
4. Copy the source `addressbook.json` into that destination `data` subfolder, keeping its filename. Copy the whole
   file without changing contact IDs or guardian IDs, so that guardian relationships are retained.
5. Open a terminal in the destination TutorRoster home folder and run `java -jar` with the JAR's file name in quotes, as in
   [Quick start](#quick-start). Launching from
   this folder ensures the relative data path refers to the file you copied. Check the contacts and guardian links;
   use `list st/former` to check former students as well.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

1. **Mac fullscreen dialogs:** Using fullscreen mode for secondary windows, such as the Help window, may cause
   unexpected behavior. Leave fullscreen mode before opening the Help window.

1. **Linux with Wayland:** If launch fails with a `Gdk-CRITICAL` error, try running
   `GDK_BACKEND=x11 java -jar "tutorroster.jar"` from the folder containing the JAR file, replacing
   `tutorroster.jar` with your JAR's file name if it differs.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/ROLE n/NAME [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]` <br> e.g., `add r/student n/James Ho p/22224444 e/jamesho@example.com lv/Sec 3 sub/Math`
**Clear**  | `clear confirm` (permanent; cannot be undone)
**Check data** | `check [guardians\|details\|duplicates]`<br> e.g., `check`, `check duplicates`
**Delete** | `delete INDEX [INDEX...]` or `delete n/NAME`<br> e.g., `delete 3`, `delete 1 3 5`, `delete n/Alice Tan`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find NAME`<br> e.g., `find alex`
**Unlink** | `unlink s/STUDENT_INDEX`<br> e.g., `unlink s/1`
**Guardian's students** | `children g/GUARDIAN_INDEX`<br> e.g., `children g/1`
**Link**   | `link s/STUDENT_INDEX g/GUARDIAN_INDEX` or `link s/STUDENT_INDEX g/NONE`<br> e.g., `link s/1 g/2`
**List**   | `list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]`<br> e.g., `list r/student`, `list st/former`, `list st/all`, `list lv/Sec 3 sub/A-Math`
**Archive** | `archive INDEX`<br> e.g., `archive 2`
**Unarchive** | `unarchive INDEX`<br> e.g., `unarchive 1`
**Undo deletion** | `undo`<br> Restores the latest successful deletion in the current session.
**View**   | `view INDEX`<br> e.g., `view 2`
**Help**   | `help`
