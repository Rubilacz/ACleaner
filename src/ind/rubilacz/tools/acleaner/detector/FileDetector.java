
package ind.rubilacz.tools.acleaner.detector;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ind.rubilacz.tools.acleaner.model.Debris;
import ind.rubilacz.tools.acleaner.model.DrawableDebris;

public abstract class FileDetector extends Detector {

    @Override
    protected Set<Debris> catchDebris(File file) {
        Set<Debris> debris = new HashSet<Debris>();
        String name = getFileNameNoSuffix(file);

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
        descBuilder.append("totally ").append(mDebris.size()).append(" useless files, about ")
                .append(totalLength / 1024).append("kb.");
        return descBuilder.toString();
    }

}
