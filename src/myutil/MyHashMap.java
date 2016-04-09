package myutil;

import java.io.Serializable;
import java.util.LinkedHashMap;
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

	// 根据hashCode返回值来计算hash值
	public final int hash(Object key) {
		int h;
		// 高16位不变，低16位是高16位和低16位的异或，这样做的目的是：让hashCode的高位也参与进来，减少碰撞
		return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
	}

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
	 * 如果talbe满了（超过容量*加载因子）则进行resize（将table长度变成两倍）
	 */
	public V put(K key, V value) {
		// TODO
		return null;
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
				// TODO 直接调用红黑树的方法实现添加
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
		// TODO 这个有什么用。。。evict的作用是什么，在这个方法里面都没有被使用过？？？
		afterNodeInsertion(evict);
		return null;// 新建的节点就直接返回null，如果是替换就需要返回原先的值
	}

	// 如果table为空就用initialCapacity创建table，否则就将table的length变成两倍，然后进行数据迁移
	final Node<K, V>[] resize() {
		// TODO
		return null;
	}

	final void treeifyBin(Node<K, V>[] tab, int hash) {
		// TODO 该方法需要重点实现，将某个链表改成红黑树
	}

	public Set<MyMap.Entry<K, V>> entrySet() {
		return null;
	}

	// 得到一个普通的节点
	Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
		return new Node<K, V>(hash, key, value, next);
	}

	// 说是回调函数，，但是我并不知道是什么鬼。。。
	void afterNodeAccess(Node<K, V> p) {
	}

	void afterNodeInsertion(boolean evict) {
	}

	void afterNodeRemoval(Node<K, V> p) {
	}

	// TODO这个类需要重点来完善
	static final class TreeNode<K, V> extends MyLinkedHashMap.Entry<K, V> {

		TreeNode(int hash, K key, V value, Node<K, V> next) {
			super(hash, key, value, next);
		}

	}
}
/*
 * 参考资料：
 * 1.http://www.importnew.com/18633.html
 */
