package myutil;

public class MyLinkedHashMap<K, V> extends MyHashMap<K, V> implements MyMap<K, V> {
	static class Entry<K, V> extends MyHashMap.Node<K, V> {
		Entry<K, V> before, after;

		Entry(int hash, K key, V value, Node<K, V> next) {
			super(hash, key, value, next);
		}
	}

	private static final long serialVersionUID = 3801124242820219131L;
}
