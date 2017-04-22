
package ind.rubilacz.tools.acleaner.detector;

import ind.rubilacz.tools.acleaner.model.Debris;
import ind.rubilacz.tools.acleaner.model.Document;
import ind.rubilacz.tools.acleaner.model.StringDebris;

public class StringDetector extends CharactersDetector {

    private static StringDetector sDetector;

    public static synchronized StringDetector getInstance() {
        if (sDetector == null) {
            sDetector = new StringDetector();
        }

        return sDetector;
    }

    private StringDetector() {
    }

    @Override
    public String identifier() {
        return "string";
    }

    @Override
    protected Debris obtain(String name, Document container) {
        return new StringDebris(name, container);
    }

}
