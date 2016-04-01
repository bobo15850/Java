package myutil;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;

/*
 * 通过红黑树来实现map
 * 二叉查找树的性质：
 * 1.如果左子树不为空，则左子树上所有的节点值小于根节点
 * 2.如果右子树不为空，则右子树上所有的节点值大于根节点
 * 3.任意节点的左右子树也都是二叉查找树
 * 4.没有键值相等的节点
 * 红黑树（二叉查找樹的一种）的性质：
 * 1.每个节点要么红色要么黑色
 * 2.根节点是黑色
 * 3.每个叶节点（最后的null节点）都是黑色
 * 4.如果一个节点是红色，那么他的两个子节点都是黑色
 * 5.对任意节点：其道叶节点尾端每条路径包含相同数目的黑节点
 * 
 * 如果将二叉查找树的红色节点并入到黑色节点中，就是一个2，3,4树，可以经过证明得到其高度与节点总个数是log（n）关系
 * 二三四数的插入不会往四节点中插入，删除不会删除一个二节点，以保证所有的叶子节点在同一层
 * 
 * 紅黑树的插入方式：1.直接通过2，3,4数来插入，插入的时候直接调整
 * 				2.先插入二叉搜索树，在进行旋转和调整颜色
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
			}// 当没有指定比较器时treemap的键不能为空

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
		fixAfterInsertion(e);// 对插入的值进行调整
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

	/*
	 * 以下是支持旋转操作调整红黑树节点形态和颜色的静态操作
	 */

	private static <K, V> boolean colorOf(Entry<K, V> e) {
		return e == null ? BLACK : e.color;
	}// 返回节点的颜色，null默认为黑色因为叶节点都是null而且为黑色

	private static <K, V> Entry<K, V> parentOf(Entry<K, V> e) {
		return e == null ? null : e.parent;
	}// 返回节点的父节点

	private static <K, V> void setColor(Entry<K, V> e, boolean c) {
		if (e != null) e.color = c;
	}// 设置节点颜色

	private static <K, V> Entry<K, V> leftOf(Entry<K, V> e) {
		return e == null ? null : e.left;
	}// 返回节点的左子树

	private static <K, V> Entry<K, V> rightOf(Entry<K, V> e) {
		return e == null ? null : e.right;
	}// 返回节点的右子树

	// 以下两种旋转操作只是改变树的形状，并不会改变颜色，不完全
	private void rotateLeft(Entry<K, V> p) {
		if (p != null) {
			Entry<K, V> r = p.right;// 这个不会是空，因为如果这个是空的就没有左旋的必要了
			p.right = r.left;
			if (r.left != null) {
				r.left.parent = p;
			}
			// 旋转之后右节点变成了顶节点,一下是设置顶节点的父节点的子节点
			r.parent = p.parent;
			if (p.parent == null) {
				root = r;
			}
			else if (p.parent.left == p) {
				p.parent.left = r;
			}
			else {
				p.parent.right = r;
			}
			r.left = p;
			p.parent = r;
		}
	}// 将该节点为根的子树左旋，需要调整三对父子关系

	private void rotateRight(Entry<K, V> p) {
		if (p != null) {
			Entry<K, V> l = p.left;// 该节点不是是null，否则的话就没有必要进行右旋
			p.left = l.right;
			if (l.right != null) {
				l.right.parent = p;
			}

			// 旋转之后左节点变成了顶节点
			l.parent = p.parent;
			if (p.parent == null) {
				root = l;
			}
			else if (p.parent.left == p) {
				p.parent.left = l;
			}
			else {
				p.parent.right = l;
			}
			l.right = p;
			p.parent = l;
		}
	}// 将该节点为根的子树右旋

	private void fixAfterInsertion(Entry<K, V> e) {

	}// 插入元素之后的调整，調用以上的方法实现

}
