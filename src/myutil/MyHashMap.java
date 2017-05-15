package myutil;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MyHashMap<K, V> extends MyAbstractMap<K, V> implements MyMap<K, V>, Cloneable, Serializable {
	/*
	 * 一些列类的静态变量
	 */
	private static final long serialVersionUID = 362498820763181265L;

	// 默认的数组容量是16，数组容量必须是2的指数，因为根据计2的幂的散列效果最好
	static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

	// 最大的散列值，有int的限制
	static final int MAXIMUM_CAPACITY = 1 << 30;

	// 默认的加载因子，如果已经填充的条目数和总容量的比值大于该数，需要rehash将容量翻倍
	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	// 升树阈值，当一个条目中的节点个数大于这个值就将链表转化为红黑树
	static final int TREEIFY_THRESHOLD = 8;

	// 退树的阈值，当一个条目的节点个数小于该值则将红黑树退化为普通的链表
	static final int UNTREEIFY_THRESHOLD = 6;

	// 最小扩容容量
	static final int MIN_TREEIFY_CAPACITY = 64;

	// 存放键值对的数据结构，链表表示
	static class Node<K, V> implements MyMap.Entry<K, V> {
		final int hash;// 这里保存的hash其实是key的hash
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

		// 这里的异或是什么作用？？？这里只是单个Node的哈希值，与用于定位的哈希值无关
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

	// 根据hashCode返回值来计算hash值
	public final int hash(Object key) {
		int h;
		// 高16位不变，低16位是高16位和低16位的异或，这样做的目的是：让hashCode的高位也参与进来，减少碰撞
		return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
	}

	// 最终得到的结果是第一个大于等于cap的2的幂的数
	static final int tableSizeFor(int cap) {
		int n = cap - 1;// 为了防止cap本身是2的幂的情况
		// 平行算法：将n变为以原先的cap的位数，所有位上全是1,
		n |= n >>> 1;// 最高2位变成1，最终为00...0011...
		n |= n >>> 2;// 最高4位变为1，最终为00...001111...
		n |= n >>> 4;// 8
		n |= n >>> 8;// 16
		n |= n >>> 16;// 32,int只有32位且第一位是符号位，到此可以把cap第一个为1的位之后的所有位变为1
		// 保证tableSize不大于1<<30的2的幂
		return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
	}

	/*
	 * 实例变量，反序列化之后变成一个全新的失实例了，相当于拿原先的元素直接初始化
	 */
	transient Node<K, V>[] table;

	transient Set<MyMap.Entry<K, V>> entrySet;

	transient int size;

	transient int modCount;

	// 阈值，表示哈希表中元素达到该值进行下一次扩容，capacity * loadFactor
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
		// 首次进行扩容的阈值即为第一次创建table时的长度，指定的capacity并没有保存而是转化为了一个tableSize，供创建table时使用，该值是第一个大于等于capacity的2的幂
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
		putMapEntries(m, false);
	}

	/**
	 * 将参数中map的所有键值放入本实例的map中
	 * 
	 * @param m
	 * @param evict
	 *            该参数是在putVal方法中使用，提供扩展点
	 */
	final void putMapEntries(MyMap<? extends K, ? extends V> m, boolean evict) {
		int s = m.size();
		if (s > 0) {
			// 初始化，直接分配阈值等参数
			if (table == null) {
				// 反向根据阈值和加载因子求容量
				float ft = ((float) s / loadFactor) + 1.0F;
				int t = (ft < (float) MAXIMUM_CAPACITY) ? (int) ft : MAXIMUM_CAPACITY;
				if (t > threshold) {
					threshold = tableSizeFor(t);
				}
			}
			// 添加元素,待添加的元素已经大于阈值，直接先进行扩容操作
			else if (s > threshold) {
				resize();
			}
			// 通过循环添加元素
			for (MyMap.Entry<? extends K, ? extends V> e : m.entrySet()) {
				K key = e.getKey();
				V value = e.getValue();
				putVal(hash(key), key, value, false, evict);
			}

		}
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	// 重写抽象父类的方法，添加一个元素
	/*
	 * 首先根据hashCode做hash，再确定table的index，如果没有碰撞就直接放在bucket中，
	 * 如果碰撞了以链表的形式存在bucket后面，如果碰撞导致链表过长，就将链表改成红黑树，
	 * 如果节点已经存在就用newValue替换oldValue
	 * 如果table满了（超过容量*加载因子）则进行resize（将table长度变成两倍）
	 */
	public V put(K key, V value) {
		return putVal(hash(key), key, value, false, true);
	}

	// 添加元组的真正实现类
	final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
		Node<K, V>[] tab;
		Node<K, V> p;// 表示table的index位置的第一个元素
		int n, i;// n表示table的长度，i表示hash之后的位置index值
		// tab为空则创建
		if ((tab = table) == null || (n = tab.length) == 0) {
			n = (tab = resize()).length;// 同时设置了table和n
		}
		// index的计算方式：传进来的hash&(table的长度-1)，只有hash的最后几位进行了计算，所以之前的hash函数把高位加进来很有必要
		if ((p = tab[i = hash & (n - 1)]) == null) {// 这一步即设置了p(当前table的index里的元素)，又设置了i（index）
			// 没有碰撞，直接添加
			tab[i] = new Node<K, V>(hash, key, value, null);
		}
		// 发生了碰撞
		else {
			Node<K, V> e = null;// 遍历链表的局部变量，最终会成为添加的元素的指针
			K k = null;// 表示table的index位置第一个元素的key
			// 这一步判断第一个元素先不用关心存储的方式，接下来要针对不同的存储结构（链表，树）进行不同的操作
			if (p.hash == hash && (k = p.key) == key || (key != null && key.equals(k))) {
				e = p;
			}
			else if (p instanceof TreeNode) {
				((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);// 直接调用红黑树的方法实现添加
			}
			else {
				// 这里是直接的链表实现方式,找到最终需要添加的地方
				for (int binCount = 0;; ++binCount) {
					// 每一个循环都要做一次e =p.next---我觉得这种表达方式不好，应该分开来写
					if ((e = p.next) == null) {
						p.next = newNode(hash, key, value, null);
						// 判断是否达到了转化为红黑树的长度
						if (binCount >= TREEIFY_THRESHOLD - 1) {
							treeifyBin(tab, hash);// 将链表转化为红黑树存储
						}
						break;
					}
					// 找到相同的键了，接下来直接替换
					if (e.hash == hash && (k = e.key) == key || (k != null && k.equals(key))) {
						break;
					}
					p = e;
				}
			}
			if (e != null) {// 存在这个键
				V oldValue = e.value;
				// 如果设置了没有只有缺少才可以添加，或者不管有没有设置onlyIfAbsent位只要原来的value是null
				if (!onlyIfAbsent || oldValue == null) {
					e.value = value;
					afterNodeAccess(e);
					return oldValue;
				}
			}
		}
		++modCount;
		if (++size > threshold) {// 如果添加之后超过loadFactor*currentCapacity，需要resize
			resize();
		}
		// evict是为了在LinkedHashMap中使用的，在LinkedHashMap中会重写该方法进行其他操作
		afterNodeInsertion(evict);
		return null;// 新建的节点就直接返回null，如果是替换就需要返回原先的值
	}

	/**
	 * 如果table为空就用initialCapacity创建table，否则就将table的length变成两倍，然后进行数据迁移，
	 * 迁移的过程中可能会有红黑树、链表的拆分，红黑树的退化为链表等操作
	 * 
	 * @return
	 */
	final Node<K, V>[] resize() {
		Node<K, V>[] oldTab = table;
		int oldCap = (oldTab == null) ? 0 : oldTab.length;
		int oldThr = threshold;
		int newCap, newThr = 0;
		if (oldCap > 0) { // 已经初始化过了，进行扩容
			// 此处有可能oldCap本身为1<<30,再进行移位就变成负数了
			if (oldCap >= MAXIMUM_CAPACITY) { // 已经达到最大容量
				threshold = Integer.MAX_VALUE;
				return oldTab;
			}
			else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY) {
				threshold = oldThr << 1; // 扩容两倍同样阈值也扩大两倍
			}
		}
		else if (oldThr > 0) { // table未初始化，仅仅初始化了threshold，通过new
								// HashMap(int)或者new HashMap(int,float)进行创建
			newCap = oldThr;// 第一次分配table的大小通过HashMap(int,float)中的threshold进行了保存
		}
		else { // 执行至此说明HashMap是通过new HashMap()直接创建，没有指定初始化参数
			newCap = DEFAULT_INITIAL_CAPACITY;
			newThr = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		}
		if (newThr == 0) {// 对应于上面的第二个else
							// if，说明是第一次创建，且在初始化hashMap的时候指定了capacity，此时的threshold是根据构造函数计算出来的tableSize，这里需要重新计算newThr
			float ft = newCap * loadFactor;
			newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY) ? (int) ft : Integer.MAX_VALUE;
		}
		threshold = newThr;

		// 分成两部分：上半部分是计算新的capacity以及新的threshold，下半部分进行数据迁移

		@SuppressWarnings("unchecked")
		Node<K, V>[] newTab = new Node[newCap];
		table = newTab;
		if (oldTab != null) {
			for (int j = 0; j < newCap; ++j) {
				Node<K, V> e;
				if ((e = oldTab[j]) != null) {
					oldTab[j] = null;// 将原来的table上的一个节点上的内容缓存到e上，使得oldTable能够被GC回收
					if (e.next == null) {// 只有一个元素，只需要将该元素本身迁移
						newTab[e.hash & (newCap - 1)] = e;// newCap是2的k次幂，求模相当于取其最后的k位
															// ！！！
					}
					else if (e instanceof TreeNode) {// 已经是一棵红黑树，需要把该红黑树分裂开来，一半放在高位，一半放在低位，具体处理在split方法中
						((TreeNode<K, V>) e).split(this, newTab, j, newCap);
					}
					else {
						// 原本是一个链表，将其分裂成两个链表，分别放在高低位
						Node<K, V> loHead = null, loTail = null;
						Node<K, V> hiHead = null, hiTail = null;
						Node<K, V> next;
						do {
							next = e.next;
							if ((e.hash & oldCap) == 0) { // 说明是模newCap的值小于oldCap，应该放在低位
								if (loTail == null) {
									loHead = e;// head保存第一个指针
								}
								else {
									loTail.next = e;// tail是一个游标，将之后的元素链上去
								}
								loTail = e;
							}
							else { // 高位
								if (hiTail == null) {
									hiHead = e;
								}
								else {
									hiTail.next = e;
								}
								hiTail = e;
							}
						} while ((e = next) != null);
						if (loTail != null) {// 低位有元素
							loTail.next = null;
							newTab[j] = loHead;
						}
						if (hiTail != null) {// 高位有元素
							hiTail.next = null;
							newTab[j + oldCap] = hiHead;
						}
					}
				}
			}
		}
		return newTab;
	}

	/**
	 * 冲突链表长度达到转化阈值判断是进行扩容还是将链表转化为红黑树
	 * 
	 * @param tab
	 *            整个map的链表
	 * @param hash
	 *            为了定位到具体的链达到阈值的链
	 */
	final void treeifyBin(Node<K, V>[] tab, int hash) {
		int n, index;
		Node<K, V> e;
		// 如果map为空为初始化或者tab的长度小于最小转化为红黑树的的tab长度，则进行resize
		if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) {
			resize();
		}
		// 找到对应的位置的开头不为空则进行红黑树的转化，对2^n取模相当于对2^n - 1做&运算
		else if ((e = tab[index = (n - 1) & hash]) != null) {
			TreeNode<K, V> hd = null, tl = null;
			// 该循环中只有tl是循环量，p是每次循环创建的
			// tl表示当前正在处理的节点的前一个节点，hd表示该链表的头结点，红黑树的根节点
			do {
				TreeNode<K, V> p = replacementTreeNode(e, null);
				if (tl == null) {
					hd = p;
				}
				else {
					p.prev = tl;
					tl.next = p;
				}
				tl = p;
			} while ((e = e.next) != null);
			// 现将普通node链表转化为红黑树TreeNode链表，再进行红黑树转化
			if ((tab[index] = hd) != null) {
				hd.treeify(tab);
			}
		}
	}

	public void putAll(MyMap<? extends K, ? extends V> m) {
		putMapEntries(m, true);// TODO 此处使用true作用？？原因？？
	}

	/**
	 * 删除map中的键值
	 * 
	 * @param key
	 *            键
	 * @return 如果不存在key的键返回null，但是返回null也可能是key键的值本来就是null
	 */
	public V remove(Object key) {
		return null;
	}

	/**
	 * 删除操作的具体实现方法
	 * 
	 * @param hash
	 *            键的哈希值
	 * @param key
	 * @param value
	 *            期望的值，只有在matchValue被设置为true的时候才会检查是否匹配
	 * @param matchValue
	 *            设置之后，只有当value和通过key取到的值匹配才进行删除
	 * @param movable
	 *            TODO 作用暂且不明
	 * 
	 * @return
	 */
	final Node<K, V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
		Node<K, V>[] tab;
		Node<K, V> p;// 通过hash定位到当前的budget的第一个元素
		int n, index;
		// map已经初始化，且不为空，且待处理的位置上有元素，此种编码方式将赋值操作和判断操作同时进行了
		if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
			Node<K, V> node = null, e;// node用以保存目标节点，e作为循环变量
			K k;
			V v;
			// budget的第一个元素就是需要找的key
			if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
				node = p;
			}
			// 查找该budget剩下的元素
			else if ((e = p.next) != null) {
				// 该budget下是红黑树,直接调用内部TreeNode的查找方法
				if (p instanceof TreeNode) {
					node = ((TreeNode<K, V>) p).getTreeNode(hash, key);
				}
				// 该budget下是链表,通过遍历查找对应的节点
				else {
					do {
						if (e.hash == hash && (((k = e.key) == key) || (key != null && key.equals(k)))) {
							node = e;
							break;
						}
						p = e;// p保存当前循环节点的前一个节点
					} while ((e = e.next) != null);
				}
			}
			// 处理matchValue参数，并选择删除方式进行处理
			// 处理方式同时兼顾了==判断和equal判断
			if (node != null && (!matchValue || (v = node.value) == value || value != null && value.equals(v))) {
				// 红黑树的删除直接调用TreeNode方法实现
				if (node instanceof TreeNode) {
					((TreeNode<K, V>) node).removeTreeNode(this, tab, movable);
				}
				// 目标节点是该budget下的第一个节点
				else if (node == p) {
					tab[index] = node.next;
				}
				// 目标节点在链表中部
				else {
					p.next = node.next;
				}

				++modCount;
				--size;
				afterNodeRemoval(node);// 钩子函数，可扩展删除元素之后的行为
				return node;
			}
		}
		return null;
	}

	public Set<MyMap.Entry<K, V>> entrySet() {
		return null;
	}

	// 得到一个普通的节点
	Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
		return new Node<K, V>(hash, key, value, next);
	}

	// 将一个hashMap的node转化为一个红黑树的Treenode，在将链表转化为红黑树时使用
	TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
		return new TreeNode<K, V>(p.hash, p.key, p.value, next);
	}

	// 回调函数，给LinkedHashMap来使用
	void afterNodeAccess(Node<K, V> p) {
	}

	void afterNodeInsertion(boolean evict) {
	}

	void afterNodeRemoval(Node<K, V> p) {
	}

	// TODO这个类需要重点来完善,通过红黑树节点来存储过长的链表
	static final class TreeNode<K, V> extends MyLinkedHashMap.Entry<K, V> {
		TreeNode<K, V> parent;
		TreeNode<K, V> left;
		TreeNode<K, V> right;
		TreeNode<K, V> prev;

		TreeNode(int hash, K key, V value, Node<K, V> next) {
			super(hash, key, value, next);
		}

		/**
		 * 查找红黑树中对应的节点
		 * 
		 * @param hash
		 * @param key
		 * @param keyClass
		 * @return
		 */
		final TreeNode<K, V> find(int hash, Object key, Class<?> keyClass) {
			// TODO
			return null;
		}

		/**
		 * 
		 * @param hash
		 * @param key
		 * @return
		 */
		final TreeNode<K, V> getTreeNode(int hash, Object key) {
			// TODO
			return null;
		}

		/**
		 * 将链表转化为红黑树
		 * 
		 * @param tab
		 */
		final void treeify(Node<K, V>[] tab) {
			// TODO
		}

		/**
		 * 该方法实现了向红黑树中添加元素
		 * 
		 * @param map
		 * @param tab
		 * @param hash
		 * @param key
		 * @param value
		 * @return
		 */
		final Node<K, V> putTreeVal(MyHashMap<K, V> map, Node<K, V>[] tab, int hash, K key, V value) {
			// TODO
			return null;
		}

		/**
		 * 失信从红黑树中删除一个元素
		 * 
		 * @param map
		 * @param tab
		 * @param movable
		 */
		final void removeTreeNode(MyHashMap<K, V> map, Node<K, V>[] tab, boolean movable) {
			// TODO 从红黑树中删除一个元素
		}

		/**
		 * 该方法实现了扩容时将原来是红黑树的节点进行分裂，分成低位和高位两部分
		 * 两部分根据是否小于{@link MyHashMap#UNTREEIFY_THRESHOLD}来决定是否退化为链表
		 * 
		 * @param map
		 *            所属的hashMap
		 * @param tab
		 *            扩容后的table
		 * @param index
		 *            扩容前的位置
		 * @param bit
		 *            扩容前的capacity
		 */
		final void split(MyHashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
			// TODO
		}

	}
}
/*
 * 参考资料：
 * 1.http://www.importnew.com/18633.html
 * 2.http://www.cnblogs.com/dongkuo/p/4960550.html
 */
