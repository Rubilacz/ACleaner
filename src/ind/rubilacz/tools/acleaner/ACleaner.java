
package ind.rubilacz.tools.acleaner;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import ind.rubilacz.tools.acleaner.detector.Detector;

public class ACleaner {

    public static void main(String[] args) throws Exception {
        Set<Class<?>> detectorClazz = new HashSet<Class<?>>();
        Set<Class<?>> clazz = getClazzInPackage(Detector.class.getPackage().getName());
        for (Class<?> cls : clazz) {
            if (Detector.class.isAssignableFrom(cls)
                    && ((cls.getModifiers() & Modifier.ABSTRACT) == 0)) {
                detectorClazz.add(cls);
            }
        }

        Set<Detector> detectors = new HashSet<Detector>();
        for (Class<?> detectorClass : detectorClazz) {
            Method mtd = detectorClass.getMethod("getInstance", (Class<?>[]) null);
            detectors.add((Detector) mtd.invoke((Object) null, (Object[]) null));
        }

        for (Detector detector : detectors) {
            detector.detect(args[0]);
            detector.scan();
            detector.clean();
            System.out.println(detector.debrisDescription());
        }
    }

    public static Set<Class<?>> getClazzInPackage(String pkgName) throws Exception {
        Set<Class<?>> clazz = new HashSet<Class<?>>();
        File destDir = new File(new File(".").getCanonicalPath() + File.separator + "src"
                + packageToRelativePath(pkgName));
        File[] clsFiles = destDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase().endsWith(".java");
            }

        });

        Class<?> cls = null;
        for (File clsFile : clsFiles) {
            if (clsFile.isDirectory()) {
                continue;
            }
            cls = Class.forName(pkgName + "." + clsFile.getName().split("\\.")[0]);
            clazz.add(cls);
        }

        return clazz;
    }

    public static String packageToRelativePath(String pkgName) {
        String[] segs = pkgName.split("\\.");
        StringBuffer pathBuilder = new StringBuffer();
        for (String seg : segs) {
            pathBuilder.append(File.separator).append(seg);
        }

        return pathBuilder.toString();
    }

}
