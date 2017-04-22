
package ind.rubilacz.tools.acleaner.model;

public abstract class Debris {

    private String mName;

    public Debris(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("name can not be null!");
        }
        mName = name;
    }

    public String name() {
        return mName;
    }

    public final void eliminate() {
        destruct();
    }

    protected abstract void destruct();

    @Override
    public String toString() {
        return mName;
    }

}
