import java.util.zip.*;
import java.io.*;

public class FindClass {
    public static void main(String[] args) throws Exception {
        File dir = new File("/Users/admin/.gradle/caches");
        search(dir);
    }
    static void search(File f) throws Exception {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) search(child);
        } else if (f.getName().endsWith(".aar") || f.getName().endsWith(".jar")) {
            if (f.getName().contains("navigation3")) {
                try (ZipFile zf = new ZipFile(f)) {
                    zf.stream().forEach(e -> {
                        if (e.getName().endsWith("NavKey.class") || e.getName().endsWith("NavBackStack.class")) {
                            System.out.println("Found " + e.getName() + " in " + f.getName());
                        } else if (e.getName().equals("classes.jar")) {
                            try (InputStream is = zf.getInputStream(e)) {
                                File temp = File.createTempFile("classes", ".jar");
                                try (FileOutputStream fos = new FileOutputStream(temp)) {
                                    is.transferTo(fos);
                                }
                                try (ZipFile inner = new ZipFile(temp)) {
                                    inner.stream().forEach(ie -> {
                                        if (ie.getName().toLowerCase().contains("navkey") || ie.getName().toLowerCase().contains("navbackstack")) {
                                            System.out.println("Found " + ie.getName() + " in " + f.getName() + " -> classes.jar");
                                        }
                                    });
                                }
                                temp.delete();
                            } catch(Exception ex) {}
                        }
                    });
                } catch(Exception e) {}
            }
        }
    }
}
