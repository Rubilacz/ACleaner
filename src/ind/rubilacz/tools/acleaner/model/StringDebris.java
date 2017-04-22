
package ind.rubilacz.tools.acleaner.model;

import ind.rubilacz.tools.acleaner.detector.StringDetector;

public class StringDebris extends CharactersDebris {

    public StringDebris(String name, Document container) {
        super(name, container);
    }

    @Override
    public String identifier() {
        return StringDetector.getInstance().identifier();
    }

}
