package jact.depUtils;

import com.google.gson.*;
import jact.plugin.AbstractReportMojo;

import java.io.FileReader;
import java.util.*;

import static jact.depUtils.ProjectDependency.depIdToDirName;
import static jact.depUtils.ProjectDependency.depToDirName;
import static jact.plugin.AbstractReportMojo.getReportPath;
import static jact.utils.CommandExecutor.generateDependencyLockfile;

/**
 * Creates all the project dependencies and their ProjectDependency objects
 * in order to calculate and write the reported usage from jacoco.
 */
public class ProjectDependencies {
    public static Map<String, ProjectDependency> projectDependenciesMap = new HashMap<>();

    public static Map<String, TransitiveDependencies> transitiveUsageMap = new HashMap<>();

    public static List<String> rootDepIds = new ArrayList<>();


    private static boolean skipTestDependencies;

    public static Map<String, ProjectDependency> getAllProjectDependencies(String targetDirectory,
                                                                           boolean genLockfile,
                                                                           boolean skipTestDeps) {

        skipTestDependencies = skipTestDeps;

        if (projectDependenciesMap.isEmpty()) {
            generateAllProjectDependencies(targetDirectory, genLockfile);
        }
        return projectDependenciesMap;
    }

    /**
     * Generate the project lockfile containing all the project dependencies
     * including their transitive dependencies and creates their corresponding
     * ProjectDependency object with child/parent dependencies.
     * @param targetDirectory
     * @param genLockfile
     */
    private static void generateAllProjectDependencies(String targetDirectory, boolean genLockfile) {
        if(genLockfile){
            generateDependencyLockfile(targetDirectory);
        }
        String filePath = targetDirectory + "lockfile.json"; // Path to the JSON file
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new GsonBuilder().registerTypeAdapter(ProjectDependency.class, new ProjectDependencyDeserializer()).create();
            JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);
            JsonArray dependenciesArray = jsonElement.getAsJsonObject().getAsJsonArray("dependencies");
            for (JsonElement dependencyElement : dependenciesArray) {
                gson.fromJson(dependencyElement, ProjectDependency.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ProjectDependencyDeserializer implements com.google.gson.JsonDeserializer<ProjectDependency> {
        @Override
        public ProjectDependency deserialize(JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) {
            JsonObject jsonObject = json.getAsJsonObject();
            Set<String> visited = new HashSet<>();
            ProjectDependency parentDep = new ProjectDependency();
            return parseDependency(jsonObject, parentDep, visited);
        }

        private void setupChildDependency(ProjectDependency projectDependency, ProjectDependency parentDep){
            projectDependency.addParentDep(parentDep);
            // Add all parent paths, a transitive dependency.
            for (String path : parentDep.getReportPaths()) {
                if(!projectDependency.getReportPaths().contains(path + "transitive-dependencies/" + depToDirName(projectDependency) + "/")){
                    projectDependency.addReportPath(path + "transitive-dependencies/" + depToDirName(projectDependency) + "/");
                }
            }
            if(!transitiveUsageMap.containsKey(depToDirName(parentDep))){
                transitiveUsageMap.put(depToDirName(parentDep), new TransitiveDependencies(parentDep));
            }else{
                transitiveUsageMap.get(depToDirName(parentDep)).addReportPaths(parentDep);
            }
        }

        private ProjectDependency parseDependency(JsonObject jsonObject, ProjectDependency parentDep, Set<String> visited) {
            String dependencyId = jsonObject.get("id").getAsString();
            String dependencyScope = jsonObject.get("scope").getAsString();
            String parentString = jsonObject.has("parent") ? jsonObject.get("parent").getAsString() : "";

            if(skipTestDependencies && dependencyScope.equals("test")){
                //Skipping test-scope dependencies
                return new ProjectDependency();
            }
            if (visited.contains(dependencyId)) {
                // If the dependency has been visited before, find it, add the parent and return it.
                ProjectDependency pd = projectDependenciesMap.get(depIdToDirName(dependencyId));
                if(parentDep.getId() != null){
                    setupChildDependency(pd, parentDep);
                }else if(!parentString.isEmpty()){
                    setupChildDependency(pd, projectDependenciesMap.get(depIdToDirName(parentString)));
                }else{
                    if(!rootDepIds.contains(dependencyId)){
                        rootDepIds.add(dependencyId);
                    }
                    pd.addReportPath(getReportPath() + "dependencies/" + depToDirName(pd) + "/");
                }
                return pd;
            }
            visited.add(dependencyId);

            ProjectDependency projectDependency = new ProjectDependency();
            projectDependency.setId(jsonObject.has("id") ? jsonObject.get("id").getAsString() : "");
            projectDependency.setGroupId(jsonObject.has("groupId") ? jsonObject.get("groupId").getAsString() : "");
            projectDependency.setArtifactId(jsonObject.has("artifactId") ? jsonObject.get("artifactId").getAsString() : "");
            projectDependency.setVersion(jsonObject.has("selectedVersion") ? jsonObject.get("selectedVersion").getAsString() : "");
            projectDependency.setScope(jsonObject.has("scope") ? jsonObject.get("scope").getAsString() : "");



            // Adding the directory name to the potential paths to report the usage
            // Build the report path
            if(parentDep.getId() != null){
                setupChildDependency(projectDependency, parentDep);
            }else if(!parentString.isEmpty()){
                setupChildDependency(projectDependency, projectDependenciesMap.get(depIdToDirName(parentString)));
            }else{
                if(!rootDepIds.contains(dependencyId)){
                    rootDepIds.add(dependencyId);
                }
                projectDependency.addReportPath(getReportPath() + "dependencies/" + depToDirName(projectDependency) + "/");
            }

            //System.out.println("ADDING: " + projectDependency.toString());
            projectDependenciesMap.put(depToDirName(projectDependency), projectDependency);
            JsonArray childrenJsonArray = jsonObject.getAsJsonArray("children");
            if (childrenJsonArray != null) {
                for (JsonElement element : childrenJsonArray) {
                    ProjectDependency child = parseDependency(element.getAsJsonObject(), projectDependency, visited);
                    projectDependency.addChildDep(child);
                }
            }
            return projectDependency;
        }
    }

}