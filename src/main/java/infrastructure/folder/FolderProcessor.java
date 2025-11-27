package infrastructure.folder;

import java.nio.file.Files;
import java.nio.file.Path;

public class FolderProcessor {
    public static void cleanFolder(Path folder) {
        try {
            if (Files.exists(folder) && Files.isDirectory(folder)) {
                try (var files = Files.newDirectoryStream(folder)) {
                    for (Path file : files) {
                        Files.deleteIfExists(file);
                    }
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
