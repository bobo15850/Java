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

	// TODO 省略一系列查询方法，先学习添加元素的方法

	public V put(K key, V value) {
		// TODO,该方法设计红黑树的遍历，以及红黑树的调整
		Entry<K, V> t = root;
		if (root == null) {
			t = new Entry<K, V>(key, value, null);
			return null;
		}// 原先map中没有内容

		return null;
	}// 向map中添加一个元素，如果存在key则替换原先的value，返回值是原来的value值，或者null（原来不存在，新添加的key-value）

	/*
	 * 下面是静态工具方法
	 */
	// TODO

	static final boolean valEquals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);// 避免了null检查
	}// 判断两个对象是否equals，如果是两个null返回true

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

		Entry(K key, V value, Entry<K, V> parent) {
			this.key = key;
			this.value = value;
			this.parent = parent;
		}// 初始化方法

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}// 返回值是之前value的值

	}

	public Set<MyMap.Entry<K, V>> entrySet() {
		return null;
	}

}
