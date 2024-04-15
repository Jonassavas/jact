package jact.depUtils;

import java.io.IOException;
import java.util.*;

import static jact.core.HtmlAugmenter.writeHTMLStringToFile;

/**
 * Tracks the usage of a dependency.
 */
public class ProjectDependency {
    public DependencyUsage dependencyUsage = new DependencyUsage();
    public Map<String, DependencyUsage> packageUsageMap = new HashMap<>();
    public boolean writtenEntryToFile = false;
    public boolean writtenTransitive = false;
    public boolean writtenTotalToFile = false;
    private String id;
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;

    public boolean rootDep = false;
    //private List<ProjectDependency> children = new ArrayList<>();
    //private List<ProjectDependency> parents = new ArrayList<>();

    private Map<String, ProjectDependency> children = new HashMap<>();
    private Map<String, ProjectDependency> parents = new HashMap<>();

    private String raportPath;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void addChildDep(ProjectDependency child) {
        if(!this.children.containsKey(child.getId())){
            this.children.put(child.getId(), child);
        }
    }

    public Map<String, ProjectDependency> getChildDeps() {
        return this.children;
    }

    public void addParentDep(ProjectDependency parent) {
        if(!this.parents.containsKey(parent.getId())){
            this.parents.put(parent.getId(), parent);
        }
    }

    public Map<String, ProjectDependency> getParentDeps() {
        return this.parents;
    }

    public void setReportPath(String reportPath) {
        this.raportPath = reportPath;
    }

    public String getReportPath() {
        return this.raportPath;
    }


    @Override
    public String toString() {
        return "{" +
                "id='" + this.getId() + '\'' +
                "groupId='" + this.getGroupId() + '\'' +
                ", artifactId='" + this.getArtifactId() + '\'' +
                ", version='" + this.getVersion() + '\'' +
                ", scope='" + this.getScope() + '\'' +
                ", children=[" + childrenToString() + ']' +
                ", parents=[" + parentsToString() + ']' +
                '}';
    }

    private String childrenToString() {
        StringBuilder sb = new StringBuilder();
        if (!this.getChildDeps().isEmpty()) {
            boolean comma = false;
            for(ProjectDependency child : this.getChildDeps().values()){
                if (comma) {
                    sb.append(", ");
                }
                comma = true;
                sb.append(child.getId());
            }
            return sb.toString();
        }
        return "";
    }

    private String parentsToString() {
        if (this.getParentDeps().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean comma = false;
        for(ProjectDependency parent : this.getParentDeps().values()){
            if (comma) {
                sb.append(", ");
            }
            comma = true;
            sb.append(parent.getId());
        }
        return sb.toString();
    }

    public void writePackagesToFile(String path, DependencyUsage total) {
        // Iterate through the map entries
        for (Map.Entry<String, DependencyUsage> entry : this.packageUsageMap.entrySet()) {
            try {
                writeHTMLStringToFile(path + "/index.html", entry.getValue().usageToHTML(entry.getKey(), total, true, false));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Gets the corresponding directory name for a dependency.
     * @param dependency
     * @return String
     */
    public static String depToDirName(ProjectDependency dependency) {
        return dependency.getGroupId().replace("-", ".") + "." +
                dependency.getArtifactId().replace("-", ".") + "-v" + dependency.getVersion();
    }

    public static String depIdToDirName(String depId) {
        String[] split = depId.split(":");
        return split[0].replace("-", ".") + "." +
                split[1].replace("-", ".") + "-v" + split[2];
    }

//    public boolean presentReportPath(String reportPath){
//        boolean present = false;
//        for(String path : this.getReportPaths()){
//            if(path.equals(reportPath)){
//                present = true;
//                break;
//            }
//        }
//        return present;
//    }

}
