package seedu.address;

import java.nio.file.Files;
import java.nio.file.Path;

import seedu.address.commons.core.GuiSettings;

/**
 * Exercises the packaged application's real code-source lookup in a separate process without starting its UI.
 * A test-only launcher JAR references the application JAR, without any unpackaged production classes.
 */
public final class MainAppJarTestLauncher {

    private MainAppJarTestLauncher() {
    }

    /** Creates or reloads saved data using the real packaged {@code MainApp}. */
    public static void main(String[] args) throws Exception {
        MainApp app = new MainApp();
        app.init();
        verifyPackagedStorage(app);
        switch (args[0]) {
            case "create" -> createInitialData(app);
            case "reload" -> reloadAndUpdateData(app);
            default -> throw new IllegalArgumentException("Unknown operation: " + args[0]);
        }
        app.stop();
    }

    private static void verifyPackagedStorage(MainApp app) throws Exception {
        Path codeSource = Path.of(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!Files.isRegularFile(codeSource)) {
            throw new AssertionError("MainApp must be loaded from the packaged JAR: " + codeSource);
        }
        if (!app.storage.getAddressBookFilePath().equals(codeSource.getParent().resolve("data/addressbook.json"))) {
            throw new AssertionError("Contact storage must be relative to the packaged JAR.");
        }
    }

    private static void createInitialData(MainApp app) throws Exception {
        if (!app.model.getAddressBook().getPersonList().isEmpty()) {
            throw new AssertionError("The first launch must start without saved contacts.");
        }
        app.logic.execute("add r/student n/Alex Tan");
        app.logic.setGuiSettings(new GuiSettings(800, 600, 10, 20));
    }

    private static void reloadAndUpdateData(MainApp app) throws Exception {
        if (app.model.getAddressBook().getPersonList().size() != 1
                || !app.model.getAddressBook().getPersonList().getFirst().getName().fullName.equals("Alex Tan")) {
            throw new AssertionError("The second launch must reload the contact saved beside the JAR.");
        }
        if (!app.logic.getGuiSettings().equals(new GuiSettings(800, 600, 10, 20))) {
            throw new AssertionError("The second launch must reload the preferences saved beside the JAR.");
        }
        app.logic.execute("add r/student n/Ben Lim");
        app.logic.setGuiSettings(new GuiSettings(900, 650, 30, 40));
    }
}
