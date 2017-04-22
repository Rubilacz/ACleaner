
package ind.rubilacz.tools.acleaner.model;

import ind.rubilacz.tools.acleaner.detector.ColorDetector;

public class ColorDebris extends CharactersDebris {

    public ColorDebris(String name, Document container) {
        super(name, container);
    }

    @Override
    public String identifier() {
        return ColorDetector.getInstance().identifier();
    }

}
