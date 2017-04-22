
package ind.rubilacz.tools.acleaner.detector;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ind.rubilacz.tools.acleaner.model.Debris;
import ind.rubilacz.tools.acleaner.model.DrawableDebris;

public class DrawableDetector extends FileDetector {

    private static DrawableDetector sDetector;

    public static synchronized DrawableDetector getInstance() {
        if (sDetector == null) {
            sDetector = new DrawableDetector();
        }

        return sDetector;
    }

    private DrawableDetector() {
    }

    @Override
    public String identifier() {
        return "drawable";
    }

    @Override
    protected boolean shouldDirProcessDeeply(String relativePath) {
        return relativePath.matches("^res/drawable$") || relativePath.matches("^res\\\\drawable$");
    }

    @Override
    protected boolean isDirLittery(String relativePath) {
        return relativePath.matches("^res$") || relativePath.matches("^res/drawable.*")
                || relativePath.matches("^res\\\\drawable.*");
    }

    @Override
    protected Set<Debris> catchDebris(File file) {
        Set<Debris> debris = new HashSet<Debris>();
        String name = getFileNameNoSuffix(file);
        if (name.contains(".9")) {
            name = name.substring(0, name.lastIndexOf(".9"));
        }

        if (!mElementNames.contains(name)) {
            debris.add(new DrawableDebris(name, file));
        }
        return debris;
    }

    @Override
    public String getDebrisDesc() {
        int totalLength = 0;
        for (Debris debris : mDebris) {
            totalLength += ((DrawableDebris) debris).length();
        }

        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("totally ").append(mDebris.size()).append(" useless drawables, about ")
                .append(totalLength / 1024).append("kb.");
        return descBuilder.toString();
    }

}
