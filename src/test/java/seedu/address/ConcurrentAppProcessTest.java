package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.storage.DataFileConflictException;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Exercises save conflicts across actual JVMs using production classes exclusively from the application JAR.
 */
public class ConcurrentAppProcessTest {
    @TempDir
    public Path temporaryFolder;

    private Path launcher;
    private Path contacts;

    @BeforeEach
    public void setUp() throws Exception {
        Path application = Files.copy(Path.of(System.getProperty("tutorroster.test.jar")),
                temporaryFolder.resolve("tutorroster.jar"));
        contacts = temporaryFolder.resolve("data/addressbook.json");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, ConcurrentAppTestLauncher.class.getName());
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, application.toUri().toASCIIString());
        launcher = temporaryFolder.resolve("launcher.jar");
        String className = ConcurrentAppTestLauncher.class.getName().replace('.', '/') + ".class";
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(launcher), manifest);
                InputStream bytecode = ConcurrentAppTestLauncher.class.getResourceAsStream("/" + className)) {
            jar.putNextEntry(new JarEntry(className));
            bytecode.transferTo(jar);
        }
    }

    @Test
    public void twoPackagedApplications_staleSecondSaveCannotLoseFirstContacts() throws Exception {
        Process first = startSession("first", "Alice");
        Process second = startSession("second", "Bob");
        try {
            awaitFile("first.ready", first);
            awaitFile("second.ready", second);
            if (System.getProperty("os.name").startsWith("Windows")) {
                assertEquals("GUI", Files.readString(temporaryFolder.resolve("first.ready")));
                assertEquals("GUI", Files.readString(temporaryFolder.resolve("second.ready")));
            }
            Files.writeString(temporaryFolder.resolve("first.go"), "");
            awaitFile("first.result", first);
            assertTrue(Files.readString(temporaryFolder.resolve("first.result")).contains("Added contact: Alice"));
            Files.writeString(temporaryFolder.resolve("second.go"), "");
            awaitFile("second.result", second);

            assertEquals(DataFileConflictException.MESSAGE,
                    Files.readString(temporaryFolder.resolve("second.result")));
            assertEquals(List.of("Alice"), new JsonAddressBookStorage(contacts).readAddressBook().orElseThrow()
                    .getPersonList().stream().map(person -> person.getName().fullName).toList());
        } finally {
            try {
                stopSession("first", first);
            } finally {
                stopSession("second", second);
            }
        }
    }

    @Test
    public void lockHeldByAnotherProcess_rejectsSaveWithoutChangingContacts() throws Exception {
        Process owner = startSession("owner", "lock");
        try {
            awaitFile("owner.ready", owner);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(contacts);
            storage.readAddressBook();
            assertThrows(DataFileConflictException.class, () -> storage.saveAddressBook(new AddressBook()));
        } finally {
            stopSession("owner", owner);
        }
        JsonAddressBookStorage storage = new JsonAddressBookStorage(contacts);
        storage.readAddressBook();
        storage.saveAddressBook(new AddressBook());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    private Process startSession(String session, String operation) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        return new ProcessBuilder(java.toString(), "-ea", "-jar", launcher.toString(),
                temporaryFolder.toString(), session, operation)
                .directory(temporaryFolder.toFile()).redirectErrorStream(true)
                .redirectOutput(temporaryFolder.resolve(session + ".log").toFile()).start();
    }

    private void awaitFile(String fileName, Process process) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!Files.exists(temporaryFolder.resolve(fileName))) {
            assertTrue(process.isAlive(), "Application exited while waiting for " + fileName);
            assertTrue(System.nanoTime() < deadline, "Timed out waiting for " + fileName);
            Thread.sleep(25);
        }
    }

    private void stopSession(String session, Process process) throws Exception {
        Files.writeString(temporaryFolder.resolve(session + ".stop"), "");
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Application did not shut down: " + session);
            assertEquals(0, process.exitValue(), Files.readString(temporaryFolder.resolve(session + ".log")));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }
}
