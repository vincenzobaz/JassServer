package stats;

/**
 * @param <T> the type of the key.
 * @param <V> the type of the value.
 * @author vincenzobaz
 *         <p>
 *         Class representing a Tuple. This class is needed in UserStats to associate a date to a counter
 *         or a date to a rank that have to be stored in a data structure which is ordered and iterable.
 */
class Tuple2<T, V> {
    private T k;
    private V v;

    /**
     * Constructor
     *
     * @param key
     * @param value
     */
    Tuple2(T key, V value) {
        this.k = key;
        this.v = value;
    }

    /**
     * Empty constructor for Firebase Serialization
     */
    public Tuple2() {
    }

    /**
     * @return the key of the tuple.
     */
    public T getKey() {
        return k;
    }

    /**
     * @return the value of the tuple
     */
    public V getValue() {
        return v;
    }

    /**
     * Changes the value of the tuple to the received value.
     *
     * @param newValue the new value of the tuple
     * @return the current object, for method chaining.
     */
    Tuple2<T, V> setValue(V newValue) {
        this.v = newValue;
        return this;
    }
}
