package jact.utils;

import jact.plugin.AbstractReportMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class CommandExecutor {
    public static String hostOS = System.getProperty("os.name").toLowerCase();

    /**
     * Copies the jacoco cli jar for generating the jacoco
     * report before augmenting it.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void copyJacocoCliJar() throws IOException, URISyntaxException {
        // Get the path to the plugin JAR file
        Path pluginJarPath = Paths.get(AbstractReportMojo.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        // Create a JarInputStream to read from the plugin JAR
        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(pluginJarPath.toFile()))) {
            // Loop through all entries in the plugin JAR
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                // Look for jacococli.jar entry
                if (jarEntry.getName().equals("jacococli.jar")) {
                    // Prepare the target directory
                    Path targetDirectory = Paths.get("target", "jact-resources");
                    Files.createDirectories(targetDirectory);

                    // Define the target file path
                    Path targetPath = targetDirectory.resolve("jacococli.jar");

                    // Copy the entry to the target directory
                    try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = jarInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                    break; // Exit loop once jacococli.jar is found and copied
                }
            }
        }
    }

    /**
     * Copies the jacoco report.dtd file containing
     * the jacoco xml report specification for verifying
     * the JACT XML report.
     *
     * @param filename
     * @param destinationDirectory
     * @throws IOException
     */
    public static void copyDtdFile(String filename, String destinationDirectory) throws IOException {
        // Load the DTD file from the plugin's resources/xml-resources directory
        ClassLoader classLoader = CommandExecutor.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("xml-resources/" + filename);

        if (inputStream == null) {
            throw new IOException("File not found in plugin resources: " + filename);
        }

        // Create the destination directory if it doesn't exist
        File destinationDir = new File(destinationDirectory);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // Construct the destination path
        Path destinationPath = new File(destinationDirectory, filename).toPath();

        // Copy the DTD file to the destination directory
        try (OutputStream outputStream = new FileOutputStream(destinationPath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("DTD file copied successfully to: " + destinationPath);
    }

    /**
     * Copies the JACT logo for the HTML report.
     *
     * @param filename
     * @param destinationDirectory
     * @throws IOException
     */
    public static void copyPNGImage(String filename, String destinationDirectory) throws IOException {
        // Check if the filename ends with .png
        if (!filename.toLowerCase().endsWith(".png")) {
            throw new IllegalArgumentException("File is not a PNG image.");
        }

        // Load the image from src/main/resources folder
        ClassLoader classLoader = CommandExecutor.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filename);

        if (inputStream == null) {
            throw new IOException("File not found in resources: " + filename);
        }

        // Create the destination directory if it doesn't exist
        File destinationDir = new File(destinationDirectory);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // Construct the destination path
        Path destinationPath = new File(destinationDirectory, filename).toPath();

        // Copy the image to the destination directory
        try (OutputStream outputStream = new FileOutputStream(destinationPath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("PNG image copied successfully to: " + destinationPath);
    }

    /**
     * Generates the dependency lockfile for identifying
     * all dependencies in the project and their heritage.
     *
     * @param targetDirectory
     */
    public static void generateDependencyLockfile(String targetDirectory) {
        try {
            // Command to be executed
            String command = "mvn io.github.chains-project:maven-lockfile:generate -Dreduced=true";

            // Adapts the command based on OS:
            ProcessBuilder processBuilder;
            if (hostOS.contains("linux")) {
                processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            } else if (hostOS.contains("windows")) {
                processBuilder = new ProcessBuilder("cmd", "/c", command); // For Windows
            } else {
                // No support for macOS currently
                throw new RuntimeException("Could not identify operating system for lock file generation.");
            }

            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Get the input stream of the process
            InputStream inputStream = process.getInputStream();

            // Read the output
            String output = readInputStream(inputStream);

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Print the output
            System.out.println("Exit Code: " + exitCode);
            System.out.println("Output:\n" + output);

            // Move the generated lockfile.json to ./target/jact-report/lockfile.json
            File sourceFile = new File("./lockfile.json");
            File targetDir = new File(targetDirectory);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            File targetFile = new File(targetDir, "lockfile.json");

            if (sourceFile.renameTo(targetFile)) {
                System.out.println("File moved successfully to " + targetFile.getAbsolutePath());
            } else {
                System.out.println("Failed to move the file");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        // Read the input stream and convert it to a string
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }


    public static void executeJacocoCLI(String jarName, boolean htmlReport) throws MojoExecutionException {
        try {
            // Retrieve the URL to the jacococli.jar file
            // Command to execute Jacoco CLI
            String command;
            if (htmlReport) {
                command = String.format("java -jar ./target/jact-resources/jacococli.jar report ./target/jacoco.exec " +
                        "--classfiles " + "./target/" + jarName + ".jar --html ./target/jact-report");
            } else {
                // XML report:
                command = String.format("java -jar ./target/jact-resources/jacococli.jar report ./target/jacoco.exec " +
                        "--classfiles " + "./target/" + jarName + ".jar --xml ./target/jact-report/jacoco_report.xml");
            }


            // Adapts the command based on OS:
            ProcessBuilder processBuilder;
            if (hostOS.contains("linux")) {
                processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            } else if (hostOS.contains("windows")) {
                processBuilder = new ProcessBuilder("cmd", "/c", command); // For Windows
            } else {
                // No support for MacOS currently
                throw new RuntimeException("Could not identify operating system for lock file generation.");
            }

            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // If Jacoco CLI execution fails
            if (exitCode != 0) {
                throw new MojoExecutionException("Failed to execute Jacoco CLI");
            }

            // Print the output
            InputStream inputStream = process.getInputStream();
            String output = readInputStream(inputStream);
            copyPNGImage("jact-logo.png", "./target/jact-report/jacoco-resources");
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Error executing Jacoco CLI", e);
        }
    }
}
