import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<String> extSet = new HashSet<>();
        extSet.add("mkv");
        extSet.add("mp4");
        extSet.add("mov");
        extSet.add("avi");
        extSet.add("png");
        extSet.add("jpg");
        extSet.add("jpeg");
        extSet.add("svg");
        extSet.add("gif");
        extSet.add("bmp");
        extSet.add("pdf");
        extSet.add("doc");
        extSet.add("docx");
        extSet.add("xsl");
        extSet.add("xsls");
        extSet.add("txt");
        extSet.add("ppt");
        extSet.add("pptx");
        extSet.add("dmg");
        extSet.add("zip");
        extSet.add("rar");
        extSet.add("7z");
        extSet.add("tar");
        extSet.add("gz");
        extSet.add("mp3");
        extSet.add("wav");
        extSet.add("aac");
        extSet.add("ogg");
        try {
            Path downloadFolderPath = Paths.get(System.getProperty("user.home"), "Downloads");

            // WatcherService
            WatchService downloadWatchService = FileSystems.getDefault().newWatchService();
            // Register Download Folder for ENTRY_CREATE event
            downloadFolderPath.register(downloadWatchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("Downloads folder watch service started...");
            while (true) {
                WatchKey key = downloadWatchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
//                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
//                    System.out.println("Event type: " + kind + ", File: " + fileName);
                    // logic to handle the newly created file
                    String fileExt = getFileExtension(fileName.toString());
                    System.out.println(fileExt);
                    if (extSet.contains(fileExt)) {
                        switch (fileExt) {
                            case "mkv":
                            case "mp4":
                                moveNewFileToTargetFolder(downloadFolderPath.resolve(fileName.getFileName()), "Downloads/videos");
                                break;
                            case "png":
                            case "jpg":
                            case "jpeg":
                            case "svg":
                                moveNewFileToTargetFolder(downloadFolderPath.resolve(fileName.getFileName()),
                                        "Downloads/images");
                                break;
                            case "pdf":
                            case "docx":
                            case "doc":
                            case "xsl":
                            case "xsls":
                                moveNewFileToTargetFolder(downloadFolderPath.resolve(fileName.getFileName()),
                                        "Downloads/pdfs");
                                break;
                            case "dmg":
                                moveNewFileToTargetFolder(downloadFolderPath.resolve(fileName.getFileName()),
                                        "Downloads/dmg");
                                break;
                            default:
                                moveNewFileToTargetFolder(downloadFolderPath.resolve(fileName.getFileName()),
                                        "Downloads/misc");
                                break;
                        }
                    }

                }
                key.reset();
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void moveNewFileToTargetFolder(Path fileName, String folderPath) {
        Path targetFolderPath = Paths.get(System.getProperty("user.home"), folderPath);
        try {
            if (Files.notExists(targetFolderPath)) {
                Files.createDirectory(targetFolderPath);
            }
            Path targetFilePath = targetFolderPath.resolve(fileName.getFileName());
            Files.move(fileName, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved successfully to: " + targetFolderPath);
            // Display a notification
//            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            displayNotification("File moved successfully!", fileName.toString());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    private static void displayNotification(String title, String message) {
        SystemTray tray = SystemTray.getSystemTray();

        // Assuming you have an icon image in the same directory as your class
        Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Download Notification");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            tray.remove(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}