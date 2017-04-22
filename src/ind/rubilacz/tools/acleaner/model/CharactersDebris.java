
package ind.rubilacz.tools.acleaner.model;

public abstract class CharactersDebris extends Debris {

    private Document mContainer;

    public CharactersDebris(String name, Document container) {
        super(name);
        if (container == null) {
            throw new IllegalArgumentException("container can not be null!");
        }
        mContainer = container;
    }

    protected abstract String identifier();

    protected String description() {
        return "\\s*<\\s*" + identifier() + "\\s*name\\s*=\\s*\"" + name() + "\"\\s*>.*</\\s*"
                + identifier() + "\\s*>+?";
    }

    @Override
    protected void destruct() {
        mContainer.eliminate(description());
    }

    public void commit() {
        mContainer.save();
    }

    @Override
    public String toString() {
        return name() + "(" + mContainer.getSource() + ")";
    }

}
