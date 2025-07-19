import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class MyMacDownloadsFolderMonitor {

    private static final Map<String, String> EXT_FOLDER_MAP = new HashMap<>();

    static {
        // Videos
        for (String ext : new String[]{"mp4", "mkv", "mov", "avi"}) EXT_FOLDER_MAP.put(ext, "videos");
        // Images
        for (String ext : new String[]{"png", "jpg", "jpeg", "svg", "gif", "bmp"}) EXT_FOLDER_MAP.put(ext, "images");
        // Documents
        for (String ext : new String[]{"pdf", "doc", "docx", "xsl", "xsls", "txt", "ppt", "pptx"})
            EXT_FOLDER_MAP.put(ext, "pdfs");
        // Executables
        for (String ext : new String[]{"dmg", "pkg", "app"}) EXT_FOLDER_MAP.put(ext, "mexe");
        // Archives
        for (String ext : new String[]{"zip", "rar", "7z", "tar", "gz"}) EXT_FOLDER_MAP.put(ext, "misc");
        // Audio
        for (String ext : new String[]{"mp3", "wav", "aac", "ogg"}) EXT_FOLDER_MAP.put(ext, "misc");
    }

    public static void main(String[] args) {
        Path downloadFolder = Paths.get(System.getProperty("user.home"), "Downloads");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            downloadFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("📁 Monitoring Downloads folder...");

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();

                    // Skip hidden files or temporary files
                    if (fileName.toString().startsWith(".") || fileName.toString().endsWith(".crdownload"))
                        continue;

                    String extension = getFileExtension(fileName.toString()).toLowerCase();
                    String targetFolder = EXT_FOLDER_MAP.getOrDefault(extension, "misc");

                    Path fullFilePath = downloadFolder.resolve(fileName);
                    waitForFileToBeReady(fullFilePath);

                    moveFileToTargetFolder(fullFilePath, targetFolder);
                }

                if (!key.reset()) {
                    System.out.println("❌ WatchKey could not be reset. Exiting...");
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < fileName.length() - 1) ? fileName.substring(dotIndex + 1) : "";
    }

    private static void waitForFileToBeReady(Path filePath) throws InterruptedException {
        long size = -1;
        while (true) {
            if (Files.exists(filePath)) {
                long newSize = filePath.toFile().length();
                if (newSize == size && newSize > 0) break;
                size = newSize;
            }
            Thread.sleep(500);
        }
    }

    private static void moveFileToTargetFolder(Path filePath, String folderName) {
        Path targetDir = Paths.get(System.getProperty("user.home"), "Downloads", folderName);
        try {
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(filePath.getFileName());
            Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ " + filePath.getFileName() + " moved to " + folderName);
            showNotification("Moved: " + filePath.getFileName());
        } catch (IOException e) {
            System.err.println("❌ Failed to move " + filePath.getFileName());
            e.printStackTrace();
        }
    }

    private static void showNotificationForMac(String message) {
        try {
            String script = String.format("display notification \"%s\" with title \"File Moved\"", message.replace("\"", "'"));
            new ProcessBuilder("osascript", "-e", script).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showNotification(String message) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); // Use a valid image path
                TrayIcon trayIcon = new TrayIcon(image, "Download Watcher");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage("File Moved", message, TrayIcon.MessageType.INFO);
                Thread.sleep(1000); // Give it time to show before removing
                tray.remove(trayIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showNotificationForMac(message);
        }
    }
}
