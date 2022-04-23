package dzuchun.util;

public class Pair<K, V> {
	public K key;
	public V value;

	public Pair(K keyIn, V valueIn) {
		this.key = keyIn;
		this.value = valueIn;
	}
}
