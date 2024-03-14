package jact;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static jact.JacocoHTMLAugmenter.REPORTPATH;

public class PackageToDependencyResolver {
    /**
     * Take a list of all dependencies,
     * add the package to that dependency
     * Create a project 'dependency' for
     * all project packages
     */


    // Handle the scenario where two or more packages have the same name:


    public static ProjectDependency packageToDepPaths(String packageName, List<ProjectDependency> dependencies) {
        // List of dependencies along with their coordinates

        //List<ProjectDependency> currMatchedDeps = new ArrayList<>();
        Map<String, Set<String>> projectPackages = CompleteCoverageMojo.getProjectPackagesAndClasses();

        Boolean packageNameInProject = projectPackages.containsKey(packageName);

        //List<ProjectDependency> dependencies = ProjectDependencies.getAllProjectDependencies();

        // Directory where your Maven dependencies are stored
        String mavenRepositoryDir = CompleteCoverageMojo.getLocalRepoPath();

        ProjectDependency matchedDep = new ProjectDependency();

        // Iterate over each dependency
        for (ProjectDependency dependency : dependencies) {
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String version = dependency.getVersion();

            // Construct the path to the JAR file
            String jarFilePath = mavenRepositoryDir + "/" + groupId.replace('.', '/') +
                    "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
            File jarFile = new File(jarFilePath);

            // Check if the JAR file exists
            if (jarFile.exists()) {
                try (ZipFile zipFile = new ZipFile(jarFile)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        // Check if the entry is a class file within the desired package
                        if (entry.getName().startsWith(packageName.replace('.', '/')) && entry.getName().endsWith(".class")) {
                            System.out.println("Package: " + packageName + " matched to dependency: " + groupId + ":" + artifactId + ":" + version);
                            //currMatchedDeps.add(dependency);
                            matchedDep = dependency;
                            break;
                            //return dependency;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(matchedDep.getId() != null && packageNameInProject){
            // Extract the classes in the project and put them in a separate directory
            String projectPath = REPORTPATH + "/" + packageName;
            throw new RuntimeException("CANNOT RESOLVE PACKAGES: Package name: " + packageName +
                    " has an identical name to a package in " + matchedDep.getId());
        }
        // Handle the case when multiple dependencies has been matched
        // Here I need to create a new directory for those classes
        // that come from different dependencies


        //return currMatchedDeps;
        return matchedDep;
    }
}

