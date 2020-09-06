package ch.probyte.antgradlecheck;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class ComparePlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "ComparePlugin";
    public static final String TASK_ID = "compareTask";

    @Override
    public void apply(Project project) {
        project.getExtensions().create(PLUGIN_ID, ComparePluginConfiguration.class);

        project.getTasks().create(TASK_ID, CompareTask.class);
    }
}
