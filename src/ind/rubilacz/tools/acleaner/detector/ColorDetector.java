
package ind.rubilacz.tools.acleaner.detector;

import ind.rubilacz.tools.acleaner.model.ColorDebris;
import ind.rubilacz.tools.acleaner.model.Debris;
import ind.rubilacz.tools.acleaner.model.Document;

public class ColorDetector extends CharactersDetector {

    private static ColorDetector sDetector;

    public static synchronized ColorDetector getInstance() {
        if (sDetector == null) {
            sDetector = new ColorDetector();
        }

        return sDetector;
    }

    private ColorDetector() {
    }

    @Override
    public String identifier() {
        return "color";
    }

    @Override
    protected Debris obtain(String name, Document container) {
        return new ColorDebris(name, container);
    }

    @Override
    protected boolean shouldDirProcessDeeply(String relativePath) {
        return relativePath.matches("^res/color$") || relativePath.matches("^res\\\\color$");
    }

    @Override
    protected boolean isDeepProcessingNeeded() {
        return true;
    }

}
