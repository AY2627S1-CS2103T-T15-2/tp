---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TutorRoster Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* TutorRoster is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) by the
  [SE-EDU initiative](https://se-education.org/). Its code, documentation structure and UML diagrams have been reused
  and adapted for student and guardian contact management. This includes the proposed undo/redo design below.
* [JavaFX](https://openjfx.io/) provides the graphical interface, and
  [Jackson](https://github.com/FasterXML/jackson) handles JSON serialization.
* [JUnit 5](https://junit.org/junit5/) provides the automated testing framework.
* [MarkBind](https://markbind.org/) builds the documentation website, with
  [PlantUML](https://plantuml.com/) rendering the UML diagrams.
* Build and quality tools include [Gradle](https://gradle.org/),
  [Shadow](https://github.com/GradleUp/shadow), [Checkstyle](https://checkstyle.org/) and
  [JaCoCo](https://www.jacoco.org/jacoco/).

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

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550" alt="Partial Logic class diagram showing the shared student status command hierarchy"/>

`XYZCommand` represents the concrete commands listed in the diagram. `ArchiveCommand` and `UnarchiveCommand`
instead extend the abstract `StudentStatusCommand`, which shares their student-status validation and update logic.

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
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

The overview below shows how the Model stores all contacts and exposes a filtered, sorted view for display.
The contact's fields are expanded in the following Person class diagram.

<puml src="diagrams/ModelClassDiagram.puml" alt="Model component: contact storage, preferences and displayed view" />

The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list, sorted by name and then role. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

The following diagram expands `Person`. The multiplicity `1` means a value is required; `0..1` means it is optional.
Associations represent the stored values, including the values wrapped in `Optional` fields in Java.

<puml src="diagrams/PersonClassDiagram.puml" alt="Person fields: required identity, name, role and status; optional guardian ID, phone, email, level and subject" />

Each contact has a stable `ContactId`, a `Name`, a `Role` (`STUDENT` or `GUARDIAN`) and a `StudentStatus`
(`ACTIVE` or `FORMER`). Phone and email are individually optional, although a guardian must have at least one.
Only students can have a level, subject or guardian reference, and only students can be former.

The `id` association identifies the contact itself. The optional `guardianId` stores another contact's ID,
which must resolve to an existing guardian. Each student can reference at most one guardian, and several students
can reference the same guardian. The Person diagram uses ordinary associations because replacement contacts can
share immutable field values.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T15-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

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

### Undoing the latest deletion

The `undo` command restores one successful deletion, including index, bulk and name-based deletion.
`DeletionSnapshot` holds a separate copy of the pre-deletion contact data, the display filter and the deleted IDs.
Restoring the complete data also restores links cleared on surviving students when their guardian was deleted.

The sequence below shows successful restoration and the save-failure path. Parsing and snapshot accessor calls are
simplified to keep the focus on restoring a candidate before saving and publishing it to the live model.

<puml src="diagrams/UndoDeletionSequenceDiagram.puml" alt="Undo deletion restores a candidate and saves before updating live data and clearing history" />

`LogicManager` records history only after deletion saves successfully. Read-only commands and failed operations
retain that history; a later successful deletion replaces it, and another successful data-changing command clears it.
Undo consumes history only after the restored candidate saves successfully. A failed save therefore preserves both
the current live state and the opportunity to retry. History is limited to the current session and does not provide redo.
This deletion-specific implementation is separate from the proposed general undo/redo design above.

### Editing contacts

`edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]` changes the name, phone number, email address,
level or subject of a displayed contact. Only a student can have a level or subject.
The role cannot be edited. The sequence diagram below shows `edit 1 p/NONE`, which removes the phone number of the
first displayed contact `p`. Because edit changes data, `LogicManager` runs it on a candidate copy `c` of the live
model `m`, saves `c` through `Storage`, and only then copies `c`'s data into `m`. If the save fails, `m` is never
touched.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions inside the Logic component for the `edit 1 p/NONE` command" />

**Parsing.** `EditCommandParser` tokenizes the arguments with `ArgumentTokenizer#tokenizeWithQuotedValue`, so a
quoted name such as `n/"Randy p/e ratio"` keeps any prefix-like text inside the quotes. It then checks, in order:

1. No unknown parameter. Any word ending in `/` outside a quoted name, other than `n/`, `p/`, `e/`, `lv/` or `sub/`, is rejected
   with `Unknown parameter`. This is why an unquoted name such as `Ravi s/o Kumar` must be quoted.
2. No repeated parameter. Each of `n/`, `p/`, `e/`, `lv/` and `sub/` may appear once.
3. A valid index. A missing index, or extra text before the first parameter, gives `Invalid edit command`. A value
   that is not a positive integer gives `The contact index must be a positive integer shown in the list.`

Each supplied value is stored in an `EditPersonDescriptor`. `NONE` in any letter case is stored as a removal
(such as `removePhone()` or `removeLevel()`) rather than a value. Setting a value after a removal clears the removal flag.
An empty descriptor is rejected with `At least one of n/, p/, e/, lv/, or sub/ must be provided.`

**Execution.** `EditCommand#execute` resolves the index against the filtered, sorted display list, so the index the
user sees is the one edited. An index beyond the list gives `No contact exists at index INDEX.` The edited contact is
built by `createEditedPerson`: each field takes the descriptor's value, is emptied if marked for removal, or keeps the
current value otherwise. The contact's ID, role, status and guardian link are always copied, so relationships survive an
edit.

The edit is rejected if it sets or removes the level or subject of a guardian. It is then rejected if it would leave
a guardian with no phone number and no email, or if the edited contact
would duplicate another contact. The duplicate check is skipped when the edited contact still matches its original
duplicate identity, so changing only the capitalization of a name is allowed.

If the edit is allowed, `getPossibleDuplicateWarning` compares the edited contact with every other contact, excluding
itself by ID, using the same rules as `add`. A contact with the same role and name that also has a matching phone
number (ignoring spaces and hyphens), the same email address, or a shared missing phone number or email produces a
warning, which is appended to the success message. The edit is still applied, because different people may share a
name.

On success, `Model#setPerson` replaces the contact and the full list is shown again. The `CommandResult` carries the
edited contact's ID, and `MainWindow` selects that contact so the detail panel shows the update. Like other
data-changing commands, `LogicManager` runs the edit on a candidate model and saves it before replacing the live
model, so a failed save leaves the contact unchanged.

**Design considerations.**

* **Removing a field:** `p/NONE` and `e/NONE` were chosen over an empty `p/` or `e/`, which is rejected as a likely
  typo. `NONE` cannot be a valid phone number or email, so it never clashes with real data. Letter case is ignored as
  a deliberate choice for ease of typing.
* **Possible duplicates:** a warning was chosen over rejecting the edit, matching `add`, so that two people with
  the same name can both be recorded. Rejecting would force the tutor to change one name just to save the record.
* **Rejecting unknown parameters:** silently keeping `a/15` as part of a name would save data the user did not intend.
  The cost is that names containing a word ending in `/` must be quoted.

### Marking students as former and active

`archive INDEX` marks a student as `FORMER`; `unarchive INDEX` marks a former student as `ACTIVE` again.
Both commands retain the contact and its guardian relationship. They do not archive or export the data file.

**Parsing and validation.** `ArchiveCommandParser` and `UnarchiveCommandParser` use `ParserUtil#parseIndex` to
accept one positive integer. Invalid input, including a missing index or extra arguments, gives
`The contact index must be a positive integer shown in the list.` Both concrete commands inherit
`StudentStatusCommand#execute`, supplying a target status and command-specific messages. This shared method
resolves the index against `Model#getFilteredPersonList`, which is sorted in displayed order. It rejects an index
beyond that list, a guardian target, or a student who already has the requested status before changing any data.

**Updating the student.** `Person#withStatus` creates a replacement contact with the new status and the original
ID, name, phone, email, role, guardian ID, level and subject. `Model#setPerson` replaces the target by ID, so even
same-name students remain separate records. The result reports the student's name and new status.

The sequence below shows a successful `archive 1` when the first displayed contact is an active student.
Parsing, UI updates and deletion-history maintenance are omitted to focus on command execution and saving.
The Model and Storage components are shown through their public operations.

<puml src="diagrams/ArchiveSequenceDiagram.puml" alt="Archiving a student on a candidate model, saving it, and then updating the live model" />

`unarchive` follows the same sequence through the inherited `execute` method, using `StudentStatus.ACTIVE`
and the messages supplied by `UnarchiveCommand` instead.

**Keeping the current view.** The status commands retain the current filter. Replacing the contact causes the
filtered list to re-evaluate whether the student still matches:

| Current view | After `archive` | After `unarchive` |
| --- | --- | --- |
| `list` or `list st/active` (guardians and active students) | The student disappears; the guardian remains. | Former students are hidden; use `list st/former` or `list st/all` first. |
| `list st/former` | An already-former student is rejected. | The student disappears from the former-student list. |
| `list st/all` | The student remains visible with the `Former` badge. | The student remains visible without the badge. |
| `find NAME` | A matching student remains visible with the `Former` badge. | The student remains visible without the badge. |

If the student was selected, the UI clears the selection and detail panel when the student leaves the view.
When the student remains in the view, as with `find` or `list st/all`, the existing selection is retained
and its details are refreshed. The details panel shows the status as `Role: Student [Former]`.

**Saving and failure handling.** Both commands declare themselves data-changing. `LogicManager` creates a
candidate model containing the live data, preferences and current filter, then executes the command on that copy.
It saves the candidate through `Storage#saveAddressBook` before publishing its data and filter to the live model.
A validation failure or failed save leaves the live contacts, relationships and filter unchanged. A save failure
reports `Changes could not be saved. No changes were made.`; see
[Saving contacts and relationships](#saving-contacts-and-relationships) for the storage mechanism.
Successful status changes clear the previous deletion's undo history, following the rule described under
[Undoing the latest deletion](#undoing-the-latest-deletion).

**Design considerations.**

* Sharing `StudentStatusCommand` keeps index, role and status checks consistent between the two commands.
* Only students can be former: guardians remain contactable even when their linked students stop taking lessons.
  Keeping the student's ID and guardian reference allows the tutor to reactivate the same record later.
* Retaining the filter preserves the tutor's current task. Former students stay retrievable through
  `list st/former` and `list st/all`, and `find` matches names independently of status, so a name search
  does not silently change meaning when a student becomes former.

### Student-guardian linking

The diagram below shows how relationship identity is represented independently of names, display indexes and
student status. Both `id` and `guardianId` use `ContactId`; they are distinct fields with different multiplicities.

<puml src="diagrams/LinkRelationshipClassDiagram.puml" alt="Contact identity and student-guardian references" />

`LinkCommandParser` accepts `link s/STUDENT_INDEX g/GUARDIAN_INDEX` in either parameter order. It rejects missing, repeated and unknown prefixes, extra text and invalid integer indexes. `LinkCommand` resolves both indexes against the filtered, sorted display list, validates their roles, rejects duplicate links and replaces an existing guardian when requested.

Every `Person` has an immutable `ContactId`, represented by a UUID. A student has an optional `guardianId` referencing a guardian's ID. This gives each student at most one guardian while allowing several students to refer to the same guardian. References use IDs because names can change and display indexes change after sorting or filtering. Contact-field duplicate detection continues to use `isSamePerson`, including the exception for students without either contact method described below. `Person.equals` includes the guardian relationship as contact data.

`UniquePersonList` locates update and removal targets by `ContactId`, rather than contact-field equality. Two students
with the same name and no contact methods can have equal fields but must remain separate records. Linking, editing
or deleting the second displayed student must therefore use its ID. Otherwise replacing the first equal contact with
the second contact's updated record would lose the first ID and save the second ID twice, making the file invalid.

`EditCommand` preserves both IDs when constructing the edited person. `AddressBook.removePerson` clears references to a removed guardian. The detail panel resolves the guardian against the complete contact list, including contacts outside the current search results, and refreshes when either contact changes. The command result carries the student's ID so the UI can select that contact after a successful save.

**Interaction with student status and deletion.** Archiving changes a student's status while retaining the student's
ID and guardian reference. A former student can be linked after a command such as `find` makes both targets visible.
The target is still resolved by its displayed index and then updated by ID, so an active student with the same name
remains a separate record. Relinking preserves the student's status, level and subject.

Deleting a guardian clears references from all its students, including former students hidden by the current filter.
Bulk deletion resolves every target before removing any contact, so index shifts cannot retarget later deletions.
Its relationship count counts each affected student once, even when both that student and its guardian are deleted.
These changes run on the candidate model described below; a failed save must not clear live relationships.

### Contact details and Add validation

`AddCommandParser` requires `r/ROLE` and `n/NAME` and parses `p/PHONE`, `e/EMAIL`, `lv/LEVEL` and `sub/SUBJECT` as optional
fields. It validates supplied values in the fixed order role, name, phone, email, level, then subject, regardless of
the order in which parameters appear.
The shared tokenizer recognizes prefixes at the start of the arguments or after `Character.isWhitespace`
characters, including tabs and line breaks, while preserving internal value whitespace and quoted names.
Command dispatch accepts the same separators and preserves multiline arguments for field validation.
Unknown prefixes, repeated prefixes and unclosed quotes are syntax errors reported before field-value validation.
The parser checks for a missing role, validates the role, then checks for a missing name and unexpected preamble
before validating the name. A student may omit both contact methods. The
parser rejects a guardian if both are absent to provide a user-facing command error, and `EditCommand` applies the same
rule to the resulting contact before updating the model. `Person` stores
phone and email as optional values, but its constructor also enforces that a guardian has at least one contact
method. This model invariant applies regardless of which code path creates a `Person`.

`parse` delegates syntax handling to `tokenizeArguments` and field parsing to `parsePerson`. Field-specific helpers
keep the validation sequence visible without mixing it with extraction details. `AddCommand#execute` similarly
separates rejection checks, feedback preparation and model updates. Feedback is prepared before insertion so the
new contact cannot match itself when checking for possible duplicates.

Both the Add scanner and tokenizer enter quoted mode only when the first non-whitespace character after `n/` is
a straight double quote. Quotes in phone text remain literal characters and cannot hide later parameters.
`ParserUtil#parseName` trims surrounding whitespace before and after removing enclosing quotes, including Java
whitespace such as em spaces, tabs and line breaks. Internal tabs and other unsupported whitespace still fail name
validation; only repeated ordinary spaces are collapsed. Keeping these rules consistent lets quotation marks protect
prefix-like name text without changing whitespace validation.

The Add parser identifies the unknown prefix, for example `Invalid parameter: a/. Valid parameters are r/ROLE,
n/NAME, p/PHONE, e/EMAIL, lv/LEVEL, and sub/SUBJECT.` If an unknown parameter follows an unquoted name, it also suggests
quoting the full name. After a quoted name, it reports only the invalid parameter. For Add, every name containing
`/` must be enclosed in double quotes, including an embedded slash such as
`AC/DC`. Edit retains its separate behavior, which accepts unquoted embedded slashes. Missing `r/ROLE` or `n/NAME`
errors identify the missing field and append the add command usage. Repeated parameters return
`Repeated parameter: lv/. Each parameter may appear only once.`, substituting the repeated prefix. If several
prefixes repeat, it reports the first in the fixed field order. Empty roles receive a separate error from unsupported roles; an
unsupported role reports `Invalid role. Role must be student or guardian.`

`Person` also stores an optional `Level` and `Subject` for students. Both are free-text value classes that allow 1 to
30 Unicode code points, allowing letters, digits, spaces, hyphens, periods, plus signs and parentheses, with at least
one letter or digit. `ParserUtil` trims the value, reduces repeated
spaces to one and rejects `NONE`, which `edit` reserves for removing the field. The `Level` and `Subject` constructors
reject blank values with their constraints messages. For command input, `ParserUtil` instead reports the specific
empty-parameter message from `Level.MESSAGE_EMPTY` or `Subject.MESSAGE_EMPTY`. For other invalid values, it uses
`getValidationErrorMessage` to distinguish excessive length, unsupported characters, and the absence of a letter or
digit, in that order. `Level` and `Subject` delegate these common checks to `StudentDetailValidator` and map its
validation result to their own field-specific messages. This keeps their rules aligned while preserving distinct
value types and error messages. The `Person` constructor rejects a
guardian with a level or subject. After validating phone and email, the Add parser checks the guardian contact-method
requirement, then rejects student-only prefixes before parsing either value. `isSameValue` compares
two values ignoring letter case and spaces, so that filters can treat `Sec 3` and `sec3` as the same level.

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

`DuplicateWarning#classify` owns the warning priority shared by Add and Edit and returns `PHONE`, `EMAIL`, `GENERAL`
or `NONE`. Each command supplies its own wording; Add also logs the selected warning. Edit excludes the contact
being edited by ID before calling the classifier. Sharing classification prevents the commands' warning rules from
drifting apart without coupling their success messages or exact-duplicate rejection.

Duplicate and warning checks use the complete saved contact list, including former students, regardless of the
current filter. Level, subject, IDs and guardian links do not contribute to duplicate identity.

After adding the contact, `AddCommand` resets the filter with `PREDICATE_SHOW_ACTIVE_CONTACTS`, showing guardians
and active students while hiding former students. The new contact starts active. The command result carries its ID
so the UI can select it after a successful save, even if an earlier search or list filter hid similar contacts.

### Saving contacts and relationships

Contact JSON records contain an `id` UUID string and an optional `guardianId` UUID string, alongside the existing fields. `JsonAdaptedPerson` restores saved IDs; legacy records without IDs receive new IDs that are persisted on the next successful save. `JsonSerializableAddressBook` first loads all contacts, rejecting duplicate IDs and duplicate contact details, then validates that every guardian reference points to an existing contact with the guardian role. A guardian cannot carry a guardian reference. Repeated JSON object fields are rejected during parsing.

`Person` stores phone and email as non-null `Optional` values. `Phone` and `Email` always represent valid, present
values. Phone details contain at least 3 ASCII digits (`0-9`) and no more than 100 characters. Other numerals and
Unicode text remain allowed but do not count towards the minimum. Saved phone values with fewer than 3 ASCII digits
are rejected during loading, including values that previously passed using only non-ASCII numerals.
The JSON adapter keeps both fields as
nullable strings: missing or null means absent, while empty and invalid
strings are rejected. Existing valid strings retain their capitalization. Single-label email domains and domains
whose final label has fewer than two characters are invalid; incompatible files fail loading without being rewritten
automatically. `JsonAdaptedPerson` rejects a guardian record that has neither a phone number nor an email address
while converting it to a model object. The `Person` constructor enforces the same invariant, so saved data cannot
bypass the guardian contact-method requirement.

Level and subject are optional JSON strings. Legacy records without these properties, and explicit null values,
load with the corresponding fields absent. Present values must satisfy their value-class constraints; blank or
invalid strings and guardian records containing either field are rejected during loading.

Email validation uses iterative character scans for the local part and domain labels, avoiding recursive regex
matching on long inputs. `Email.isValidEmail` and the parser share the same validation rules and specific errors.
For each component, unsupported characters are reported separately from punctuation-placement errors. Only ASCII
letters and digits are accepted; allowed punctuation must appear singly between letters or digits. The domain
permits hyphens only, while the local part also permits periods, underscores, percent signs and plus signs.
An email is limited to 254 characters, with at most 64 in the local part and 63 in each domain label.
The parser trims surrounding whitespace before checking these limits and reports
`Email address cannot exceed 254 characters.` for longer values. The same limits apply to saved email values;
overlong values cause loading to fail without rewriting the file.
Email component length validation also applies during Add, Edit and JSON loading. Previously accepted emails
with local parts longer than 64 characters or domain labels longer than 63 characters fail loading. The original
file is preserved; correct the invalid values and restart to re-enable data-changing commands. Length errors
report the exceeded limit in CLI parsing. Email accepts ASCII characters only, so character and byte counts agree.


Loading applies the duplicate identity rules described under Contact details and Add validation, including the
exception for contactless students. Full object equality remains case-sensitive so capitalization-only edits remain
observable. Existing same-role records differing only in
capitalization may therefore fail duplicate validation on loading. IDs and guardian links remain independent of
duplicate identity.

Each command declares whether it changes data through `Command.isDataChanging()`. `LogicManager` executes data-changing commands on a candidate model carrying the original filter. It saves that candidate before replacing the live model. An unsuccessful command or save therefore cannot change the live contacts, relationships or displayed list. `JsonAddressBookStorage` writes a temporary file in the destination directory and atomically replaces the data file; it fails safely if atomic replacement is unavailable. Read-only commands do not save data.

The activity diagram below separates command validation, persistence and publication to the live model. Temporary
file cleanup is attempted after every save attempt; cleanup failures are logged without replacing the save outcome.

<puml src="diagrams/SafeSaveActivityDiagram.puml" width="520" alt="Candidate execution and safe-save decision flow" />

If loading fails, storage records that failure for the session. The UI displays the startup error, and all data-changing commands are disabled until the user fixes the file and restarts the application. The invalid file is never overwritten during that session. A missing data file starts with an empty list and is created by the first successful data-changing command.

Relevant tests cover Add parsing and validation, repeated parameters, fixed validation-error ordering, whitespace
separators, literal prefixes inside quoted names, level and subject length boundaries, the phone length limit,
optional student contact methods, guardians with either one contact method, guardian contact-method
requirements, exact duplicates and possible-duplicate warning priority, displayed indexes, role validation, first links, replacements, shared guardians, ID preservation,
relationship persistence, deletion cleanup, invalid references, failed saves and UI selection/details. JavaFX UI tests
run on Windows desktop environments; the remaining tests run on all supported platforms.

### Listing and finding contacts

`list` and `find` change which contacts the list panel shows by updating the predicate of the model's filtered list.
Neither command changes any contact data.

**Listing.** `list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]` shows the contacts that match a `ContactFilter`, as
shown in the class diagram below.

<puml src="diagrams/ContactFilterClassDiagram.puml" alt="Classes involved in filtering the contact list" />

A `ContactFilter` is a `Predicate<Person>` that holds an optional `Role`, `StudentStatus`, `Level` and `Subject`. A
contact matches when it has every chosen value; an empty value matches any contact. Levels and subjects match when
`Level#isSameValue` and `Subject#isSameValue` hold, so `list lv/sec3` matches `Sec 3`, ignoring letter case and spaces.
A whole value must match, so `sub/Math` does not match `A-Math`. Guardians are always `ACTIVE` and have no level or
subject, so they never match a filter for former contacts or for a level or subject.
`ContactFilter.ACTIVE_CONTACTS` (guardians and active students) is the default, `ContactFilter.FORMER_STUDENTS` shows
only former students, and `ContactFilter.ALL_CONTACTS` shows everyone. `Model#PREDICATE_SHOW_ACTIVE_CONTACTS` and
`Model#PREDICATE_SHOW_FORMER_STUDENTS` refer to the first two filters, so the rules for which contacts are active live
in one class.

`ListCommandParser` checks the input as shown in the activity diagram below, and stops at the first problem it finds.

<puml src="diagrams/ListParserActivityDiagram.puml" width="400" alt="Checks made by ListCommandParser" />

* **Format error:** any prefix other than `r/`, `st/`, `lv/` or `sub/` (such as `n/` in `n/Alex`), or any text before
  the first prefix (such as `extra` in `list extra r/student`), gives
  `Invalid list command. Use: list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]`.
  If the whole input is a single role or status without its prefix, such as `list former`, the message suggests the
  intended command instead: `Invalid list command. Did you mean: list st/former?`
* **Repeated parameter:** a repeated prefix gives `Each of r/, st/, lv/ and sub/ may appear only once.`
* **Role or status error:** `r/ROLE` is parsed with `ParserUtil#parseRole`, the same method `add` uses, so role values
  and errors match `add`. `st/` accepts `active`, `former` or `all`, ignoring letter case; `all` becomes an empty
  status, which matches any status. Any other value, or an empty `st/`, gives
  `Invalid status. Status must be active, former, or all.` Without `st/`, the status is `ACTIVE`. `lv/` and `sub/`
  are parsed with `ParserUtil#parseLevel` and `ParserUtil#parseSubject`, the same methods `add` and `edit` use, so
  their values and errors match those commands. The values are checked in the order `r/`, `st/`, `lv/`, `sub/`,
  whatever order the user types them in.
* **Guardians cannot be former:** `r/guardian st/former` gives
  `Guardians cannot be former. Remove r/guardian or st/former.`, because that filter could never match a contact.
* **Guardians have no level or subject:** `r/guardian` with `lv/` or `sub/` gives
  `Guardians have no level or subject. Remove r/guardian, or remove lv/ and sub/.`, for the same reason.

`ListCommand#execute` passes its filter to `Model#updateFilteredPersonList`. It then reports the number of contacts
shown, for example `Listed 6 contacts` or `Listed 2 former students`, using `Messages#formatContactCount` and
`Messages#formatFormerStudentCount`.

When the filter shows active contacts only (`ContactFilter#hidesFormerStudents`), former students are hidden without
the tutor asking for it. To make this visible, `ListCommand` counts the former students that
`ContactFilter#withAnyStatus` (the same filter with any status) would also have shown, and adds the count to the
message, for example `Listed 6 contacts. 2 former students hidden. Add st/all to include them.` For
`list r/guardian`, this count is always 0, so no note is added.

When nothing matches, the message explains why, so the tutor knows whether the contact list is empty or the filter hid
everyone:

* `list` with no saved contacts gives `No contacts found.` If every saved contact is a former student, it gives
  `No active contacts found. Use list st/former to see former students.`
* `list st/former` gives `No former students found.`
* `list r/guardian` gives `No guardians found.` `list r/student` gives `No students found.`, or
  `No active students found. Use list r/student st/former to see former students.` if every student is former.
* A filter with a level or subject gives `No contacts found.`, followed by the hidden-former-students note if
  former students would match. This check comes before the status and role messages, because a missing level or
  subject is then the reason nothing is shown. For example, `list st/former lv/Sec 4` gives `No contacts found.`
  rather than `No former students found.`, which would suggest there are no former students at all.

`ListCommand#execute` also logs the filter and both counts at the `FINE` level, to help trace unexpected results.

`list` changes no contact data, so `LogicManager` runs it directly on the live model, and it still works when data
changes are disabled after a failed load.

The sequence diagram below shows how `list r/student` is executed.

<puml src="diagrams/ListSequenceDiagram.puml" alt="Interactions for the `list r/student` command" />

**Finding.** `FindCommandParser` treats all the text after `find` as a single search term. It removes leading and
trailing whitespace, reduces each run of internal whitespace to one space, and rejects a term that is then empty with
`Invalid find command. Use: find NAME`. The term is wrapped in a `NameContainsSubstringPredicate`, which matches a
contact whose name contains the term, ignoring letter case. Consecutive spaces in the name are also treated as one
space. `FindCommand` applies the predicate with `Model#updateFilteredPersonList` and reports either
`Found X contacts matching: NAME.` or `No contacts found matching: NAME`.

The term is matched as a whole, so word order matters: `find tan alex` does not match `Alex Tan`. Matching only the
name keeps results predictable for the tutor, who searches for a person rather than for a phone number or email.

Curly apostrophes (`’` and `‘`) in the name and the term are treated as straight apostrophes, using
`Name#replaceCurlyApostrophes`. Names copied from messaging apps often contain `’`, while tutors usually type `'`.
`PersonDisplayOrderComparator` applies the same replacement, so these names are also ordered together. Accents are also
ignored, using `Name#removeAccents`, so `find elise` matches `Élise` and `find soren` matches `Søren`. Tutors may not
know how to type accented letters, and the contact list already orders accented letters with their unaccented forms.
Both the name and the term are processed in the same way, so a match is never lost; scripts that use combining marks for
vowels may match slightly more names.

The sequence diagram below shows how `find ale` is executed.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Interactions for the `find ale` command" />

#### Design considerations:

**Aspect: Where filtering by role and status happens**

* **Alternative 1 (current choice):** Add optional filters to `list`, and keep `find` for names only.
  * Pros: `list r/student` reads as "list the students". `find NAME` stays a simple name search. The default `list`
    already hides former students, so `list r/student` shows only current students without an extra parameter.
  * Cons: Searching by name and role together needs two steps, `find` and then reading the roles shown on the cards.
* **Alternative 2:** Add the filters to `find`, so that the name becomes optional.
  * Pros: One command could combine a name with a role or status.
  * Cons: `find r/student` with no name reads oddly. `find` includes former students, so `find r/student` would
    show former students too, which defeats the purpose of hiding them.

**Aspect: How the status is given**

* **Alternative 1 (current choice):** A separate `st/` prefix, independent of `r/`.
  * Pros: Role and status are separate attributes in the model, as in `Role: Student [Former]`. `r/` accepts the same
    values in `add` and `list`. This follows common command-line tools, such as `gh issue list --label bug
    --state closed`, where each attribute has its own option.
  * Cons: Users must learn a second prefix, and `st/` is close to `link`'s `s/`. The invalid-format message names the
    valid prefixes to help with this.
* **Alternative 2:** Treat `former` as a role value, as in `list r/former`.
  * Pros: Only one prefix to learn.
  * Cons: `r/former` would be accepted by `list` but not by `add` or `edit`. It could not express "all students,
    active and former" without another special value.

**Aspect: How `ListCommand` stores what to show**

* **Alternative 1 (current choice):** A single `ContactFilter` object holding the optional role and the status.
  * Pros: New filters, such as level and subject, are added as fields of one class, without changing how
    `ListCommand` applies the filter. The filter has `equals`, so parser tests can compare commands directly.
  * Cons: One more class than a plain lambda.
* **Alternative 2:** One `boolean` or lambda per option in `ListCommand`.
  * Pros: Less code for a single option.
  * Cons: Each new option adds another constructor argument and branch, and `new ListCommand(true)` does not say what
    `true` means.

### Ordering and displaying contacts

**Ordering.** `ModelManager` wraps its `FilteredList` in a `SortedList` that uses `PersonDisplayOrderComparator`, and
`Model#getFilteredPersonList` returns this sorted view. The comparator orders contacts by name, ignoring letter case and
treating consecutive spaces as one space. It first compares names with accents removed using `Name#removeAccents`, so
that `Élise` is ordered with `Elise` rather than after `Zoe`. This method decomposes the name with
`java.text.Normalizer` and removes the combining marks. Some letters, such as `ø`, `ł`, `æ` and `ß`, are separate
letters in Unicode and cannot be decomposed, so the method replaces them with their base letters from a fixed table, for
example `Søren` with `Soren`. Names that are still equal are then compared with their accents, so that names differing
only in accents have a fixed order. `java.text.Collator` was not used because it ignores spaces and some punctuation,
which would place `Alexa Lim` before `Alex Tan`. Contacts with the same name are ordered by role, with guardians before
students. The specification does not order contacts with the same name and role, so the comparator treats them as equal.

Commands that take an index, such as `edit`, `delete` and `link`, look it up in `Model#getFilteredPersonList`, so
the index always refers to the order shown on screen. The order is kept after `list`, `find`, `add` and
`edit`, and when the application starts, without any command having to sort the list itself.

**List cards.** `PersonCard` shows each contact as `NAME [ROLE]` followed by `Phone:` and `Email:` lines.
`ContactDisplayFormatter` formats these values, showing `Not provided` for a missing phone number or email address.
A student's card also shows `Guardian: NAME`, or `Guardian: None` if the student has no linked guardian.
Each line wraps instead of being cut off, so long names, email addresses and guardian names are shown in full.

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

**Value proposition**: TutorRoster helps independent private tutors keep student and guardian contact information organized and up to date, allowing them to retrieve accurate details quickly when communicating with students and families.


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
* **Normalized value**: A value after leading and trailing spaces are removed and repeated spaces inside a name are collapsed to one. This is the form used to store contacts and check for duplicates

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
      Expected: The GUI opens with an empty contact list, no selected contact and the command box focused.
      No data file is created until the first successful data-changing command. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

### Adding a contact

Use a disposable application folder with an empty contact list. Run the following cases in order.
For each failed Add command, verify that contacts, selection, details and saved data remain unchanged.
The failed command should remain in the command box, which keeps focus.

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
   Expected: The phone error says it must contain at least 3 digits (0-9).

1. Test case: `add r/student n/Alex Tan p/`.<br>
   Expected: The result says `Empty phone parameter: Phone cannot be empty when p/ is specified.`

1. Test case: `add r/student n/Alex Tan p/` followed immediately by 101 ASCII digits.<br>
   Expected: The result says `Phone details cannot exceed 100 characters.`

1. Test case: `add r/teacher n/Alex Tan p/12` and `add p/12 n/Alex Tan r/teacher`.<br>
   Expected: Both report the role error first, following the fixed parameter order regardless of command order.

1. Syntax errors to try, each leaving the existing contacts unchanged:
   * `add` reports `Missing required field: r/ROLE.` followed by the command usage.
   * `add r/student` reports `Missing required field: n/NAME.` followed by the command usage.
   * `add r/student n/"Alex Tan" a/15` identifies `Invalid parameter: a/.` and lists valid parameters.
   * `add r/student n/Alex Tan r/guardian` reports
     `Repeated parameter: r/. Each parameter may appear only once.`

1. Test case: `add r/student n/Casey Lee lv/Sec 3 sub/A-Math`.<br>
   Expected: The student is added with both student details and no contact methods. The same name as an earlier
   student does not cause rejection because the contact methods differ. A general warning is shown because both
   students have no email. Restart and verify that the level and subject remain saved.

1. Test case: `add r/student n/Ravi lv/ sub/` and `add sub/ lv/ n/Ravi r/student`.<br>
   Expected: Both report `Empty level parameter: Level cannot be empty when lv/ is specified.`
   With `lv/Sec 3` instead, both report the empty-subject message. No contact is added.

1. Test case: `add r/guardian n/Mei Tan p/91234567 lv/Sec 3`.<br>
   Expected: `Only students can have a level or subject. Remove lv/ and sub/ when adding a guardian.`
   Without the phone prefix, the guardian contact-method error takes precedence.

1. Test case: `add r/student n/"Ravi lv/Sec sub/Math"`.<br>
   Expected: The entire quoted text is saved as the name, with no level or subject set. Repeat with pasted tabs
   between parameters instead of spaces; the result is unchanged. A tab inside the quoted name is rejected.

### Deleting contacts

1. Deleting contacts while all contacts are being shown

   1. Prerequisites: Prepare a dataset with at least three active contacts visible after running `list`.
      Back up `data/addressbook.json` with the app closed. Before each successful deletion test below, close
      the app, restore that same backup, restart the app and run `list`. This ensures `delete 1 3` and
      `delete 3 1` start from the same contacts and indexes. Restarting alone does not restore deleted contacts.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 1 3`<br>
      Expected: The contacts originally shown at indexes 1 and 3 are deleted. The status message lists both contacts.

   1. Test case: `delete 3 1`<br>
      Expected: The same two contacts are deleted, listed in the order supplied.

   1. Test case: `delete 1 1`<br>
      Expected: No contact is deleted. The status message says each index may appear only once.

   1. Test case: `delete 1 INDEX`, where `INDEX` is larger than the list size.<br>
      Expected: No contact is deleted, including the valid contact at index 1. The status message identifies the
      out-of-range index.

   1. Test case: `delete 0`<br>
      Expected: No contact is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Reviewing contact data

1. Prerequisites: start with an empty list, then run `add r/student n/Alex Tan p/91234567 lv/Sec 3 sub/Math`
   and `add r/guardian n/Mei Tan p/98765432`. Run `list` before using the displayed indexes below.

1. Test case: `check guardians`.<br>
   Expected: `Guardian links (1)` identifies `1: Alex Tan [Student]: no guardian linked.` Suggestions are advisory;
   contacts and the displayed list remain unchanged.

1. Test case: `link s/1 g/2`, `archive 1`, `list st/former`, then `check guardians`.<br>
   Expected: One displayed contact is checked and `Guardian links (0)` reports `No suggestions.` The former
   student's link remains recognized even though the guardian is hidden from the displayed view.

1. Test case: on that former-student view, `edit 1 lv/NONE sub/NONE`, then `check details`.<br>
   Expected: `1: Alex Tan [Student] [Former]: consider adding level, subject.` Restore with
   `edit 1 lv/Sec 3 sub/Math`, then run `check details` again. Expected: `Contact and student details (0)` and
   `No suggestions.` The contact ID, former status and guardian link are preserved.

1. Test case: `check unknown` or `check guardians details`.<br>
   Expected: `Use check [guardians|details|duplicates].` No contacts or saved data change.

1. Test case: `list`, then `find Nobody`, then `check`.<br>
   Expected: `No displayed contacts to check. Use list or find to display contacts first.`

### Undoing a deletion

1. Prerequisites: prepare an active student linked to a guardian and a former student linked to the same guardian.
   Close the application and back up `data/addressbook.json`. Restore this same backup with the application closed
   before each independent case below; restarting alone does not restore deleted contacts.

1. Test case: `list`, delete the guardian by its displayed index, then `undo`.<br>
   Expected: The guardian returns with its original ID and details. Both students regain their guardian link,
   including the hidden former student. The active view is restored and the guardian is selected.

1. Test case: `list st/former`, `delete 1`, `list`, then `undo`.<br>
   Expected: The former student returns with its original status, details and guardian. The former-student view
   returns and selects the restored student.

1. Test case: `list`, `delete 1 2`, then `undo`.<br>
   Expected: Both original contacts return together, including any relationship between them. A second `undo`
   reports `No deletion is available to undo.`

1. Test case: delete a contact, successfully edit a remaining contact, then `undo`.<br>
   Expected: Undo is unavailable and the edit is retained. Repeat with an invalid edit instead; undo remains available.

1. Test case: delete a contact, close and reopen the application, then `undo`.<br>
   Expected: The deletion remains saved and undo is unavailable because history is session-only.

Save failures are verified by `UndoDeletionTest` with simulated storage errors. The tests check both failure to undo
and failed intervening changes, ensuring that live data, saved data and the previous undo opportunity are preserved.

### Editing a contact

1. Prerequisites: start with an empty contact list, for example by running `clear`. Then add three contacts:
   `add r/student n/Bernice Yu p/99272758`, `add r/guardian n/David Li p/91031282` and
   `add r/student n/Irfan Ibrahim e/irfan@example.com`.

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

1. Test case: `add r/student n/Roy Balakrishnan p/92624417 e/irfan@example.com`, then `edit INDEX n/Irfan Ibrahim`
   using Roy's index.<br>
   Expected: The name is updated. Below the success message, a warning says another contact with the same role and
   name, and the same email address exists, and that the contact may be a duplicate but was updated.

1. Incorrect commands to try, each leaving the contacts unchanged:
   * `edit 1` gives `At least one of n/, p/, e/, lv/, or sub/ must be provided.`
   * `edit n/Alex` gives `Invalid edit command. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]`
   * `edit 0 n/Alex` gives `The contact index must be a positive integer shown in the list.`
   * `edit 999 n/Alex` gives `No contact exists at index 999.`
   * `edit 1 r/guardian` gives `Unknown parameter. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [lv/LEVEL] [sub/SUBJECT]`
   * `edit 1 n/A n/B` gives `Each of n/, p/, e/, lv/, and sub/ may appear only once.`
   * Editing a contact so that it matches another contact's role, name, phone and email gives
     `This edit would create a duplicate contact.`

### Archiving and unarchiving students

Use a separate, disposable application folder with an empty contact list. Run these cases in order so the
displayed indexes are predictable. Launch with Java 25 using `java -jar "tutorroster.jar"`; use the same folder
for each restart. This setup is independent of the data used in the other manual-testing sections.

1. Prepare a linked student and guardian:
   1. `add r/student n/Ava Tan p/98765432 e/ava@example.com lv/Sec 3 sub/Math`
   1. `add r/guardian n/Mei Tan p/91234567`
   1. `list`, then `link s/1 g/2`.<br>
      Expected: Ava Tan is student `1`, Mei Tan is guardian `2`, and Ava is linked to Mei.
   1. `view 1`.<br>
      Expected: Ava's details show her phone, email, level, subject and guardian Mei Tan.

1. Archive, restart and reactivate:
   1. `archive 1`.<br>
      Expected: `Marked Ava Tan as a former student.` Ava disappears from the default list; Mei remains.
      Because Ava was selected, the selection clears and the detail panel says `Select a contact to view details.`
   1. `list st/former`, then `view 1`.<br>
      Expected: Ava is the only displayed contact, with the `Former` badge. Her contact details, level,
      subject and link to Mei are unchanged, even though Mei is outside this view.
   1. `exit`, then relaunch the same JAR from the same folder. Run `list st/former`, then `view 1`.<br>
      Expected: Ava is still former, with the same details and guardian. The saved status and relationship
      survive restart.
   1. `unarchive 1`.<br>
      Expected: `Marked Ava Tan as an active student.` The former-student list becomes empty, and the selection
      and details clear. Run `list` to see Ava at `1` and Mei at `2` again.
   1. `exit`, then relaunch again. Run `list`, then `view 1`.<br>
      Expected: Ava is active, and her details and guardian link are still present.

1. Change status while searching:
   1. `find Ava`, then `view 1`.
   1. `archive 1`.<br>
      Expected: Ava stays at index `1` in the search results, gains the `Former` badge, and remains selected.
      Her detail panel still shows guardian Mei Tan.
   1. `unarchive 1`.<br>
      Expected: Ava stays selected in the same search results. The `Former` badge disappears and her other
      details stay unchanged.

1. Reject invalid targets and repeated status changes:
   1. Run `find Tan` to show active student Ava at `1` and guardian Mei at `2`.
   1. Try each command below. For every rejection, verify that the displayed contacts, their status, the selected
      contact, guardian link and saved data remain unchanged.

      | Command | Expected error |
      | --- | --- |
      | `unarchive 1` | `Ava Tan is already an active student.` |
      | `archive 2` | `Only students can be archived.` |
      | `unarchive 2` | `Only students can be unarchived.` |
      | `archive 3` and `unarchive 3`, separately | `No contact exists at index 3.` |
      | `archive 0` and `unarchive abc`, separately | `The contact index must be a positive integer shown in the list.` |

   1. Run `archive 1`, then repeat `archive 1`.<br>
      Expected: The first command succeeds and Ava remains visible because `find Tan` is still applied.
      The second reports `Ava Tan is already a former student.` and leaves the data unchanged.

### Listing contacts

1. Prerequisites: start with an empty contact list, for example by running `clear`.

1. Test case: `list`<br>
   Expected: No contacts are shown. The status message shows `No contacts found.`

1. Test case: `add r/student n/Alex Yeoh`, then `list`<br>
   Expected: Alex Yeoh is shown. The status message shows `Listed 1 contact`.

1. Test case: `add r/guardian n/Mei Tan p/98765432`, then `list`<br>
   Expected: Both contacts are shown. The status message shows `Listed 2 contacts`.

1. Test case: `list 3`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Invalid list command. Use: list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]`

### Filtering the contact list

1. Prerequisites: start with an empty contact list, for example by running `clear`. Then add four contacts:
   `add r/guardian n/Mei Tan p/98765432`, `add r/student n/Alex Tan p/91234567 lv/Sec 3 sub/A-Math`,
   `add r/student n/Bea Lim lv/Sec 4 sub/A-Math` and `add r/student n/Chloe Ng lv/Sec 3`. Mark Chloe Ng as former
   with `find Chloe`, then `archive 1`.

1. Test case: `list`<br>
   Expected: Alex Tan, Bea Lim and Mei Tan are shown. The status message shows
   `Listed 3 contacts. 1 former student hidden. Add st/all to include them.`

1. Test case: `list r/student`<br>
   Expected: Alex Tan and Bea Lim are shown. The status message shows
   `Listed 2 contacts. 1 former student hidden. Add st/all to include them.`

1. Test case: `list r/GUARDIAN`<br>
   Expected: Mei Tan is shown. The status message shows `Listed 1 contact`.

1. Test case: `list r/student st/former`<br>
   Expected: Chloe Ng is shown. The status message shows `Listed 1 former student`.

1. Test case: `list st/all`<br>
   Expected: All four contacts are shown. The status message shows `Listed 4 contacts`.

1. Test case: `list r/student st/ALL`<br>
   Expected: Alex Tan, Bea Lim and Chloe Ng are shown. The status message shows `Listed 3 contacts`.

1. Test case: `list r/guardian st/former`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Guardians cannot be former. Remove r/guardian or st/former.`

1. Test case: `list r/teacher`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Invalid role. Role must be student or guardian.`

1. Test case: `list r/student r/guardian`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Each of r/, st/, lv/ and sub/ may appear only once.`

1. Test case: `list st/former st/former`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Each of r/, st/, lv/ and sub/ may appear only once.`

1. Test case: `list st/active`<br>
   Expected: The same contacts and message as `list`.

1. Test case: `list st/archived`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Invalid status. Status must be active, former, or all.`

1. Test case: `list former`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Invalid list command. Did you mean: list st/former?`

1. Test case: `list n/Alex`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Invalid list command. Use: list [r/ROLE] [st/STATUS] [lv/LEVEL] [sub/SUBJECT]`

1. Test case: `list lv/sec3`<br>
   Expected: Alex Tan is shown. The status message shows
   `Listed 1 contact. 1 former student hidden. Add st/all to include them.`

1. Test case: `list sub/a-math`<br>
   Expected: Alex Tan and Bea Lim are shown. The status message shows `Listed 2 contacts`.

1. Test case: `list lv/Sec 3 sub/A-Math`<br>
   Expected: Alex Tan is shown. The status message shows `Listed 1 contact`.

1. Test case: `list lv/Sec 3 st/all`<br>
   Expected: Alex Tan and Chloe Ng are shown. The status message shows `Listed 2 contacts`.

1. Test case: `list st/former lv/Sec 4`<br>
   Expected: No contacts are shown, because the only former student, Chloe Ng, is at Sec 3. The status message shows
   `No contacts found.`

1. Test case: `list sub/Math`<br>
   Expected: No contacts are shown, because `Math` and `A-Math` are different subjects. The status message shows
   `No contacts found.`

1. Test case: `list r/guardian lv/Sec 3`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Guardians have no level or subject. Remove r/guardian, or remove lv/ and sub/.`

1. Test case: `list lv/Sec 3 lv/Sec 4`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Each of r/, st/, lv/ and sub/ may appear only once.`

1. Test case: `list lv/`<br>
   Expected: The shown contacts are unchanged. The status message shows
   `Empty level parameter: Level cannot be empty when lv/ is specified.`

1. Test case: `find Mei`, then `delete 1`, then `list r/guardian`<br>
   Expected: No contacts are shown. The status message shows `No guardians found.`

### Finding contacts

1. Prerequisites: start with an empty contact list, for example by running `clear`. Then add three contacts:
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

1. Prerequisites: start with an empty contact list, for example by running `clear`. Then add four contacts in this
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
   Expected: The new guardian `aaron lim` is shown first, as the order ignores capitalization.

1. Test case: `add r/student n/Maximilian Alexander Christopher Bartholomew Montgomery Fitzgerald`<br>
   Expected: The whole name is visible. If it does not fit on one line, it wraps onto the next line instead of being
   cut off.

### Linking students to guardians

Use a disposable copy of TutorRoster in a separate folder so these tests do not change real contact data.
Run the following cases in order.

1. Prerequisites: run `clear`, then add `add r/student n/Alex Tan`, `add r/student n/Bea Tan`,
   `add r/guardian n/Mei Tan p/98765432` and `add r/guardian n/Sarah Lim e/sarah@example.com`.
   Run `list`. The displayed indexes are Alex 1, Bea 2, Mei 3 and Sarah 4.

1. Test case: `link s/01 g/03`<br>
   Expected: `Linked student Alex Tan to guardian Mei Tan.` Alex is selected. His card shows `Guardian: Mei Tan`,
   and the detail panel shows Mei's name, `Guardian phone: 98765432` and `Guardian email: Not provided`.
   The command box is empty and retains focus. The relationship is saved automatically.

1. Test case: `link g/3 s/1`<br>
   Expected: `Alex Tan is already linked to Mei Tan.` Contacts, links, selection, details and saved data are unchanged.
   The failed command remains in the command box, which retains focus.

1. Test case: `link g/4 s/1`<br>
   Expected: `Changed Alex Tan’s guardian from Mei Tan to Sarah Lim.` Alex remains selected, with Sarah's name,
   `Guardian phone: Not provided` and `Guardian email: sarah@example.com` in the detail panel.

1. Test case: `find Tan`, then `link s/1 g/3`<br>
   Expected: The displayed contacts are Alex, Bea and Mei. Alex's guardian changes from Sarah to Mei even though
   Sarah is outside the search results. Only those three contacts remain displayed after linking.

1. Test case: `find Alex`<br>
   Expected: Alex's card and detail panel still show Mei's details even though Mei is hidden by the search.
   `link s/1 g/3` now reports `No contact exists at guardian index 3.` without changing the existing link. Run `list`.

1. Test case: `link s/2 g/3`, then close and restart TutorRoster.<br>
   Expected: Alex and Bea both retain their links to Mei. All four contacts reload in alphabetical order.
   No contact is initially selected. Selecting either student shows Mei's current details.

1. Test case: `edit 3 n/Zara Tan p/NONE e/zara@example.com`<br>
   Expected: The edited guardian is selected and moves to index 4. Select Alex, then Bea; each shows
   `Guardian: Zara Tan`, `Guardian phone: Not provided` and `Guardian email: zara@example.com`.
   Restart and check that both links and the updated guardian details remain.

1. Test case: `delete 4`<br>
   Expected: Zara is deleted and two relationships are reported removed. Alex and Bea remain with `Guardian: None`.
   Restart and check that the guardian and both links remain absent.

1. Incorrect commands to try while all contacts are displayed:
   * `link s/1` or `link s/1 g/3 extra`: `Invalid link command. Use: link s/STUDENT_INDEX g/GUARDIAN_INDEX`
   * `link s/1 g/3 x/4`: `Unknown parameter. Use: link s/STUDENT_INDEX g/GUARDIAN_INDEX`
   * `link s/1 s/2 g/3`: `Each of s/ and g/ may appear only once.`
   * `link s/0 g/3`: `Student index must be a positive integer shown in the list.`
   * `link s/1 g/-1`: `Guardian index must be a positive integer shown in the list.`
   * `link s/999 g/3`: `No contact exists at student index 999.`
   * `link s/3 g/3`: `Student index must refer to a student.`
   * `link s/1 g/2`: `Guardian index must refer to a guardian.`
   Expected for every failure: contacts, relationships, selection, details and saved data remain unchanged.

1. Same-name students: run `clear`, add `add r/student n/Alex Tan` twice, then add
   `add r/guardian n/Mei Tan p/98765432`. Run `link s/2 g/3`.<br>
   Expected: Only the second Alex is linked and selected. The first Alex still shows `Guardian: None`.
   Restart and check that both students are present with their separate links. Run `link s/1 g/3`, then
   `edit 2 n/Bea Tan`. Only the second student is renamed and both retain Mei as guardian. Rename Bea back to Alex,
   then `delete 2`; only the second student is removed. Restart and check that the first Alex and Mei remain linked.

### Saving data

Use a disposable application folder and keep a backup of its data file before testing failures.

1. Missing data file: launch TutorRoster in a folder without `data/addressbook.json`.<br>
   Expected: An empty list, no selected contact and no load error. `list` does not create the file.
   Add a student and a guardian, link them, then restart. Both contacts and their relationship reload.

1. Invalid data file: close TutorRoster, replace `data/addressbook.json` with `{invalid json}`, and restart.<br>
   Expected: An empty list and `Data changes are disabled because the data file could not be loaded.
   Fix the file and restart TutorRoster.` Valid `add`, `link`, `edit`, `delete` and `clear` commands are blocked.
   `list` still works, and the invalid file remains unchanged. Restore the backup and restart to recover the contacts.

1. Invalid relationship: close the app and change a student's `guardianId` in the test file to the ID of a student,
   or to a UUID that is absent from the contact records. Restart.<br>
   Expected: The same load-failure behavior as above, with no contacts partially loaded or invalid file overwritten.
   Restore the backup before continuing.

1. Save failure on Windows: start with two students and two guardians in the disposable folder. Link one student,
   select that student, and use `find` to display both that student and a different guardian. Keep the app open and
   lock `data/addressbook.json` against replacement, for example by opening it in a separate PowerShell session with
   `File.Open` using `FileMode.Open`, `FileAccess.Read` and `FileShare.None`. Attempt to link to the other guardian.<br>
   Expected: `Changes could not be saved. No changes were made.` The old guardian, filtered list, selected contact
   and detail panel remain unchanged. Release the lock by disposing the file stream; the file is unchanged and
   restarting restores the old link. Retrying the command after releasing the lock succeeds.

   Automated `LinkPersistenceTest` and `LinkUiTest` also exercise failed saves using a storage implementation that
   throws `IOException`, without requiring file-permission changes.
