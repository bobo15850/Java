package myutil;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/*
 * 模拟abstractMap
 */
public abstract class MyAbstractMap<K, V> implements MyMap<K, V> {
	// 唯一的构造方法，子类调用
	protected MyAbstractMap() {
	}

	public int size() {
		return entrySet().size();
	}// 得到键值对的数量

	public boolean isEmpty() {
		return size() == 0;
	}// 判断是否为空

	public boolean containsValue(Object value) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (value == null) {
			while (i.hasNext()) {
				if (i.next().getValue() == null) {
					return true;
				}
			}
		}
		else {
			while (i.hasNext()) {
				if (i.hasNext()) {
					if (value.equals(i.next().getValue())) {
						return true;
					}
				}
			}
		}
		return false;
	}// 判断是否含有某个值，需要考虑为null的情况

	public boolean containsKey(Object key) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (key == null) {
			while (i.hasNext()) {
				if (i.next().getKey() == null) {
					return true;
				}
			}
		}
		else {
			while (i.hasNext()) {
				if (i.hasNext()) {
					if (key.equals(i.next().getKey())) {
						return true;
					}
				}
			}
		}
		return false;
	}// 判断是否存在某个键

	public V get(Object key) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (key == null) {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getKey() == null) {
					return e.getValue();
				}
			}
		}
		else {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (key.equals(e.getKey())) {
					return e.getValue();
				}
			}
		}
		return null;
	}// 根据传入的键值得到值

	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}// 没有具体的实现数据结构，无法执行该操作，该方法必须由子类实现//TODO 为什么不弄成抽象方法？？？？？

	public V remove(Object key) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		Entry<K, V> currentEntry = null;
		if (key == null) {
			while (currentEntry == null && i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getKey() == null) {
					currentEntry = e;
				}
			}
		}
		else {
			while (currentEntry == null && i.hasNext()) {
				Entry<K, V> e = i.next();
				if (key.equals(e.getKey())) {
					currentEntry = e;
				}
			}
		}

		// 上面的操作为了的到对应键的entry，得到之后迭代就停止了，有了迭代器，用迭代器删除

		V oldValue = null;
		if (currentEntry != null) {
			oldValue = currentEntry.getValue();
			i.remove();
		}
		return oldValue;
	}// 移除某个键的键值对，返回该键值对的值

	public void putAll(MyMap<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}// 将所有的另一个map中的元素添加到this map中

	public void clear() {
		entrySet().clear();
	}// 刪除所有的键值对，entrySet必须支持clear方法，否则抛出unsupportedOperatorException

	/*
	 * 下面两个变量都是瞬时的，并且是原子的
	 * 
	 * 确保共享变量能被准确和一致的更新，线程应该确保通过排他锁单独获得这个变量
	 * 
	 * 使用得当的话比synchronized的使用和执行成本会更低，因为它不会引起线程上下文的切换和调度。
	 */
	transient volatile Set<K> keySet;// 因为该属性是通过调用keySet生成的，是瞬时的
	transient volatile Collection<V> values;

	public Set<K> keySet() {
		if (keySet == null) {
			keySet = new AbstractSet<K>() {

				public Iterator<K> iterator() {
					return new Iterator<K>() {
						private Iterator<Entry<K, V>> i = entrySet().iterator();

						public boolean hasNext() {
							return i.hasNext();
						}

						public K next() {
							return i.next().getKey();
						}

						public void remove() {
							i.remove();
						}
					};
				}

				public int size() {
					return MyAbstractMap.this.size();
				}

				public boolean isEmpty() {
					return MyAbstractMap.this.isEmpty();
				}

				public void clear() {
					MyAbstractMap.this.clear();
				}

				public boolean contains(Object k) {
					return MyAbstractMap.this.containsKey(k);
				}
			};
		}
		return keySet;
	}// 该方法用来初始化keySet属性，在第一次调用的时候初始化，相当于懒加载的单例模式

	public Collection<V> values() {
		if (values == null) {
			values = new AbstractCollection<V>() {

				public Iterator<V> iterator() {
					return new Iterator<V>() {
						private Iterator<Entry<K, V>> i = entrySet().iterator();

						public boolean hasNext() {
							return i.hasNext();
						}

						public V next() {
							return i.next().getValue();
						}

						public void remove() {
							i.remove();
						}
					};
				}

				public int size() {
					return MyAbstractMap.this.size();
				}

				public boolean isEmpty() {
					return MyAbstractMap.this.isEmpty();
				}

				public void clear() {
					MyAbstractMap.this.clear();
				}

				public boolean contains(Object v) {
					return MyAbstractMap.this.containsValue(v);
				}
			};
		}
		return values;
	}// 该方法类同域keySet，第一次调用的时候初始化values

	public abstract Set<Entry<K, V>> entrySet();

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof MyMap)) return false;
		MyMap<?, ?> m = (MyMap<?, ?>) o;

		if (m.size() != size()) return false;// 保证键值对个数相等

		try {
			Iterator<Entry<K, V>> i = entrySet().iterator();
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				K key = e.getKey();
				V v = e.getValue();
				if (v == null) {
					if (!(m.get(key) == null && m.containsKey(key))) return false;// 另一个map含有键，值为null
				}
				else {
					if (!v.equals(m.get(key))) return false;// 键相等，值也相等
				}
			} // 如果每一个键都能在另一个中找到对应的相等的键值对，就说明两个map相等
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int h = 0;
		Iterator<Entry<K, V>> i = entrySet().iterator();
		while (i.hasNext()) {
			h += i.next().hashCode();
		}
		return h;
	}// 集合类型或者map的hashCode就是每一个值的hashCode的和

	public String toString() {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (!i.hasNext()) return "{}";
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		for (;;) {
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this ? "(this map)" : key);
			sb.append("=");
			sb.append(value == this ? "this map" : value);// 防止递归死循环
			if (!i.hasNext()) return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}// 转化成{键=值，键=值}的形式

	public Object clone() throws CloneNotSupportedException {
		MyAbstractMap<?, ?> result = (MyAbstractMap<?, ?>) super.clone();
		result.keySet = null;
		result.values = null;// 得到一个浅拷贝，不拷贝具体的内容
		return result;
	}

	private static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	// entry的简单实现
	public static class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {
		private static final long serialVersionUID = -2942656592941818141L;

		private final K key;
		private V value;

		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public SimpleEntry(Entry<? extends K, ? extends V> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		public boolean equals(Object o) {
			if (!(o instanceof MyMap.Entry)) return false;
			MyMap.Entry<?, ?> e = (MyMap.Entry<?, ?>) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}// 键和值全部相等

		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}// 定义成键和值的hashCode取异或//TODO 为什么要设成这个值？？？？不明白

		public String toString() {
			return key + "=" + value;
		}
	}

	// 不可变的Entry
	public static class SimpleImmutableEntry<K, V> implements Entry<K, V>, java.io.Serializable {

		private static final long serialVersionUID = -2942656592941818141L;

		private final K key;
		private final V value;

		public SimpleImmutableEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public SimpleImmutableEntry(Entry<? extends K, ? extends V> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		public boolean equals(Object o) {
			if (!(o instanceof MyMap.Entry)) return false;
			MyMap.Entry<?, ?> e = (MyMap.Entry<?, ?>) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}// 键和值全部相等

		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}// 定义成键和值的hashCode取异或

		public String toString() {
			return key + "=" + value;
		}

	}
}
