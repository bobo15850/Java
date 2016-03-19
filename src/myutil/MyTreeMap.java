package myutil;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;

/*
 * 通过树来实现map
 */
public class MyTreeMap<K, V> extends MyAbstractMap<K, V> implements Cloneable, Serializable {
	private static final long serialVersionUID = -2987251376526992040L;

	private final Comparator<? super K> comparator;// 比较器，不可变，只能在初始化的时候指定

	private transient Entry<K, V> root;// 根节点

	private transient int size = 0;// 随时维护该值

	private transient int modCount = 0;

	public MyTreeMap() {
		comparator = null;
	}// 默认构造器，使用默认比较器

	public MyTreeMap(Comparator<? super K> comparator) {
		this.comparator = comparator;
	}// 使用一个比较器初始化一个map

	public MyTreeMap(MyMap<? extends K, ? extends V> map) {
		this.comparator = null;// 使用自然顺序比较器
		putAll(map);
	}// 使用一个常规map初始化treemap

	public MyTreeMap(SortedMap<K, ? extends V> m) {
		this.comparator = m.comparator();
		// TODO 建立treemap
	}

	public int size() {
		return size;
	}// 返回键值对的多少

	private static final boolean RED = false;
	private static final boolean BLACK = true;

	/*
	 * 红黑树的节点
	 */
	static final class Entry<K, V> implements MyMap.Entry<K, V> {
		K key;
		V value;
		Entry<K, V> left;
		Entry<K, V> right;
		Entry<K, V> parent;
		boolean color = BLACK;// 初始化时都是黑色

		@Override
		public K getKey() {
			return null;
		}

		@Override
		public V getValue() {
			return null;
		}

		@Override
		public V setValue(V value) {
			return null;
		}

	}

	public Set<MyMap.Entry<K, V>> entrySet() {
		return null;
	}

}
