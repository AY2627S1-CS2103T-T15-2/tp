---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list, sorted by name and then role. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### Editing contacts

`edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]` changes the name, phone number or email address of a displayed contact.
The role cannot be edited. The sequence diagram below shows `edit 1 p/NONE`, which removes the phone number of the
first displayed contact `p`. Because edit changes data, `LogicManager` runs it on a candidate copy `c` of the live
model `m`, saves `c` through `Storage`, and only then copies `c`'s data into `m`. If the save fails, `m` is never
touched.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions inside the Logic component for the `edit 1 p/NONE` command" />

**Parsing.** `EditCommandParser` tokenizes the arguments with `ArgumentTokenizer#tokenizeWithQuotedValue`, so a
quoted name such as `n/"Randy p/e ratio"` keeps any prefix-like text inside the quotes. It then checks, in order:

1. No unknown parameter. Any word ending in `/` outside a quoted name, other than `n/`, `p/` or `e/`, is rejected
   with `Unknown parameter`. This is why an unquoted name such as `Ravi s/o Kumar` must be quoted.
2. No repeated parameter. Each of `n/`, `p/` and `e/` may appear once.
3. A valid index. A missing index, or extra text before the first parameter, gives `Invalid edit command`. A value
   that is not a positive integer gives `The contact index must be a positive integer shown in the list.`

Each supplied value is stored in an `EditPersonDescriptor`. `NONE` in any letter case is stored as a removal
(`removePhone()` or `removeEmail()`) rather than a value. Setting a value after a removal clears the removal flag.
An empty descriptor is rejected with `At least one of n/, p/, or e/ must be provided.`

**Execution.** `EditCommand#execute` resolves the index against the filtered, sorted display list, so the index the
user sees is the one edited. An index beyond the list gives `No contact exists at index INDEX.` The edited contact is
built by `createEditedPerson`: each field takes the descriptor's value, is emptied if marked for removal, or keeps the
current value otherwise. The contact's ID, role and guardian link are always copied, so relationships survive an
edit.

The edit is then rejected if it would leave a guardian with no phone number and no email, or if the edited contact
would duplicate another contact. The duplicate check is skipped when the edited contact still matches its original
duplicate identity, so changing only the capitalization of a name is allowed.

On success, `Model#setPerson` replaces the contact and the full list is shown again. The `CommandResult` carries the
edited contact's ID, and `MainWindow` selects that contact so the detail panel shows the update. Like other
data-changing commands, `LogicManager` runs the edit on a candidate model and saves it before replacing the live
model, so a failed save leaves the contact unchanged.

**Design considerations.**

* **Removing a field:** `p/NONE` and `e/NONE` were chosen over an empty `p/` or `e/`, which is rejected as a likely
  typo. `NONE` cannot be a valid phone number or email, so it never clashes with real data. Letter case is ignored as
  a deliberate choice for ease of typing.
* **Rejecting unknown parameters:** silently keeping `a/15` as part of a name would save data the user did not intend.
  The cost is that names containing a word ending in `/` must be quoted.

### Student-guardian linking

`LinkCommandParser` accepts `link s/STUDENT_INDEX g/GUARDIAN_INDEX` in either parameter order. It rejects missing, repeated and unknown prefixes, extra text and invalid integer indexes. `LinkCommand` resolves both indexes against the filtered, sorted display list, validates their roles, rejects duplicate links and replaces an existing guardian when requested.

Every `Person` has an immutable `ContactId`, represented by a UUID. A student has an optional `guardianId` referencing a guardian's ID. This gives each student at most one guardian while allowing several students to refer to the same guardian. References use IDs because names can change and display indexes change after sorting or filtering. Contact-field duplicate detection continues to use `isSamePerson`; an ID does not allow duplicate contact details. `Person.equals` includes the guardian relationship as contact data.

`EditCommand` preserves both IDs when constructing the edited person. `AddressBook.removePerson` clears references to a removed guardian. The detail panel resolves the guardian against the complete contact list, including contacts outside the current search results, and refreshes when either contact changes. The command result carries the student's ID so the UI can select that contact after a successful save.

### Contact details and Add validation

`AddCommandParser` requires `r/ROLE` and `n/NAME` and parses `p/PHONE` and `e/EMAIL` as optional fields. It validates
supplied values in the fixed order role, name, phone, then email, regardless of the order in which parameters appear.
Missing required fields and invalid roles are also reported in that order. A student may omit both contact methods. The
parser rejects a guardian if both are absent to provide a user-facing command error, and `EditCommand` applies the same
rule to the resulting contact before updating the model. `Person` stores
phone and email as optional values, but its constructor also enforces that a guardian has at least one contact
method. This model invariant applies regardless of which code path creates a `Person`.

The Add parser rejects unknown parameters with `Invalid parameter. Valid parameters are r/ROLE, n/NAME, p/PHONE, and
e/EMAIL.` If the unknown parameter occurs while parsing a name, the message also instructs the user to quote the full
name. For Add, every name containing `/` must be enclosed in double quotes, including an embedded slash such as
`AC/DC`. Edit retains its separate behavior, which accepts unquoted embedded slashes. Missing `r/ROLE` or `n/NAME`
errors identify the missing field and append the add command usage. Repeated parameters return
`Each of r/, n/, p/, and e/ may appear only once.` Empty roles receive a separate error from unsupported roles; an
unsupported role reports `Invalid role. Role must be student or guardian.` When multiple supplied values are invalid,
the parser reports the first error in the fixed order role, name, phone, then email.

`AddCommand` rejects an exact duplicate using `Model#hasPerson`, which delegates to `Person#isSamePerson`. Duplicate
identity consists of role, name, phone and email. Name and email comparisons ignore capitalization; phone numbers are
compared after trimming leading and trailing whitespace, while internal spaces and hyphens remain significant. Two
absent values for an optional field match, provided both contacts have at least one contact method. Exact duplicates
are rejected with `This contact already exists.` This gives students with neither a phone number nor an email address
a specific exception: matching role and name alone does not make them exact duplicates. They may both be added, with a
general possible-duplicate warning. For other contacts, matching role and name with different details is allowed. Add
checks possible duplicates in this order: a phone number that matches after spaces and hyphens are ignored; an email
address that matches case-insensitively; then a contact method that is absent from both contacts. It displays only the
first matching warning, appends it after the normal success message, and still adds the contact. Contacts with
different roles or names do not receive these warnings.

### Saving contacts and relationships

Contact JSON records contain an `id` UUID string and an optional `guardianId` UUID string, alongside the existing fields. `JsonAdaptedPerson` restores saved IDs; legacy records without IDs receive new IDs that are persisted on the next successful save. `JsonSerializableAddressBook` first loads all contacts, rejecting duplicate IDs and duplicate contact details, then validates that every guardian reference points to an existing contact with the guardian role. A guardian cannot carry a guardian reference. Repeated JSON object fields are rejected during parsing.

`Person` stores phone and email as non-null `Optional` values. `Phone` and `Email` always represent valid, present
values. Phone details contain at least 3 digits and no more than 100 characters. The JSON adapter keeps both fields as
nullable strings: missing or null means absent, while empty and invalid
strings are rejected. Existing valid strings retain their capitalization. Single-label email domains and domains
whose final label has fewer than two characters are invalid; incompatible files fail loading without being rewritten
automatically. `JsonAdaptedPerson` rejects a guardian record that has neither a phone number nor an email address
while converting it to a model object. The `Person` constructor enforces the same invariant, so saved data cannot
bypass the guardian contact-method requirement.

Duplicate identity is Role + Name + Phone + Email. `Name.isSameName` and `Email.isSameEmail` ignore capitalization;
two absent values for optional fields match when each contact has at least one contact method. A contactless student
is not considered an exact duplicate of another student based on role and name alone. Full object equality remains
case-sensitive so capitalization-only edits remain observable. Existing same-role records differing only in
capitalization may therefore fail duplicate validation on loading. IDs and guardian links remain independent of
duplicate identity.

Each command declares whether it changes data through `Command.isDataChanging()`. `LogicManager` executes data-changing commands on a candidate model carrying the original filter. It saves that candidate before replacing the live model. An unsuccessful command or save therefore cannot change the live contacts, relationships or displayed list. `JsonAddressBookStorage` writes a temporary file in the destination directory and atomically replaces the data file; it fails safely if atomic replacement is unavailable. Read-only commands do not save data.

If loading fails, storage records that failure for the session. The UI displays the startup error, and all data-changing commands are disabled until the user fixes the file and restarts the application. The invalid file is never overwritten during that session. A missing data file starts with an empty list and is created by the first successful data-changing command.

Relevant tests cover Add parsing and validation, repeated parameters, fixed validation-error ordering, quoted names
containing slashes, the phone length limit, optional student contact methods, guardians with either one contact method, guardian contact-method
requirements, exact duplicates and possible-duplicate warning priority, displayed indexes, role validation, first links, replacements, shared guardians, ID preservation,
relationship persistence, deletion cleanup, invalid references, failed saves and UI selection/details. JavaFX UI tests
run on Windows desktop environments; the remaining tests run on all supported platforms.

### Listing and finding contacts

`list` and `find` change which contacts the list panel shows by updating the predicate of the model's filtered list.
Neither command changes any contact data.

**Listing.** `AddressBookParser` passes any text after `list` to `ListCommandParser`, which rejects non-blank text
with `Invalid list command. Use: list`. `ListCommand` resets the filter to `PREDICATE_SHOW_ALL_PERSONS` and reports
the number of contacts, for example `Listed 6 contacts`. `Messages#formatContactCount` uses the singular `contact`
when there is exactly one contact. If there are no contacts, `ListCommand` reports `No contacts found.` instead.

**Finding.** `FindCommandParser` treats all the text after `find` as a single search term. It removes leading and
trailing whitespace, reduces each run of internal whitespace to one space, and rejects a term that is then empty with
`Invalid find command. Use: find NAME`. The term is wrapped in a `NameContainsSubstringPredicate`, which matches a
contact whose name contains the term, ignoring letter case. Consecutive spaces in the name are also treated as one
space. `FindCommand` applies the predicate with `Model#updateFilteredPersonList` and reports either
`Found X contacts matching: NAME.` or `No contacts found matching: NAME`.

The term is matched as a whole, so word order matters: `find tan alex` does not match `Alex Tan`. Matching only the
name keeps results predictable for the tutor, who searches for a person rather than for a phone number or email.

The sequence diagram below shows how `find ale` is executed.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Interactions for the `find ale` command" />

### Ordering and displaying contacts

**Ordering.** `ModelManager` wraps its `FilteredList` in a `SortedList` that uses `PersonDisplayOrderComparator`, and
`Model#getFilteredPersonList` returns this sorted view. The comparator orders contacts by name, ignoring letter case
and treating consecutive spaces as one space. Contacts with the same name are ordered by role, with guardians before
students. The specification does not order contacts with the same name and role, so the comparator treats them as
equal.

Commands that take an index, such as `edit`, `delete` and `link`, look it up in `Model#getFilteredPersonList`, so
the index always refers to the order shown on screen. The order is kept after `list`, `find`, `add` and
`edit`, and when the application starts, without any command having to sort the list itself.

**List cards.** `PersonCard` shows each contact as `NAME [ROLE]` followed by `Phone:` and `Email:` lines.
`ContactDisplayFormatter` formats these values, showing `Not provided` for a missing phone number or email address.
A student's card also shows `Guardian: NAME`, or `Guardian: None` if the student has no linked guardian.

`PersonListPanel` receives both the displayed list and the complete contact list. It looks up a student's guardian in
the complete list, so the guardian is still named when `find` hides the guardian from the displayed list. The panel
redraws its cards whenever any contact changes, so renaming a guardian also updates the cards of the guardian's
students.

#### Design considerations:

**Aspect: Where contacts are sorted**

* **Alternative 1 (current choice):** Sort in `ModelManager`, as a sorted view of the filtered list.
  * Pros: Every command and the UI see the same order, so displayed indexes always match the indexes commands use.
  * Cons: The order is fixed for all views of the list.
* **Alternative 2:** Sort in each command or in the UI.
  * Pros: Different views could use different orders.
  * Cons: Each command must remember to sort, and the UI order could differ from the indexes commands use.

**Aspect: How `find` matches names**

* **Alternative 1 (current choice):** Match the whole search term as part of the name, ignoring case.
  * Pros: Tutors can find a contact from part of a name, as required by the MVP specification.
  * Cons: Word order matters, so `find tan alex` does not match `Alex Tan`.
* **Alternative 2:** Match any of several whole-word keywords, as in AB3.
  * Pros: Word order does not matter.
  * Cons: Partial names such as `ale` do not match, and searching for several words returns many unrelated contacts.

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:
* are independent private tutors personally managing contact information for a small group of secondary school or junior college students and their guardians
* need to retrieve and update student and guardian details quickly
* need to record tutoring context and relationships between students and guardians
* prefer using a desktop application that stores their contacts locally on a single device
* can type quickly and prefer keyboard-driven commands to mouse interactions

**Value proposition**: TutorRoster helps independent private tutors keep student and guardian contact information organised and up to date, allowing them to retrieve accurate details quickly when communicating with students and families.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                                    | I want to …                                                       | So that I can …                                                              |
|----------|-----------------------------------------------------------|-------------------------------------------------------------------|------------------------------------------------------------------------------|
| `* * *`  | private tutor taking on a new student                     | add a student contact                                             | store the student's contact details                                          |
| `* * *`  | private tutor                                             | add a guardian contact                                            | know whom to contact when necessary                                          |
| `* *`    | private tutor who communicates through different channels | record multiple contact methods for a student or guardian         | have alternative ways to contact them                                        |
| `* *`    | private tutor                                             | add notes to a contact                                            | record useful information that does not fit into the standard contact fields |
| `* *`    | private tutor teaching different levels and subjects      | record concise tutoring context such as education level or subject | distinguish students quickly                                                 |
| `* * *`  | private tutor                                             | record the relationship between a student and guardian            | know which contacts are related                                              |
| `* *`    | private tutor                                             | associate multiple guardians with a student                       | keep track of all relevant contacts for that student                         |
| `* *`    | private tutor                                             | designate a student's primary guardian                            | know whom to contact first when necessary                                    |
| `* *`    | private tutor                                             | unlink an incorrect or outdated student-guardian association      | avoid confusion caused by inaccurate relationships                           |
| `* * *`  | private tutor                                             | view the guardian linked to a student                             | identify whom to contact without searching separately                        |
| `* *`    | private tutor teaching siblings                           | view all students linked to a guardian                            | manage families with multiple students efficiently                           |
| `* *`    | private tutor                                             | search for a contact by name                                      | find the person quickly                                                      |
| `* *`    | busy private tutor                                        | filter contacts using tutoring-related information                | retrieve the relevant contacts efficiently                                   |
| `* * *`  | private tutor                                             | view a student's complete contact record                          | retrieve the information needed for communication                            |
| `* * *`  | private tutor                                             | view a guardian's complete contact record                         | retrieve the information needed to contact the appropriate adult             |
| `* *`    | private tutor with contacts who have identical names      | view identifying information for contacts with identical names    | select the correct person                                                    |
| `* *`    | private tutor                                             | update outdated contact information                               | keep my records accurate                                                     |
| `* *`    | private tutor                                             | mark a student as active or former                                | prevent old records from cluttering my daily use                             |
| `*`      | cautious private tutor                                    | receive warnings about potential duplicate contacts               | avoid maintaining multiple records of the same person                        |
| `* *`    | private tutor                                             | see which required contact information is missing                 | keep my records complete                                                     |
| `* * *`  | private tutor                                             | delete a contact that was added by mistake                        | remove incorrect records from my contact list                                |
| `* *`    | beginner private tutor                                    | receive feedback explaining why a command is invalid              | correct the command quickly                                                  |
| `*`      | private tutor                                             | undo my most recent change                                        | recover from an accidental modification                                      |
| `* * *`  | private tutor                                             | view all stored contacts                                          | review the people in my contact list                                         |
| `* * *`  | private tutor                                             | identify whether a contact is a student or guardian               | understand the person's role                                                 |
| `* *`    | private tutor                                             | search using part of a contact's name                             | find the person when I do not remember the full name                         |
| `* *`    | private tutor                                             | mark a former student as active again                             | resume managing that contact if the student returns                          |
| `* * *`  | private tutor                                             | retain my contact records after closing and reopening TutorRoster | avoid entering the information again                                         |
| `* *`    | private tutor                                             | filter contacts by whether they are students or guardians         | focus on the relevant type of contact                                        |
| `* *`    | beginner private tutor                                    | view the available commands and their usage                       | learn how to manage contacts                                                 |

### Use cases

For all use cases below, the **System** is `TutorRoster` and the **Actor** is the `Tutor`, unless specified otherwise

#### UC01: Link a student to a guardian

**Preconditions:** The student and guardian contacts already exist.

**MSS**

1. Tutor requests to list all contacts.
2. TutorRoster displays the contacts and their indexes.
3. Tutor requests to link a student to a guardian using their displayed indexes.
4. TutorRoster saves the relationship and displays the student's details together with the guardian's contact information.

   Use case ends.

**Extensions**

* **3a. An index is invalid or does not refer to a contact in the displayed list.**

  3a1. TutorRoster displays an error and leaves existing relationships unchanged.

  Use case resumes at step 3.

* **3b. The selected student contact is not a student, or the selected guardian contact is not a guardian.**

  3b1. TutorRoster displays an error and leaves existing relationships unchanged.

  Use case resumes at step 3.

* **3c. The student is already linked to the specified guardian.**

  3c1. TutorRoster informs the tutor that the relationship already exists and makes no changes.

  Use case ends.

* **3d. The student is linked to a different guardian.**

  3d1. TutorRoster replaces the existing relationship with the requested guardian, saves the change, and displays the updated guardian information.

  Use case ends.

* **4a. TutorRoster cannot save the relationship, including a replacement relationship in extension 3d.**

  4a1. TutorRoster displays an error. Existing contacts, relationships, and saved data remain unchanged.

  Use case ends.

#### UC02: Update a contact's details

**MSS**

1. Tutor requests to find a contact by name.
2. TutorRoster displays matching contacts and their indexes.
3. Tutor requests to edit a contact using its displayed index and supplies the new name, phone number, or email address.
4. TutorRoster saves the changes and displays the updated contact. Unspecified fields and existing student-guardian relationships remain unchanged.

   Use case ends.

**Extensions**

* **1a. The search request contains no name or only whitespace.**

  1a1. TutorRoster displays an error.

  Use case resumes at step 1.

* **2a. No contacts match the supplied name.**

  2a1. TutorRoster informs the tutor that no matching contacts were found.

  Use case ends.

* **3a. The index is invalid or does not refer to a contact in the displayed list.**

  3a1. TutorRoster displays an error and makes no changes.

  Use case resumes at step 3.

* **3b. No editable fields are supplied, or the supplied details are invalid.**

  3b1. TutorRoster displays an error and leaves the contact unchanged.

  Use case resumes at step 3.

* **3c. The requested changes would remove a guardian's last contact method.**

  3c1. TutorRoster explains that a guardian must have a phone number or email address and leaves the contact unchanged.

  Use case resumes at step 3.

* **3d. The updated contact would duplicate another existing contact.**

  3d1. TutorRoster reports the duplicate and leaves the contact unchanged.

  Use case resumes at step 3.

* **4a. TutorRoster cannot save the changes.**

  4a1. TutorRoster displays an error. Existing contact details, relationships, and saved data remain unchanged.

  Use case ends.

#### UC03: Delete a contact

**MSS**

1. Tutor requests to list all contacts.
2. TutorRoster displays the contacts and their indexes.
3. Tutor requests to delete a contact using its displayed index.
4. TutorRoster deletes the contact and removes any student-guardian relationships involving it. Other contacts are retained.
5. TutorRoster saves the changes, refreshes the contact list and its indexes, and reports the deletion and any relationships removed.

   Use case ends.

**Extensions**

* **2a. The contact list is empty.**

  2a1. TutorRoster informs the tutor that no contacts were found.

  Use case ends.

* **3a. The index is invalid or does not refer to a contact in the displayed list.**

  3a1. TutorRoster displays an error. No contact or relationship is deleted.

  Use case resumes at step 3.

* **4a. The deleted contact is a student linked to a guardian.**

  4a1. TutorRoster removes the student's relationship with the guardian. The guardian contact is retained.

  Use case resumes at step 5.

* **4b. The deleted contact is a guardian linked to one or more students.**

  4b1. TutorRoster removes all relationships involving that guardian. The student contacts are retained and show that they have no linked guardian.

  Use case resumes at step 5.

* **5a. TutorRoster cannot save the deletion.**

  5a1. TutorRoster reports that the deletion failed. The contact and its relationships are retained, and the displayed list and previously saved data remain unchanged.

  Use case ends.

### Non-Functional Requirements

**Environment and portability**

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should provide all features without an internet connection.
3.  Should be distributable as a single JAR file of at most 100MB that runs without an installer.
4.  Should not depend on a remote server or an external database management system.

**Performance**

The performance requirements below assume a computer with at least 4GB of RAM and a dual-core processor released within the last five years.

5.  Should be able to store up to 500 contacts.
6.  Should display the result of any command within 1 second when storing 500 contacts.
7.  Should display the contact list within 5 seconds of launching the JAR file when storing 500 contacts.

**Usability**

8.  Should allow every feature to be used with the keyboard alone, without a mouse.
9.  A user who types at least 40 words per minute and is familiar with the commands should be able to add a contact with a name, phone number, and email address within 20 seconds.
10. Should display all user interface elements without overlapping or being cut off on screen resolutions of 1920x1080 and higher at screen scales of 100% and 125%.
11. Should keep every feature accessible on screen resolutions of 1280x720 and higher at a screen scale of 150%, although some panels may need scrolling.

**Reliability and data**

12. Should retain the changes made by a successful command after the application is terminated unexpectedly, such as when its process is killed or the computer loses power.
13. Should store data locally in a plain-text JSON file that can be read and edited in a standard text editor.
14. Should be able to load a data file copied from another computer, including one running a different _mainstream OS_.

**Privacy and scope**

15. Should not make any network connections while running.
16. Should serve a single user on a single device, and is not required to support concurrent access by multiple users or synchronisation across devices.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Contact**: A person record in TutorRoster that represents either a student or a guardian
* **Role**: Whether a contact is a student or a guardian. This decides which rules apply to it
* **Contact method**: Either a phone number or an email address. Students may omit both; a guardian must have at least
  one
* **Student**: A learner who receives or previously received tuition from the private tutor. A student may have no
  linked guardian
* **Guardian**: An adult responsible for a student and whom the private tutor may contact regarding the student
* **Primary guardian**: The guardian designated as the private tutor's first point of contact for a student
* **Active student**: A student who is currently receiving tuition from the private tutor
* **Former student**: A student who is no longer receiving tuition but whose contact record is retained
* **Tutoring context**: Concise information about a student, such as the student's education level or subject
* **Normalised value**: A value after leading and trailing spaces are removed and repeated spaces inside a name are collapsed to one. This is the form used to store contacts and check for duplicates

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Adding a person

1. Start with an empty address book or use names and details not already in the address book.

1. Test case: `add r/student n/Ben Lim`<br>
   Expected: The student is added with no phone number or email address.

1. Test case: `add r/guardian n/Mei Tan p/91234567`<br>
   Expected: The guardian is added with a phone number and no email address.

1. Test case: `add r/guardian n/Betsy Crowe e/betsycrowe@example.com`<br>
   Expected: The guardian is added with an email address and no phone number.

1. Test case: `add r/guardian n/No Contact`<br>
   Expected: No contact is added. The result says `A guardian must have at least one contact method: phone or email.`

1. Repeat the first command.<br>
   Expected: A second contact is added with the same role and name. The success message is followed by the general
   possible-duplicate warning because both contacts have no phone number or email address.

1. Test case: `add r/student n/Alex Tan p/91234567 e/alex@example.com`, then repeat the command with the same values.<br>
   Expected: The second command is rejected with `This contact already exists.`

1. Test case: `add r/student n/Casey Lee p/9123 4567`, then `add r/student n/Casey Lee p/91234567`.<br>
   Expected: The second contact is added with the phone-related possible-duplicate warning.

1. Test case: `add r/student n/Jamie Ng p/12345678 e/jamie@example.com`, then `add r/student n/Jamie Ng p/87654321 e/JAMIE@example.com`.<br>
   Expected: The second contact is added with the email-related possible-duplicate warning.

1. Test case: `add r/student n/Alex Tan p/12`.<br>
   Expected: The phone error says it must contain at least 3 digits.

1. Test case: `add r/student n/Alex Tan p/`.<br>
   Expected: The result says `Empty phone parameter: Phone cannot be empty when p/ is specified.`

1. Test case: `add r/student n/Alex Tan p/<101-character value>`.<br>
   Expected: The result says `Phone details cannot exceed 100 characters.`

1. Test case: `add r/teacher n/Alex Tan p/12` and `add p/12 n/Alex Tan r/teacher`.<br>
   Expected: Both report the role error first, following the fixed parameter order regardless of command order.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Editing a person

1. Prerequisites: start from the sample data and run `list`. Note which contacts are students and which are
   guardians.

1. Test case: `edit 1 n/Alex Yeoh p/91234567`<br>
   Expected: The first contact's name and phone are updated. The status message shows
   `Updated contact: Alex Yeoh [ROLE].` The edited contact is selected and the detail panel shows the new details.

1. Test case: `edit 1 p/NONE` on a student<br>
   Expected: The phone number is removed and shows `Not provided`.

1. Test case: on a guardian with a phone number and no email, `edit INDEX p/NONE`<br>
   Expected: No change. The status message shows
   `A guardian must have at least one contact method: phone or email.`

1. Test case: `find` a name, then `edit 1 n/New Name`<br>
   Expected: The first search result is edited, and the full contact list is shown again.

1. Test case: `edit 1 n/"Ravi s/o Kumar"`<br>
   Expected: The name becomes `Ravi s/o Kumar`. Without the quotes, the command is rejected with `Unknown parameter`.

1. Incorrect commands to try, each leaving the contacts unchanged:
   * `edit 1` gives `At least one of n/, p/, or e/ must be provided.`
   * `edit n/Alex` gives `Invalid edit command. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`
   * `edit 0 n/Alex` gives `The contact index must be a positive integer shown in the list.`
   * `edit 999 n/Alex` gives `No contact exists at index 999.`
   * `edit 1 r/guardian` gives `Unknown parameter. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL]`
   * `edit 1 n/A n/B` gives `Each of n/, p/, and e/ may appear only once.`
   * Editing a contact so that it matches another contact's role, name, phone and email gives
     `This edit would create a duplicate contact.`

### Listing contacts

1. Prerequisites: start with an empty address book, for example by running `clear`.

1. Test case: `list`<br>
   Expected: No contacts are shown. The status message shows `No contacts found.`

1. Test case: `add r/student n/Alex Yeoh`, then `list`<br>
   Expected: Alex Yeoh is shown. The status message shows `Listed 1 contact`.

1. Test case: `add r/guardian n/Mei Tan p/98765432`, then `list`<br>
   Expected: Both contacts are shown. The status message shows `Listed 2 contacts`.

1. Test case: `list 3`<br>
   Expected: The shown contacts are unchanged. The status message shows `Invalid list command. Use: list`

### Finding contacts

1. Prerequisites: start with an empty address book, for example by running `clear`. Then add three contacts:
   `add r/student n/Alex Yeoh`, `add r/student n/Charlotte Oliveiro` and `add r/guardian n/David Li p/91031282`.

1. Test case: `find ale`<br>
   Expected: Only Alex Yeoh is shown. The status message shows `Found 1 contact matching: ale.`

1. Test case: `find LI`<br>
   Expected: Charlotte Oliveiro and David Li are shown, as the search ignores case and matches part of a name.
   The status message shows `Found 2 contacts matching: LI.`

1. Test case: `find yeoh alex`<br>
   Expected: No contacts are shown, as the words must appear in the same order as in the name. The status message
   shows `No contacts found matching: yeoh alex`

1. Test case: `list` after any of the `find` commands above<br>
   Expected: All three contacts are shown again. The status message shows `Listed 3 contacts`.

1. Test case: `find`<br>
   Expected: The shown contacts are unchanged. The status message shows `Invalid find command. Use: find NAME`

### Ordering and displaying contacts

1. Prerequisites: start with an empty address book, for example by running `clear`. Then add four contacts in this
   order: `add r/guardian n/David Li p/91031282`, `add r/student n/Charlotte Oliveiro`, `add r/student n/Alex Yeoh`
   and `add r/guardian n/Bernice Yu p/99272758`.

1. Test case: `list`<br>
   Expected: The contacts are shown in alphabetical order: Alex Yeoh, Bernice Yu, Charlotte Oliveiro, then David Li.
   Each card shows the name followed by the role, for example `Alex Yeoh [Student]`, and shows `Not provided` for a
   missing phone or email. Student cards also show `Guardian: None`, and guardian cards have no guardian line.

1. Test case: `link s/1 g/2`<br>
   Expected: The status message shows `Linked student Alex Yeoh to guardian Bernice Yu.` Alex Yeoh's card shows
   `Guardian: Bernice Yu`.

1. Test case: `edit 2 n/Bernice Tan`<br>
   Expected: Alex Yeoh's card shows `Guardian: Bernice Tan` straight away, without running another command.

1. Test case: `add r/student n/David Li`<br>
   Expected: The new student is shown directly after the guardian David Li, as guardians come before students with
   the same name.

1. Test case: `add r/guardian n/aaron lim p/81234567`<br>
   Expected: The new guardian `aaron lim` is shown first, as the order ignores capitalisation.

1. Test case: `add r/student n/Maximilian Alexander Christopher Bartholomew Montgomery Fitzgerald`<br>
   Expected: The whole name is visible. If it does not fit on one line, it wraps onto the next line instead of being
   cut off.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
