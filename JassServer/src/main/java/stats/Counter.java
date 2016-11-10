package stats;

public class Counter<T> {
    private T entity;
    private int count;

    public Counter() {}

    public Counter(T entity) {
        this.entity = entity;
        count = 0;
    }

    public T getEntity() {
        return entity;
    }

    public int getCount() {
        return count;
    }

    public void increment() {
        count += 1;
    }
}
