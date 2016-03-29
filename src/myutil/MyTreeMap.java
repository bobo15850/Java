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

	// 比较器，不可变，只能在初始化的时候指定,treemap中的顺序由key的顺序来确定 //
	// TODO,为什么不直接在entry中继承comparable接口
	private final Comparator<? super K> comparator;

	private transient Entry<K, V> root;// 根节点

	private transient int size = 0;// 随时维护该值

	private transient int modCount = 0;// 树的修改次数（添加，刪除操作次数）

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
		// TODO 利用已有的map建立treemap
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
			size = 1;
			modCount++;
			return null;
		}// 原先map中没有内容,是空的

		int cmp;// 比较结果暂存
		Entry<K, V> parent;// 父节点
		Comparator<? super K> cpr = comparator;// 比较器

		// 以下使用二分比较的方式，找到应该添加的地方
		if (cpr != null) {
			do {
				parent = t;
				cmp = cpr.compare(key, t.key);
				if (cmp < 0) {
					t = t.left;
				}
				else if (cmp > 0) {
					t = t.right;
				}
				else {
					return t.setValue(value);// 已经存在了键，只需要替换对应的值
				}
			} while (t != null);
		}// 使用初始化时传入的比较器
		else {
			if (key == null) {
				throw new NullPointerException();
			}// 当没有指定比较器时treemap的键不鞥为空

			// 此处限定了如果没有指定比较器的时候key必须实现comparable接口
			@SuppressWarnings("unchecked")
			Comparable<? super K> k = (Comparable<? super K>) key;
			do {
				parent = t;// 用parent作为t的保留值，使得t为null的时候t之前的值存在parent中
				cmp = k.compareTo(t.key);
				if (cmp < 0) {
					t = t.left;
				}
				else if (cmp > 0) {
					t = t.right;
				}
				else {
					return t.setValue(value);
				}
			} while (t != null);
		}// 使用key的compareTo方法进行比较，所以key要实现comparable接口
		Entry<K, V> e = new Entry<K, V>(key, value, parent);
		if (cmp < 0) {
			parent.left = e;
		}
		else {
			parent.right = e;
		}

		// TODO要进行红黑树的调整，使其符合红黑树的性质

		size++;
		modCount++;
		return null;// 新添加键值对返回null

	}// 向map中添加一个元素，如果存在key则替换原先的value，返回值是原来的value值，或者null（原来不存在，新添加的key-value）

	/*
	 * 下面是静态工具方法
	 */
	// TODO

	static final boolean valEquals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);// 避免了null检查
	}// 判断两个value是否equals，如果是两个null返回true

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

		public boolean equals(Object o) {
			if (!(o instanceof MyMap.Entry)) {
				return false;
			}
			// 此处只能用通配符?匹配，用为类型擦除之后只能判断o是entry，并不能知道泛型类型
			MyMap.Entry<?, ?> e = (MyMap.Entry<?, ?>) o;

			// 只有两个节点的key和value都相等才能判断两个节点相等
			return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
		}

		public int hashCode() {
			int keyHash = key == null ? 0 : key.hashCode();
			int valueHash = value == null ? 0 : value.hashCode();
			// TODO 为什么hash值是两个hash取异或？？？？？
			return keyHash ^ valueHash;
		}// TODO 作用是什么？？？treemap中entry的hash值我感觉并没有什么卵用？

		public String toString() {
			return key + "=" + value;
		}
	}

	public Set<MyMap.Entry<K, V>> entrySet() {
		return null;
	}

}
