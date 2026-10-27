package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void resolveApplicationDirectory_jarInFolderWithSpacesAndUnicode_returnsJarDirectory() throws Exception {
        Path jarDirectory = Files.createDirectories(temporaryFolder.resolve("Tutor Roster \u5b66\u751f"));
        Path jar = Files.createFile(jarDirectory.resolve("tutorroster.jar"));

        assertEquals(jarDirectory, MainApp.resolveApplicationDirectory(jar.toUri()));
    }

    @Test
    public void getApplicationDirectory_unpackedApplication_usesWorkingDirectory() throws Exception {
        assertEquals(Path.of("").toAbsolutePath(), new MainApp().getApplicationDirectory());
    }

    @Test
    public void resolveApplicationDirectory_unpackedClasses_usesWorkingDirectory() throws Exception {
        Path classesDirectory = Files.createDirectories(temporaryFolder.resolve("classes"));

        assertEquals(Path.of("").toAbsolutePath(), MainApp.resolveApplicationDirectory(classesDirectory.toUri()));
    }

    @Test
    public void init_jarDirectory_savesAndReloadsContactsAndPreferences() throws Exception {
        Path jarDirectory = Files.createDirectories(temporaryFolder.resolve("Tutor Roster \u5b66\u751f"));
        Path jar = Files.createFile(jarDirectory.resolve("tutorroster.jar"));
        MainApp app = createAppAt(jar);
        app.init();
        Path contactsFile = jarDirectory.resolve("data").resolve("addressbook.json");
        Path preferencesFile = jarDirectory.resolve("preferences.json");
        assertEquals(contactsFile, app.storage.getAddressBookFilePath());
        assertTrue(Files.exists(preferencesFile));
        assertFalse(Files.exists(contactsFile));

        app.logic.execute("add r/student n/Alex Tan");
        GuiSettings guiSettings = new GuiSettings(800, 600, 10, 20);
        app.logic.setGuiSettings(guiSettings);
        app.stop();

        MainApp reloaded = createAppAt(jar);
        reloaded.init();
        assertEquals(app.model.getAddressBook(), reloaded.model.getAddressBook());
        assertEquals(guiSettings, reloaded.logic.getGuiSettings());
    }

    @Test
    public void init_packagedJarFromDifferentWorkingDirectories_reusesContactsAndPreferences() throws Exception {
        Path jarDirectory = Files.createDirectories(temporaryFolder.resolve("Tutor Roster \u5b66\u751f"));
        Path jar = Files.copy(Path.of(System.getProperty("tutorroster.test.jar")),
                jarDirectory.resolve("tutorroster.jar"));
        Path launcher = createPackagedLauncher(jar);
        Path firstWorkingDirectory = Files.createDirectory(temporaryFolder.resolve("first launch"));
        Path secondWorkingDirectory = Files.createDirectory(temporaryFolder.resolve("second launch"));
        Path contactsFile = jarDirectory.resolve("data").resolve("addressbook.json");
        JsonAddressBookStorage contactsStorage = new JsonAddressBookStorage(contactsFile);
        JsonUserPrefsStorage preferencesStorage = new JsonUserPrefsStorage(jarDirectory.resolve("preferences.json"));

        runPackagedApp(launcher, firstWorkingDirectory, "create");

        Person originalContact = contactsStorage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals("Alex Tan", originalContact.getName().fullName);
        assertEquals(new GuiSettings(800, 600, 10, 20),
                preferencesStorage.readUserPrefs().orElseThrow().getGuiSettings());
        assertNoApplicationData(firstWorkingDirectory);

        runPackagedApp(launcher, secondWorkingDirectory, "reload");

        List<Person> reloadedContacts = contactsStorage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(List.of("Alex Tan", "Ben Lim"),
                reloadedContacts.stream().map(person -> person.getName().fullName).toList());
        assertEquals(originalContact.getId(), reloadedContacts.getFirst().getId());
        assertEquals(new GuiSettings(900, 650, 30, 40),
                preferencesStorage.readUserPrefs().orElseThrow().getGuiSettings());
        assertNoApplicationData(firstWorkingDirectory);
        assertNoApplicationData(secondWorkingDirectory);
    }

    /** Keeps the application JAR's Unicode path out of Windows' native command-line encoding. */
    private Path createPackagedLauncher(Path applicationJar) throws Exception {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MainAppJarTestLauncher.class.getName());
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, applicationJar.toUri().toASCIIString());
        String launcherClass = MainAppJarTestLauncher.class.getName().replace('.', '/') + ".class";
        Path launcher = temporaryFolder.resolve("launcher.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(launcher), manifest);
                InputStream classFile = MainAppJarTestLauncher.class.getResourceAsStream("/" + launcherClass)) {
            jar.putNextEntry(new JarEntry(launcherClass));
            classFile.transferTo(jar);
        }
        return launcher;
    }

    /** Loads production classes only from the JAR, using a test-only launcher without opening a GUI. */
    private void runPackagedApp(Path launcher, Path workingDirectory, String operation) throws Exception {
        String javaExecutable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", javaExecutable);
        Path outputFile = workingDirectory.resolve("packaged-app.log");
        Process process = new ProcessBuilder(java.toString(), "-ea", "-jar",
                workingDirectory.relativize(launcher).toString(), operation)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(outputFile.toFile())
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Packaged application did not finish: " + outputFile);
            assertEquals(0, process.exitValue(), Files.readString(outputFile));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private void assertNoApplicationData(Path workingDirectory) {
        assertFalse(Files.exists(workingDirectory.resolve("data")));
        assertFalse(Files.exists(workingDirectory.resolve("preferences.json")));
    }

    private MainApp createAppAt(Path jar) {
        return new MainApp() {
            @Override
            Path getApplicationDirectory() {
                return resolveApplicationDirectory(jar.toUri());
            }
        };
    }

    @Test
    public void init_jarDataDirectoryBlocked_failedSaveLeavesContactsUnchanged() throws Exception {
        Path jar = Files.createFile(temporaryFolder.resolve("tutorroster.jar"));
        MainApp app = createAppAt(jar);
        app.init();
        Path blockedDirectory = temporaryFolder.resolve("data");
        Files.writeString(blockedDirectory, "Existing file");

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> app.logic.execute(
                "add r/student n/Alex Tan"));

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertEquals("Existing file", Files.readString(blockedDirectory));
    }

    private StorageManager createStorage(Path contactsFile) {
        return new StorageManager(new JsonAddressBookStorage(contactsFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    @Test
    public void initModelManager_missingFile_startsEmptyAndSavesFirstContact() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        StorageManager storage = createStorage(contactsFile);
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(contactsFile));
        assertTrue(logic.getStartupMessage().isEmpty());

        logic.execute("add n/Alex Tan p/91234567 e/alex@example.com r/student");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(Files.exists(contactsFile));
        Model reloaded = new MainApp().initModelManager(createStorage(contactsFile), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(model.getAddressBook().getPersonList().getFirst().getId(),
                reloaded.getAddressBook().getPersonList().getFirst().getId());
    }

    @Test
    public void initModelManager_invalidFile_startsEmptyAndDisablesChanges() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        String invalidData = "{invalid json}";
        Files.writeString(contactsFile, invalidData);
        StorageManager storage = createStorage(contactsFile);
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(Optional.of(LogicManager.MESSAGE_DATA_LOAD_FAILURE), logic.getStartupMessage());
        assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_LOAD_FAILURE, () -> logic.execute("clear"));
        assertEquals(invalidData, Files.readString(contactsFile));
    }

    @Test
    public void initModelManager_validFile_restoresContactsAndGuardianRelationship() throws Exception {
        Path contactsFile = temporaryFolder.resolve("contacts.json");
        Person guardian = new PersonBuilder().withName("Mei Tan").withRole("guardian").build();
        Person student = new PersonBuilder().withName("Alex Tan").withGuardianId(guardian.getId()).build();
        AddressBook contacts = new AddressBook();
        contacts.setPersons(List.of(student, guardian));
        StorageManager storage = createStorage(contactsFile);
        storage.saveAddressBook(contacts);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(contacts, model.getAddressBook());
        assertEquals(student.getId(), model.getAddressBook().getPersonList().getFirst().getId());
        assertEquals(Optional.of(guardian.getId()),
                model.getAddressBook().getPersonList().getFirst().getGuardianId());
        assertFalse(storage.isDataFileLoadFailed());
    }
}
