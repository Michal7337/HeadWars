package win.codingboulder.headWars.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Pair<L, R> {

    private L left;
    private R right;
    
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> @NotNull Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L left() {
        return left;
    }

    public void left(L left) {
        this.left = left;
    }

    public R right() {
        return right;
    }

    public void right(R right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

}
