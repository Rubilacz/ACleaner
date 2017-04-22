
package ind.rubilacz.tools.acleaner.model;

import java.io.File;

public abstract class FileDebris extends Debris {

    private File mDebris;

    public FileDebris(String name, File debris) {
        super(name);
        if (debris == null || !debris.exists()) {
            throw new IllegalArgumentException("target does not exists!");
        }
        mDebris = debris;
    }

    public String path() {
        return mDebris.getPath();
    }

    public long length() {
        return mDebris.length();
    }

    @Override
    protected void destruct() {
        mDebris.delete();
    }

    @Override
    public String toString() {
        return name() + "(" + path() + ")";
    }

}
