package jact.depUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Resolves a package name to a dependency in the local .m2 folder.
 */
public class PackageToDependencyResolver {

    public static ProjectDependency packageToDependency(String packageName, Map<String, ProjectDependency> dependenciesMap,
                                                        Map<String, Set<String>> projPackagesAndClassMap, String localRepoPath) {

        boolean packageNameInProject = projPackagesAndClassMap.containsKey(packageName);
        boolean foundDep = false;
        ProjectDependency matchedDep = new ProjectDependency();

        // Iterate over each dependency
        for (ProjectDependency dependency : dependenciesMap.values()) {
            if (foundDep) {
                break;
            }
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String version = dependency.getVersion();

            // Construct the path to the JAR file
            String jarFilePath = localRepoPath + "/" + groupId.replace('.', '/') +
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
                            //System.out.println("Package: " + packageName + " matched to dependency: " + groupId + ":" + artifactId + ":" + version);
                            matchedDep = dependency;
                            foundDep = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (matchedDep.getId() != null && packageNameInProject) {
            // Extract the classes in the project and put them in a separate directory
            System.out.println("Package name: " + packageName +
                    " has an identical name to a package in " + matchedDep.getId());
            return new ProjectDependency();
        } else if (matchedDep.getId() == null && !packageNameInProject) {
            // Usually a problem with a runtime dependency required by a test-dependency.
            // Which jacoco occasionally includes. Remove it.
            //System.out.println("CANNOT MATCH PACKAGE TO ANY DEPENDENCY: " + packageName);
        }
        return matchedDep;
    }
}
