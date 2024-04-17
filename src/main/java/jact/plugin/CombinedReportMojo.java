package jact.plugin;

import jact.depUtils.ProjectDependencies;
import jact.depUtils.ProjectDependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static jact.core.HtmlAugmenter.*;
import static jact.core.XmlAugmenter.generateXmlReport;
import static jact.utils.CommandExecutor.copyJacocoCliJar;
import static jact.utils.CommandExecutor.executeJacocoCLI;


/**
 * JACT Combined Report:
 * Generates a complete code coverage report including all
 * dependencies along with their transitive dependencies.
 * This Mojo generates both the HTML and XML reports.
 */
@Mojo(name = "combined-report", defaultPhase = LifecyclePhase.INSTALL, threadSafe = false)
public class CombinedReportMojo extends AbstractReportMojo {

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


        getLog().info("STARTING: JACT - Java Complete Coverage Tracker");
        getLog().info("JARNAME: " + getOutputJarName());
        //String outputDirectory = project.getBuild().getOutputDirectory();



        // Execute JaCoCoCLI to create the report WITH dependencies
        getLog().info("Copying the `jacococli.jar` to the project.");
        try {
            copyJacocoCliJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // XML VERSION:
        Map<String, ProjectDependency> projectDependenciesMapXML =
                ProjectDependencies.getAllProjectDependencies(getJactReportPath(), true, getDepFilterParam());

        getLog().info("Creating the complete coverage report.");
        executeJacocoCLI(getOutputJarName(), false);
        getLog().info("Organizing the complete coverage report.");
        generateXmlReport(projectDependenciesMapXML, getProjectPackagesAndClasses(), getLocalRepoPath(), getProjId());
        getLog().info("JACT: XML Report Successfully Generated!");


        Map<String, ProjectDependency> projectDependenciesMapHTML =
                ProjectDependencies.getAllProjectDependencies(getJactReportPath(), false, getDepFilterParam());
        // HTML VERSION:
        getLog().info("Creating the complete coverage report.");
        executeJacocoCLI(getOutputJarName(), true);

        getLog().info("Organizing the complete coverage report.");
        generateHtmlReport(projectDependenciesMapHTML, getProjectPackagesAndClasses(), getLocalRepoPath(), getProjId());
        getLog().info("JACT: HTML Report Successfully Generated!");
    }
}