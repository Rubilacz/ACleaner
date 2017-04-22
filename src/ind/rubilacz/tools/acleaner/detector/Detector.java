
package ind.rubilacz.tools.acleaner.detector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ind.rubilacz.tools.acleaner.model.CharactersDebris;
import ind.rubilacz.tools.acleaner.model.Debris;

public abstract class Detector {

    private static final Pattern PATTERN_REFERENCE_LIBS = Pattern
            .compile("^android\\.library\\.reference.*=\\s*([^\\s]*)$");

    private Pattern mPatternJava;

    private Pattern mPatternXml;

    private static String REG_EXP_COMMENT_C = "[\\t\\x0B]*/\\*[\\s\\S]*?\\*/[\\s]*?";

    private static String REG_EXP_COMMENT_JAVA = "[\\t\\x0B]*//.*?[\\n\\r]+";

    private Set<String> mProjectDirPaths;

    private char[] mCharBuf = new char[1024];

    protected Set<String> mElementNames = new LinkedHashSet<String>();

    protected Set<Debris> mDebris = new LinkedHashSet<Debris>();

    private String mProjectPath;

    private boolean mIsLogEnabled = true;

    public void detect(String projectPath) throws Exception {
        init();
        mProjectPath = projectPath;
        File projectDir = new File(projectPath);
        Set<File> allDirs = getAllRelatedProjectDirs(projectDir);
        allDirs.add(projectDir);
        mProjectDirPaths = getPaths(allDirs);
        for (File dir : allDirs) {
            detectSingleProject(dir);
        }

        for (File dir : allDirs) {
            if (isDeepProcessingNeeded()) {
                processDeeply(dir);
            }
        }
    }

    public void scan() throws Exception {
        scanDebris(new File(mProjectPath));
        mDebrisDescription = getDebrisDesc();
    }

    public void clean() {
        for (Debris debris : mDebris) {
            if (mIsLogEnabled) {
                System.out.println("eliminate debris: " + debris.toString());
            }
            debris.eliminate();
        }

        for (Debris debris : mDebris) {
            if (debris instanceof CharactersDebris) {
                ((CharactersDebris) debris).commit();
            }
        }
    }

    private String mDebrisDescription;

    public final String debrisDescription() {
        return mDebrisDescription;
    }

    protected String getDebrisDesc() {
        return "totally " + mDebris.size() + " debris.";
    }

    private void init() {
        if (mPatternJava == null) {
            mPatternJava = Pattern.compile(".*?R\\." + identifier() + "\\.([_a-zA-Z0-9]+)");
        }
        if (mPatternXml == null) {
            mPatternXml = Pattern.compile(".*?@" + identifier() + "/([_a-zA-Z0-9]+)");
        }
        mElementNames.clear();
        mDebris.clear();
    }

    private void detectSingleProject(File projectDir) throws Exception {
        if (mIsLogEnabled) {
            System.out.println("detect project: " + projectDir.getCanonicalPath());
        }
        for (File file : projectDir.listFiles()) {
            if (file.getName().equalsIgnoreCase("AndroidManifest.xml")) {
                detectTargetFile(file);
                continue;
            }
            if (!file.isDirectory()) {
                continue;
            }

            String relativePath = getRelativePath(file);
            if (shouldDirDetected(relativePath) && !shouldDirProcessDeeply(relativePath)) {
                detectTargetFile(file);
            }
        }
        if (mIsLogEnabled) {
            System.out.println("detect project: " + projectDir.getCanonicalPath() + " complete!");
        }
    }

    private void processDeeply(File projectDir) throws Exception {
        if (mIsLogEnabled) {
            System.out.println("do deeply processing on project: " + projectDir.getCanonicalPath());
        }
        for (File file : new File(projectDir, "res").listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }

            String relativePath = getRelativePath(file);
            if (shouldDirProcessDeeply(relativePath)) {
                doRealDeeplyProcessing(file);
            }
        }
        if (mIsLogEnabled) {
            System.out.println("do deeply processing on project: " + projectDir.getCanonicalPath()
                    + " complete!");
        }
    }

    private void scanDebris(File projectDir) throws Exception {
        if (mIsLogEnabled) {
            System.out.println("scan debris on project: " + projectDir.getCanonicalPath());
        }
        for (File file : projectDir.listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }

            String relativePath = getRelativePath(file);
            if (isDirLittery(relativePath)) {
                doRealDebrisScan(file);
            }
        }
        if (mIsLogEnabled) {
            System.out.println("scan debris on project: " + projectDir.getCanonicalPath()
                    + " complete!");
        }
    }

    private void doRealDebrisScan(File file) throws Exception {
        if (file.isDirectory()) {
            if (!isDirLittery(getRelativePath(file))) {
                return;
            }
            if (mIsLogEnabled) {
                System.out.println("scan debirs on dir: " + file.getCanonicalPath());
            }
            for (File subFile : file.listFiles()) {
                doRealDebrisScan(subFile);
            }
            return;
        }

        mDebris.addAll(catchDebris(file));
    }

    protected abstract Set<Debris> catchDebris(File file);

    private void doRealDeeplyProcessing(File file) throws Exception {
        if (file.isDirectory()) {
            if (!shouldDirProcessDeeply(getRelativePath(file))) {
                return;
            }
            if (mIsLogEnabled) {
                System.out.println("process dir: " + file.getCanonicalPath());
            }
            for (File subFile : file.listFiles()) {
                doRealDeeplyProcessing(subFile);
            }
            return;
        }

        String suffix = getSuffix(file.getName());
        if (!"xml".equalsIgnoreCase(suffix) || !mElementNames.contains(getFileNameNoSuffix(file))) {
            return;
        }

        if (mIsLogEnabled) {
            System.out.println("process file: " + file.getCanonicalPath());
        }
        Set<String> names = detectContent(file);
        // deal the case while a.xml reference b.xml and b.xml may be ignored
        for (String name : names) {
            File elementContainer = new File(file.getParent(), name + ".xml");
            if (elementContainer.exists()) {
                doRealDeeplyProcessing(elementContainer);
            }
        }
        mElementNames.addAll(names);
    }

    protected String getFileNameNoSuffix(File file) {
        String name = file.getName();
        int idxSuffix = name.lastIndexOf(".");
        if (idxSuffix == -1) {
            return name;
        }

        return name.substring(0, idxSuffix);
    }

    private void detectTargetFile(File file) throws Exception {
        if (file.isDirectory()) {
            String relativePath = getRelativePath(file);
            if (!shouldDirDetected(relativePath) || shouldDirProcessDeeply(relativePath)) {
                return;
            }
            if (mIsLogEnabled) {
                System.out.println("detect dir: " + file.getCanonicalPath());
            }
            for (File subFile : file.listFiles()) {
                detectTargetFile(subFile);
            }
            return;
        }

        String suffix = getSuffix(file.getName());
        if (suffix == null) {
            return;
        }
        if (shouldDirProcessDeeply(getRelativePath(file.getParentFile()))) {
            return;
        }
        if (suffix.equalsIgnoreCase("java") || suffix.equalsIgnoreCase("xml")) {
            mElementNames.addAll(detectContent(file));
        }
    }

    private boolean isJavaFile(File file) {
        return file.getName().toLowerCase().endsWith(".java");
    }

    // eliminate interruptions cause by strings quote by "xxxxxx"
    // eg. a file contains string ["/*"] or ["//"] would interrupt we to eliminate comments
    private String eliminateInterruptionsOfXmlComments(String content) {
        return content;
    }

    private String eliminateInterruptionsOfJavaComments(String content) {
        return content.replaceAll("\"((\\\\\")|[^\"])*?\"", "");
    }

    private Set<String> detectContent(File file) throws Exception {
        String content = readContent(file);

        // eliminate interruptions and comments
        if (isJavaFile(file)) {
            content = eliminateInterruptionsOfJavaComments(content);
            content = content.replaceAll(REG_EXP_COMMENT_C, "");
            content = content.replaceAll(REG_EXP_COMMENT_JAVA, "");
        } else {
            // TODO eliminate comments in style <!-- -->
            content = eliminateInterruptionsOfXmlComments(content);
        }

        // XXX realize the situation of android.R.XXX.XXX and getResource().getIdentifier()
        Pattern pattern = isJavaFile(file) ? mPatternJava : mPatternXml;
        Matcher matcher = pattern.matcher(content);

        Set<String> names = new LinkedHashSet<String>();
        while (matcher.find()) {
            if (matcher.group(1).equals("ic_close_one")) {
                System.out.println(file.getName());
            }
            if (matcher.group(1).equals("ic_close_deep_red")) {
                System.out.println(file.getName());
            }
            names.add(matcher.group(1));
        }
        return names;
    }

    public abstract String identifier();

    private String readContent(File file) throws Exception {
        BufferedReader reader = null;
        StringBuilder contentBuilder = new StringBuilder(1024);
        try {
            reader = new BufferedReader(new FileReader(file));
            int n = 0;
            while ((n = reader.read(mCharBuf)) != -1) {
                contentBuilder.append(mCharBuf, 0, n);
            }
            return contentBuilder.toString();
        } catch (Exception e) {
            throw new Exception("read file:" + file.getName() + " error!");
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private String getSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1 || index == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(index + 1, fileName.length());
    }

    private Set<String> getPaths(Set<File> files) throws Exception {
        Set<String> paths = new LinkedHashSet<String>();
        for (File file : files) {
            try {
                paths.add(file.getCanonicalPath());
            } catch (IOException e) {
                throw new Exception("parse referenced libraries' paths error!");
            }
        }

        return paths;
    }

    private String getRelativePath(File file) throws Exception {
        try {
            String path = file.getCanonicalPath();
            for (String dirPath : mProjectDirPaths) {
                if (!path.contains(dirPath)) {
                    continue;
                }

                if (path.equalsIgnoreCase(dirPath)) {
                    return ".";
                }
                return path.substring(dirPath.length() + 1, path.length());
            }
        } catch (IOException e) {
            throw new Exception("parse relative path error!");
        }

        return null;
    }

    protected boolean shouldDirDetected(String relativePath) {
        return relativePath.matches("^src.*") || relativePath.matches("^res.*");
    }

    protected boolean isDeepProcessingNeeded() {
        return true;
    }

    protected abstract boolean shouldDirProcessDeeply(String relativePath);

    protected boolean isDirLittery(String relativePath) {
        return relativePath.matches("^res.*");
    }

    private Set<File> getAllRelatedProjectDirs(File projectDir) throws Exception {
        Set<File> projectDirs = getRelatedProjectDirs(projectDir);
        for (File dir : projectDirs) {
            Set<File> relates = getAllRelatedProjectDirs(dir);
            projectDirs.addAll(relates);
        }
        projectDirs.add(projectDir);

        return projectDirs;
    }

    private String localizeRelativePath(String path) {
        StringBuilder pathBuilder = new StringBuilder();
        String[] segs = path.split("[/\\\\]");
        for (String seg : segs) {
            if (seg.trim().length() != 0) {
                pathBuilder.append(seg).append(File.separatorChar);
            }
        }
        pathBuilder.setLength(pathBuilder.length());

        return pathBuilder.toString();
    }

    private Set<File> getRelatedProjectDirs(File projectDir) throws Exception {
        Set<File> projectDirs = new LinkedHashSet<File>();
        File propFile = new File(projectDir, "project.properties");
        if (!propFile.exists()) {
            return projectDirs;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(propFile));
            String line = null;
            Matcher matcher = null;
            while ((line = reader.readLine()) != null) {
                matcher = PATTERN_REFERENCE_LIBS.matcher(line.trim());
                if (matcher.matches()) {
                    projectDirs.add(new File(projectDir, localizeRelativePath(matcher.group(1))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("parse referenced libraries error!");
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }

        return projectDirs;
    }

}
