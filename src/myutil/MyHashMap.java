package myutil;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class MyHashMap<K, V> extends MyAbstractMap<K, V> implements MyMap<K, V>, Cloneable, Serializable {
	/*
	 * 一些列类的静态变量
	 */
	private static final long serialVersionUID = 362498820763181265L;

	// 默认的数组容量是16，数组容量必须是2的指数，因为根据计算二的指数的的散列效果最好
	static final int DEFAULT_INITIAL_CAPATICY = 1 << 4;

	// 最大的散列值，有int的限制
	static final int MAXIMUM_CAPACITY = 1 << 30;

	// 默认的加载因子，如果已经填充的条目数和总容量的比值大于该数，需要rehash将容量翻倍
	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	// 当一个条目中的节点个数大于这个值就将链表转化为红黑树
	static final int TREEIFY_THRESHOLD = 8;

	// 退树的阈值，当一个条目的节点个数小于该值则将红黑树退化为普通的链表
	static final int UNTREEIFY_THRESHOLD = 6;

	// 最小扩容容量
	static final int MIN_TREEIFY_CAPACITY = 64;

	// 存放键值对的数据结构，链表表示
	static class Node<K, V> implements MyMap.Entry<K, V> {
		final int hash;
		final K key;
		V value;
		Node<K, V> next;

		Node(int hash, K key, V value, Node<K, V> next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}

		public final K getKey() {
			return key;
		}

		public final V getValue() {
			return value;
		}

		public final String toString() {
			return key + "" + value;
		}

		// XXX 这里的抑或是什么作用，还有Node的hashCode有什么作用。。。，存取应该是使用key的hashcode呀
		public final int hashCode() {
			return Objects.hashCode(key) ^ Objects.hashCode(value);
		}

		public final V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		// 需要键值对完全相等才能判定相等
		public final boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof MyMap.Entry) {
				MyMap.Entry<?, ?> e = (MyMap.Entry<?, ?>) o;
				if (Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue())) {
					return true;
				}
			}
			return false;
		}
	}

	/*
	 * 一系列的静态方法
	 */

	// 最终得到的结果是大于cap的第一个2的指数的数
	static final int tableSizeFor(int cap) {
		int n = cap - 1;
		// 平行算法// 将n变为以原先的cap的位数，所有位上全是1
		n |= n >>> 1;
		n |= n >>> 2;
		n |= n >>> 4;
		n |= n >>> 8;
		n |= n >>> 16;
		// 最后保证table的size是2的指数
		return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
	}

	/*
	 * 实例变量，反序列化之后变成一个全新的失实例了，相当于拿原先的元素直接初始化
	 */
	transient Node<K, V>[] table;

	transient Set<MyMap.Entry<K, V>> entrySet;

	transient int size;

	transient int modCount;

	int threshold;

	final float loadFactor;

	public MyHashMap(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY) {
			initialCapacity = MAXIMUM_CAPACITY;
		}

		if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
		}
		this.loadFactor = loadFactor;
		this.threshold = tableSizeFor(initialCapacity);
	}

	public MyHashMap(int initialCapaticy) {
		this(initialCapaticy, DEFAULT_LOAD_FACTOR);
	}

	public MyHashMap() {
		this.loadFactor = DEFAULT_LOAD_FACTOR;
	}

	public MyHashMap(MyMap<? extends K, ? extends V> m) {
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		// TODO这里需要将传进来的map来初始化本实例的map
	}

	public Set<MyMap.Entry<K, V>> entrySet() {
		return null;
	}
}
