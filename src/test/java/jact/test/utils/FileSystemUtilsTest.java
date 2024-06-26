package jact.test.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static jact.utils.CommandExecutor.copyPNGImage;
import static jact.utils.FileSystemUtils.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemUtilsTest {

    static String testDirectory = "./src/test/java/jact/test/testingDir/";

    @AfterAll
    public static void cleanUpTestDirs() {
        removeDirectory(new File(testDirectory));
        Assertions.assertFalse(new File(testDirectory).exists());
    }

    @Test
    /**
     * Requirements: Nothing.
     * Contract:
     *     Pre-condition:  Need to create an empty directory
     *                     or remove an empty directory.
     *     Post-condition: Creating and removing empty directories
     *                     is performed correctly.
     */
    public void createAndRemoveEmptyDirectoryTest() {
        Assertions.assertFalse(new File(testDirectory + "createdDir").exists());
        createDir(testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir").exists());

        removeDirectory(new File(testDirectory + "createdDir"));
        Assertions.assertFalse(new File(testDirectory + "createdDir").exists());
    }

    @Test
    /**
     * Requirements: Nothing.
     * Contract:
     *     Pre-condition:  A directory needs to be deleted
     *                     or a directory needs to be created.
     *     Post-condition: Creating and removing directories with content
     *                     is performed correctly.
     */
    public void createAndRemoveDirectoryTest() throws IOException {
        createDir(testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir").exists());
        Assertions.assertFalse(new File(testDirectory + "createdDir" + "/jact-logo.png").exists());
        copyPNGImage("jact-logo.png", testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir" + "/jact-logo.png").exists());

        // Recursive deletion of files contained in the directory
        removeDirectory(new File(testDirectory + "createdDir"));
        Assertions.assertFalse(new File(testDirectory + "createdDir" + "/jact-logo.png").exists());
        Assertions.assertFalse(new File(testDirectory + "createdDir").exists());
    }


    @Test
    /**
     * Requirements: An existing directory.
     * Contract:
     *     Pre-condition:  An existing directory requires moving
     *                     or copying to another directory.
     *     Post-condition: Copying and moving directories with content
     *                     is performed correctly.
     */
    public void copyAndMoveDirectoryTest() throws IOException {
        createDir(testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir").exists());
        copyPNGImage("jact-logo.png", testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir" + "/jact-logo.png").exists());

        // Recursive deletion of files contained in the directory
        createDir(testDirectory + "movedDir");
        moveDirectory(new File(testDirectory + "createdDir"), testDirectory + "movedDir");
        Assertions.assertFalse(new File(testDirectory + "createdDir" + "/jact-logo.png").exists());
        Assertions.assertFalse(new File(testDirectory + "createdDir").exists());
        assertTrue(new File(testDirectory + "/movedDir" + "/createdDir").exists());
        assertTrue(new File(testDirectory + "/movedDir" + "/createdDir" + "/jact-logo.png").exists());


        // Remove the moved directory
        removeDirectory(new File(testDirectory + "movedDir"));
        Assertions.assertFalse(new File(testDirectory + "/movedDir" + "/createdDir" + "/jact-logo.png").exists());
        Assertions.assertFalse(new File(testDirectory + "/movedDir" + "/createdDir").exists());
    }

    @Test
    /**
     * Requirements: An existing file.
     * Contract:
     *     Pre-condition:  An existing file in a directory
     *                     requires renaming.
     *     Post-condition: The file is correctly renamed
     *                     and the old one doesn't exist.
     */
    public void renameFileTest() throws IOException {
        createDir(testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir").exists());
        copyPNGImage("jact-logo.png", testDirectory + "createdDir");
        assertTrue(new File(testDirectory + "createdDir" + "/jact-logo.png").exists());

        renameFile(testDirectory + "createdDir" + "/jact-logo.png", "new-name-jact-logo.png");

        assertTrue(new File(testDirectory + "createdDir" + "/new-name-jact-logo.png").exists());

        // Remove the moved directory
        removeDirectory(new File(testDirectory + "createdDir"));
        Assertions.assertFalse(new File(testDirectory + "createdDir").exists());
    }

}
