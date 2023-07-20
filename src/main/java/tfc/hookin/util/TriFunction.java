package tfc.hookin.util;

@FunctionalInterface
public interface TriFunction<T, V, E, U> {
	U apply(T t, V v, E e);
}
