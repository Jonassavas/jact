package jact.plugin;

import jact.depUtils.ProjectDependencies;
import jact.depUtils.ProjectDependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import static jact.core.XmlAugmenter.generateXmlReport;
import static jact.utils.CommandExecutor.copyJacocoCliJar;
import static jact.utils.CommandExecutor.executeJacocoCLI;


/**
 * JACT XML Report:
 * Generates a complete code coverage report including all
 * dependencies along with their indirect dependencies.
 */
@Mojo(name = "xml-report", defaultPhase = LifecyclePhase.INSTALL, threadSafe = false)
public class XmlReportMojo extends AbstractReportMojo {

    @Override
    public void doExecute() throws MojoExecutionException {

        // Print out packages and their classes
        getLog().info("Packages in project:");
        for (Map.Entry<String, Set<String>> entry : getProjectPackagesAndClasses().entrySet()) {
            getLog().info("- " + entry.getKey());
            for (String className : entry.getValue()) {
                getLog().info("  - " + className);
            }
        }


        getLog().info("STARTING: JACT - Java Absolute Coverage Tracker");
        getLog().info("JARNAME: " + getOutputJarName());
        //String outputDirectory = project.getBuild().getOutputDirectory();


        Map<String, ProjectDependency> projectDependenciesMap =
                ProjectDependencies.getAllProjectDependencies(getJactReportPath(), true, getDepFilterParam());

        // Execute JaCoCoCLI to create the report WITH dependencies
        getLog().info("Copying the `jacococli.jar` to the project.");
        try {
            copyJacocoCliJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        getLog().info("Creating the complete XML coverage report.");
        executeJacocoCLI(getOutputJarName(), false);
        getLog().info("Organizing the complete XML coverage report.");
        generateXmlReport(projectDependenciesMap, getProjectPackagesAndClasses(), getLocalRepoPath(), getProjId());
        getLog().info("JACT: XML Report Successfully Generated!");
    }
}