package ch.probyte.antgradlecheck;

import java.io.File;

public class ComparePluginConfiguration {
    private File targetFileAnt;
    private File targetFileGradle;

    public File getTargetFileAnt() {
        return targetFileAnt;
    }

    public void setTargetFileAnt(File targetFileAnt) {
        this.targetFileAnt = targetFileAnt;
    }

    public File getTargetFileGradle() {
        return targetFileGradle;
    }

    public void setTargetFileGradle(File targetFileGradle) {
        this.targetFileGradle = targetFileGradle;
    }
}
