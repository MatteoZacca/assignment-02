package pcd.ass02.reactive_programming;

public class Dependency {
    private final String source;
    private final String target;

    public Dependency(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return source + " -> " + target;
    }

}
