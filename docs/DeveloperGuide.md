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
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


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

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.

*{More to be added}*

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Contact**: A person record in TutorRoster that represents either a student or a guardian
* **Student**: A learner who receives or previously received tuition from the private tutor
* **Guardian**: An adult responsible for a student and whom the private tutor may contact regarding the student
* **Primary guardian**: The guardian designated as the private tutor's first point of contact for a student
* **Active student**: A student who is currently receiving tuition from the private tutor
* **Former student**: A student who is no longer receiving tuition but whose contact record is retained
* **Tutoring context**: Concise information about a student, such as the student's education level or subject

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

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
