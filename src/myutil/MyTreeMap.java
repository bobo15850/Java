package myutil;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
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
public class MyTreeMap<K, V> extends MyAbstractMap<K, V> implements MyNavigableMap<K, V>, Cloneable, Serializable {
	private static final long serialVersionUID = -2987251376526992040L;

	// 比较器，不可变，只能在初始化的时候指定,treemap中的顺序由key的顺序来确定 //
	// XXX,为什么不直接在entry中继承comparable接口
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
		comparator = null;// 使用自然顺序比较器
		putAll(map);
	}// 使用一个常规map初始化treemap

	// 通过一个排好序的map来初始化map调用buildFromSorted方法，递归的构造map
	// XXX 如果map过大会不会造成stackoverflow？？？？
	public MyTreeMap(SortedMap<K, ? extends V> m) {
		this.comparator = m.comparator();
		try {
			buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int size() {
		return size;
	}// 返回键值对的多少

	public boolean containsKey(Object key) {
		return getEntry(key) != null;
	}// 判断是否存在某个key,如果为null会跑出nullpointer异常，如果key没有实现comparable接口会抛出异常

	public boolean containsValue(Object value) {
		// 由于value是没有顺序的，所以没有O（lgn）的算法，最好的复杂度控制在O(n)
		for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
			if (valEquals(value, e.getValue())) {
				return true;
			}
		}// 使用后继的方式可以降低查找的复杂度，因为如果直接使用每一个get方法的话复杂度是O（n*logn）
		return false;
	}// 判断是否存在某个value，value的值可以为null

	// 通过key来查找value，如果key为null抛出nullpointer异常
	// 如果既没有实现comparable接口有没有指定comparator会抛出classcast异常
	public V get(Object key) {
		Entry<K, V> p = getEntry(key);
		return p == null ? null : p.value;
	}

	public Comparator<? super K> comparator() {
		return comparator;
	}// 返回比较器

	public K firstKey() {
		return key(getFirstEntry());
	}// 返回按照排序规则的第一个键

	public K lastKey() {
		return key(getLastEntry());
	}// 返回按照排序规则的最后一个键

	public void putAll(MyMap<? extends K, ? extends V> map) {
		int mapSize = map.size();
		// 如果传入的map是可排序的，当前的map是空的并且map的比较器和当前的比较器相同
		if (size == 0 && mapSize != 0 && map instanceof SortedMap) {
			Comparator<?> c = ((MySortedMap<?, ?>) map).comparator();// 得到比较器
			if (c == comparator || (c != null && c.equals(comparator))) {
				++modCount;
				try {
					buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}
		// 直接调用抽象类中的方法，一个个put
		super.putAll(map);
	}

	/*
	 * 下面的一系列方法是各种查询entry的的方法的具体实现，大于小于等于，大于等于，小于等于。。。
	 */

	final Entry<K, V> getEntry(Object key) {
		if (comparator != null) {
			return getEntryUsingComparator(key);
		}
		if (key == null) {
			throw new NullPointerException();
		}

		// 得到key的比较器，如果没有初始化比较器key必须实现comparable接口
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) key;

		// 以下利用key的comparable接口进行二叉搜索树的查找
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = k.compareTo(p.key);
			if (cmp < 0) {
				p = p.left;
			}
			else if (cmp > 0) {
				p = p.right;
			}
			else {
				return p;
			}
		}
		return null;// 一直没有查找到返回null
	}

	// 通过初始化的时候设置的比较器来得到元素，一定存在比较器
	final Entry<K, V> getEntryUsingComparator(Object key) {
		@SuppressWarnings("unchecked")
		K k = (K) key;// 这里的强转可以检测key是否为null，然后抛出nullpointerException
		Comparator<? super K> cpr = comparator;
		if (cpr != null) {
			Entry<K, V> p = root;
			while (root != null) {
				int cmp = cpr.compare(k, p.key);
				if (cmp < 0) {
					p = p.left;
				}
				else if (cmp > 0) {
					p = p.right;
				}
				else {
					return p;
				}
			}
		}
		return null;
	}

	final Entry<K, V> getLowerEntry(K key) {
		// TODO
		return null;
	}

	final Entry<K, V> getHigherEntry(K key) {
		// TODO
		return null;
	}

	final Entry<K, V> getCeilingEntry(K key) {
		// TODO
		return null;
	}

	final Entry<K, V> getFloorEntry(K key) {
		// TODO
		return null;
	}

	public V put(K key, V value) {
		// 先找到所要添加的位置，然后红黑树的调整，使其符合红黑树的性质4,5
		Entry<K, V> t = root;
		if (t == null) {
			compare(key, key);// 调用本地方法，进行类型检查和也有可能是null值检查
			root = new Entry<K, V>(key, value, null);
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
		size++;
		modCount++;
		return null;// 新添加键值对返回null
	}// 向map中添加一个元素，如果存在key则替换原先的value，返回值是原来的value值，或者null（原来不存在，新添加的key-value）

	// 刪除元素，需要判断是否存在该key的元素
	public V remove(Object key) {
		Entry<K, V> p = getEntry(key);// 找到需要删除的元素
		if (p == null) {
			return null;// 没有找到元素
		}
		V oldValue = p.value;
		deleteEntry(p);
		return oldValue;
	}

	public void clear() {
		modCount++;// 一次clear操作只算一次修改
		size = 0;
		root = null;
	}

	// 返回一个浅拷贝，我觉得这个方法并没有什么卵用
	// public Object clone() {
	// MyTreeMap<?, ?> clone;
	// try {
	// clone = (MyTreeMap<?, ?>) super.clone();// 调用父类的拷贝方法
	// } catch (CloneNotSupportedException e) {
	// throw new InternalError(e);
	// }
	// // 将这个拷贝得到的对象设置为刚出生的状态，除了比较器
	// clone.size = 0;
	// clone.keySet = null;
	// clone.modCount = 0;
	// clone.root = null;
	// clone.values = null;
	// // TODO 这里还有其他的变量没有设置
	//
	// // 这个方法暂时无法完成
	// return null;
	//
	// }

	/*
	 * 以下的方法是实现NavigableMap的API方法
	 */

	// 返回小于key的第一个entry
	public MyMap.Entry<K, V> lowerEntry(K key) {
		// TODO
		return null;
	}

	public K lowerKey(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> floorEntry(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public K floorKey(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> ceilingEntry(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public K ceilingKey(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> higherEntry(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public K higherKey(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> firstEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> lastEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> pollFirstEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	public MyMap.Entry<K, V> pollLastEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	public MyNavigableMap<K, V> descendingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public NavigableSet<K> navigableKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	public NavigableSet<K> descendingKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	public MyNavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	public MyNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	public MyNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	public MySortedMap<K, V> subMap(K fromKey, K toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public MySortedMap<K, V> headMap(K toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public MySortedMap<K, V> tailMap(K fromKey) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 下面是工具方法，对entry或者是key进行各种检查，或者是比较，导出等操作
	 */

	// 先进行类型转换，不成功会抛出异常，然后比较两个key的值
	@SuppressWarnings("unchecked")
	final int compare(Object k1, Object k2) {
		return comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
	}

	static final boolean valEquals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);// 避免了null检查
	}// 判断两个value是否equals，如果是两个null返回true

	// 返回不可变的entry对象 simpleImmutableEntry
	static <K, V> MyMap.Entry<K, V> exportEntry(MyTreeMap.Entry<K, V> e) {
		return e == null ? null : new MyAbstractMap.SimpleImmutableEntry<K, V>(e);
	}

	// 返回一个元组的键，如果元组为null则返回null
	static <K, V> K KeyOrNull(MyTreeMap.Entry<K, V> e) {
		return e == null ? null : e.key;
	}

	// 返回一个元组的键，如果键we为null则抛出异常
	static <K> K key(Entry<K, ?> e) {
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e.key;
	}

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
			// XXX 为什么hash值是两个hash取异或？？？？？
			return keyHash ^ valueHash;
		}// XXX 作用是什么？？？treemap中entry的hash值我感觉并没有什么卵用？

		public String toString() {
			return key + "=" + value;
		}
	}

	final Entry<K, V> getFirstEntry() {
		Entry<K, V> p = root;
		if (p != null) {
			while (p != null) {
				p = p.left;
			}
		}
		return p;
	}// 得到整个treemap中第一个元素,也就是最左下角边的一个元素

	final Entry<K, V> getLastEntry() {
		Entry<K, V> p = root;
		if (p != null) {
			while (p.right != null) {
				p = p.right;
			}
		}
		return p;
	}// 返回整个treemap中按照规则排序最大的一个元素

	// 返回特定节点的后继结点，按照特定的顺序的后继
	static <K, V> MyTreeMap.Entry<K, V> successor(Entry<K, V> t) {
		if (t == null) {
			return null;
		}
		// 右子树不为空
		else if (t.right != null) {
			Entry<K, V> p = t.right;
			while (p.left != null) {
				p = p.left;
			}
			return p;
		}
		// 没有右子树
		else {
			Entry<K, V> p = t.parent;
			Entry<K, V> ch = t;
			// 只要出现一个向右的拐点就说明拐点的父节点就是后继，否则则一直到达根节点，就说明没有后继，返回null
			while (p != null && ch == p.right) {
				ch = p;
				p = p.parent;
			}//
			return p;
		}
	}

	// 返回某一结点的前驱结点,类似于上面的找后继结点
	static <K, V> Entry<K, V> predecessor(Entry<K, V> t) {
		if (t == null) {
			return null;
		}
		else if (t.left != null) {
			Entry<K, V> p = t.left;
			while (p.right != null) {
				p = p.right;
			}
			return p;
		}
		else {
			Entry<K, V> p = t.parent;
			Entry<K, V> ch = t;
			// 只要出现一个向左的拐点拐点就说明拐点上面的值是前驱，如果一直没有拐点则说明一直到了root，没有前驱，返回null
			while (p != null && ch == p.left) {
				ch = p;
				p = p.parent;
			}
			return p;
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

	/*
	 * 主要思路：
	 * 1.首先判断新节点是加在左节点还是右节点上，以下以左节点为例
	 * 2.然后判断新节点的叔叔节点的颜色，如果是红色则说明爷爷节点是黑色，就要颠倒爷父叔之后递归调用爷
	 * 3.如果是黑色或者没有则要判断新加的节点是左节点还是右节点，如果是右节点则要进行左旋新节点的父节点
	 * 4.最后右旋爷爷节点，调整父节点和爷爷节点的颜色
	 * 
	 * 整个过程的结束条件就是新加的节点是加在黑色节点上的（或者本身已经成为根节点），在黑节点上加节点只要加个红节点，无需调整
	 */
	private void fixAfterInsertion(Entry<K, V> x) {
		x.color = RED;// 对于每一个新加入的节点先把颜色设置成红色，首先满足规则五

		while (x != null && x != root && x.parent.color == RED) {
			// 节点加在一个左节点上
			if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
				Entry<K, V> y = rightOf(parentOf(parentOf(x)));// 新加节点父节点的兄弟节点
				// 叔叔节点是红节点，此时父节点的父节点是黑色，父节点一定是红色
				if (colorOf(y) == RED) {
					// 直接将父节点和叔叔节点还有爷爷节点颜色颠倒，将自己的颜色设置成红色，然后再递归的调整爷爷节点
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));// 将x设置成其爷爷节点，用迭代的方式实现递归调整，直到根节点
				}
				// 叔叔节点是黑节点或者为空
				else {
					// 新加入的节点加在右节点的位置上
					if (x == rightOf(parentOf(x))) {
						// 经过旋转将其加变到左节点上，然后统一处理
						x = parentOf(x);
						rotateLeft(x);
					}
					// 以下同一处理添加在左节点位置的情况,将父节点提到顶节点的位置，颜色变为黑
					// 爷爷节点变成顶节点的右节点，颜色变为红
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					rotateRight(parentOf(parentOf(x)));
				}
			}
			// 节点加在一个右节点上
			else {
				// 这里的操作与节点添加在左节点上是相反的
				Entry<K, V> l = leftOf(parentOf(parentOf(x)));
				if (colorOf(l) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					setColor(l, BLACK);
					x = parentOf(parentOf(x));
				}
				else {
					if (x == leftOf(parentOf(x))) {
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					rotateLeft(parentOf(parentOf(x)));
				}
			}
		}
	}// 插入元素之后的调整，調用以上的方法实现

	// 刪除元素，一定存在的节点
	/*
	 * 二叉搜索樹刪除节点（该节点做右子树都有）的处理方法：
	 * 1.用该节点的左子树或者右子树结点代替被删除的节点，然后将剩下的子树连接在最左或者最右
	 * 2.用该节点的前驱结点或者后继结点代替被删除的节点，在递归的删除前驱或者后继结点
	 * 
	 * treemap中采用的是：被删除节点的右子树中最小节点与被删节点交换的方式进行维护（用后继结点代替该节点，然后递归的删除）
	 */
	private void deleteEntry(Entry<K, V> p) {
		modCount++;
		size--;

		if (p.left != null && p.right != null) {
			Entry<K, V> s = successor(p);// 得到被刪除节点的后继结点，一定不为null而且该节点一定没有左子树
			p.key = s.key;
			p.value = s.value;// 只是改变该节点的键和值
			p = s;
		}// 用后继结点代替该节点，因为后继结点一定是没有左子节点的，所以也完成了控制最多只有一个子树的任务

		Entry<K, V> replacement = (p.left != null ? p.left : p.right);

		if (replacement != null) {// 用唯一的一个子树替代该节点
			replacement.parent = p.parent;
			if (p.parent == null) {
				root = replacement;
			}
			else if (p.parent.left == p) {
				p.parent.left = replacement;
			}
			else {
				p.parent.right = replacement;
			}
			p.left = p.right = p.parent = null;// 消除引用防止内存泄漏，使其可以进行颜色调整
			if (p.color == BLACK) {
				fixAfterDeletion(replacement);
			}
		}
		else if (p.parent == null) {// p是根节点
			root = null;// 刪除根节点
		}
		else {
			if (p.color == BLACK) {
				fixAfterDeletion(p);
			}
			if (p.parent != null) {
				if (p == p.parent.left) {
					p.parent.left = null;
				}
				else if (p == p.parent.right) {
					p.parent.right = null;
				}
			}// 删除节点，以取消父节点对该节点的引用来实现
		}
	}// 刪除元素

	// 调整要被删除的树，删除节点之前进行调整，参数是将要被删除的节点
	private void fixAfterDeletion(Entry<K, V> x) {
		// 只有删除的是黑色节点才需要调整，红色节点直接删除就好了
		while (x != root && colorOf(x) == BLACK) {
			// 被刪除的节点是其父节点的左节点
			if (x == leftOf(parentOf(x))) {
				Entry<K, V> sib = rightOf(parentOf(x));// 需要被刪除节点的兄弟节点（可以不存在，顔色为黑）

				// 进行左旋
				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}

				// 需要被刪除节点的兄弟节点的两个子节点都是黑色
				if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);// 这里用while迭代代替了递归
				}
				else {
					// IPT 这段代码不太理解是怎么进行操作的。。。
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			}
			else { // 被刪除的节点是其父节点的右节点，与上面相反
				Entry<K, V> sib = leftOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}

				if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				}
				else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}
	}

	// 该方法是将所有的传入的元素都添加到map中去
	private void buildFromSorted(int size, Iterator<?> it, java.io.ObjectInputStream str, V defaultVal)
			throws java.io.IOException, ClassNotFoundException {
		this.size = size;
		root = this.buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
	}

	// 该方法通过递归的方式将部分的元素添加到map中去
	/*
	 * level:表示树现在构造到的深度
	 * lo:表示需要添加的元素的起始位置
	 * li:表示需要添加的元素的末尾位置
	 * redLevel:
	 * 需要变成红色的深度（所有的满二叉树层都可以直接用黑色，只有最后一层不满的需要使用红色这样才能保证所有路径上黑色的数目相等）
	 * it和str:这两者只需要使用其中一个就可以了，优先使用it如果it为空就使用str
	 * defaultVal:
	 * 如果为空则表示iterator中的元素是完整的Entry如果不为空则表示iterator中的元素只是key，需要使用defalutVal来填充每一个值
	 */
	@SuppressWarnings("unchecked")
	private final Entry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, Iterator<?> it,
			java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException {
		if (hi < lo) return null;
		int mid = (hi + lo) >>> 1;
		Entry<K, V> left = null;
		if (lo < mid) {
			left = buildFromSorted(level, lo, mid - 1, redLevel, it, str, defaultVal);// 递归方式构建左子树
		}

		// 得到此次遍历的根节点
		K key;
		V value;
		// 使用迭代器中的元素
		if (it != null) {
			if (defaultVal == null) {
				MyMap.Entry<?, ?> entry = (MyMap.Entry<?, ?>) it.next();
				key = (K) entry.getKey();
				value = (V) entry.getValue();
			}
			else {
				key = (K) it.next();
				value = defaultVal;
			}
		}
		// 使用str中的元素
		else {
			key = (K) str.readObject();
			value = (defaultVal != null ? defaultVal : (V) str.readObject());
		}

		// 处理现在的根节点
		Entry<K, V> middle = new Entry<K, V>(key, value, null);
		if (level == redLevel) {
			middle.color = RED;
		}
		if (left != null) {
			middle.left = left;
			left.parent = middle;
		}
		if (mid < hi) {
			// 递归得到右子树
			Entry<K, V> right = buildFromSorted(level, mid + 1, hi, redLevel, it, str, defaultVal);
			middle.right = right;
			right.parent = middle;
		}
		return middle;
	}

	// 计算最后一层不满的层（需要使用红色节点）
	private static int computeRedLevel(int sz) {
		int level = 0;
		for (int m = sz - 1; m >= 0; m = m / 2 - 1)
			level++;
		return level;
	}

}
/*
 * 参考资料：
 * 1.https://www.ibm.com/developerworks/cn/java/j-lo-tree/
 * 2.http://blog.csdn.net/v_july_v/article/details/6105630
 * 视频资料：
 * 1.http://v.youku.com/v_show/id_XMjE4MjQwMTQ4.html
 */
