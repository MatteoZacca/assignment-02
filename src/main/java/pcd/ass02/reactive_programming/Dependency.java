package pcd.ass02.reactive_programming;

import java.util.Objects;

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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Dependency that = (Dependency) obj;
        return Objects.equals(source, that.source) && Objects.equals(target, that.target);
    }

    @Override
    public String toString() {
        return source + " -> " + target;
    }
}
