package ch.probyte.antgradlecheck;

import kotlin.Triple;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CompareTask extends DefaultTask {

    @TaskAction
    public void run() {
        ComparePluginConfiguration comparePlugin = (ComparePluginConfiguration) getProject().getExtensions().getByName(ComparePlugin.PLUGIN_ID);

        try {
            assertZipEquals(comparePlugin.getTargetFileAnt(), comparePlugin.getTargetFileGradle());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void assertZipEquals(File archiveAnt, File archiveGradle) throws IOException {
        // Get Archives
        try (ZipFile zipFile1 = new ZipFile(archiveAnt); ZipFile zipFile2 = new ZipFile(archiveGradle)) {
            // Get Member Hash
            Archive ant = new Archive(archiveAnt.getAbsolutePath(), zipFile1, getMembers(zipFile1));
            Archive gradle = new Archive(archiveGradle.getAbsolutePath(), zipFile2, getMembers(zipFile2));
            // Compare Files
            assertMembersEqual(ant, gradle);
        }
    }

    private static final class Archive {
        String file;
        ZipFile zipFile;
        Map<String, ZipEntry> files;

        public Archive(String file, ZipFile zipFile, Map<String, ZipEntry> files) {
            this.file = file;
            this.zipFile = zipFile;
            this.files = files;
        }
    }

    private static HashMap<String, ZipEntry> getMembers(ZipFile archive) {
        HashMap<String, ZipEntry> map = new HashMap<>();

        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) archive.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            map.put(entry.getName(), entry);
        }
        return map;
    }

    private static void assertMembersEqual(Archive ant, Archive gradle) throws IOException {
        System.out.println("********************************************************************************");
        System.out.println("Analyse:");
        System.out.println("ANT:\t\t" + ant.file);
        System.out.println("Gradle:\t\t" + gradle.file);
        System.out.println("********************************************************************************");
        System.out.println("Anzahl Dateien:");
        boolean anzahlVergleichOk = true;
        System.out.println("ANT:\t\t\t" + ant.files.size());
        System.out.println("Gradle:\t\t\t" + gradle.files.size());
        anzahlVergleichOk = (ant.files.size() == gradle.files.size());
        System.out.println("Identisch:\t\t" + anzahlVergleichOk);
        System.out.println("********************************************************************************");
        System.out.println("Dateien:");
        boolean dateienVergleichOk = true;
        System.out.println("Fehlende Dateien in Gradle-Archiv:");
        for (String key : ant.files.keySet()) {
            if (!gradle.files.containsKey(key)) {
                System.out.println("- " + key);
                dateienVergleichOk = false;
            }
        }
        System.out.println("Fehlende Dateien in ANT-Archiv:");
        for (String key : gradle.files.keySet()) {
            if (!ant.files.containsKey(key)) {
                System.out.println("- " + key);
                dateienVergleichOk = false;
            }
        }
        System.out.println("Identisch:\t\t" + dateienVergleichOk);
        System.out.println("********************************************************************************");
        System.out.println("Dateiinhalte:");
        boolean dateiInhaltVergleichOk = true;

        System.out.println("Ungleiche Dateien:");
        List<Triple<String, String, String>> dateiInhalte = new ArrayList<>();
        for (String key : ant.files.keySet()) {
            String antContent = IOUtils.toString(ant.zipFile.getInputStream(ant.files.get(key)), StandardCharsets.UTF_8);
            String gradleContent = IOUtils.toString(gradle.zipFile.getInputStream(gradle.files.get(key)), StandardCharsets.UTF_8);

            if (!antContent.equals(gradleContent)) {
                System.out.println("- " + key);
                dateiInhalte.add(new Triple<>(key, antContent, gradleContent));
                dateiInhaltVergleichOk = false;
            }
        }
        System.out.println("Identisch:\t\t" + dateiInhaltVergleichOk);
        System.out.println("Unterschiede:\t\t" + dateiInhaltVergleichOk);
        dateiInhalte.forEach(t -> {
            System.out.println("\t" + t.component1());
            System.out.println("\tAnt:\n" + t.component2());
            System.out.println("\tGradle:\n" + t.component3());
            System.out.println("...");
        });
        System.out.println("********************************************************************************");
        if (!(anzahlVergleichOk && dateienVergleichOk && dateiInhaltVergleichOk)) {
            throw new GradleException("Archive nicht identisch");
        }
        System.out.println("********************************************************************************");
    }
}
