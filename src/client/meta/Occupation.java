package client.meta;

/**
 * @author izarooni
 */
public class Occupation {

    public enum Type {
        // @formatter:off
        Pharaoh,
         Undead,
          Demon,
          Human
        // @formatter:on
    }

    private final Type type;

    public Occupation(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
