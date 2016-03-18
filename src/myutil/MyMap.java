package myutil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/*
 * map接口
 */
public interface MyMap<K, V> {
	int size();

	boolean isEmpty();

	boolean containsKey(Object key);// TODO 为什么此处不使用K泛型，构成泛型方法？

	boolean containsValue(Object value);// TODO 问题同上

	V get(Object key);

	// TODO
	// 添加map元素的时候需要类型检查，取出元素的时候不需要类型检查，
	// 可能是因为取出的时候，就算类型不匹配也只是取不出元素，并不会出现异常，但是添加元素类型不检查可能会有异常

	V put(K key, V value);

	V remove(Object key);// 删除一个元素

	void putAll(MyMap<? extends K, ? extends V> map);// 将一个map中的元素全部添加到另一个中，不是线程安全的

	void clear();// 将map中的所有元素删除

	Set<K> keySet();// 返回所有键的集合

	// TODO 因为key没有重复的值所以可以当作一个set返回，但是value可能重复，还有其他原因吗？？？？？？？
	// 基本上每一个实现类都会自己实现一个values的类型作为返回值

	Collection<V> values();// 返回所有值，因为可能会存在重复的值，所以不能不用Set

	Set<Entry<K, V>> entrySet();

	interface Entry<K, V> {
		K getKey();

		V getValue();

		V setValue(V value);

		boolean equals(Object o);

		int hashCode();

		/*
		 * java 1.8的新特定，接口中的静态方法，该特性可以省去编写工具类的繁琐，直接在接口中返回一个想得到的接口实现
		 */

		// 此处理解为K是Comparable接口的子类型，而且这个comparable接口能比较K类型的超类
		// 返回值是一个MyMap.Entity<K,V>类型的比较器
		public static <K extends Comparable<? super K>, V> Comparator<MyMap.Entry<K, V>> comparingByKey() {
			// 因为比较器是一个函数式接口，唯一的抽象方法是comparaTo方法，所以此处用lambda表达式来代替一个内部类，提高可读性
			return (Comparator<MyMap.Entry<K, V>> & Serializable) (e1, e2) -> e1.getKey().compareTo(e2.getKey());
			// 通过编译器将Lambda表达式自动转换成一个类的实例。这个类由类型推断来决定
		}// 该方法没有防御式编程，如果传入的参数为空会跑出空指针异常

		public static <K, V extends Comparable<? super V>> Comparator<MyMap.Entry<K, V>> comparingByValue() {
			return (Comparator<MyMap.Entry<K, V>> & Serializable) (e1, e2) -> e1.getValue().compareTo(e2.getValue());
		}// 类似于上面

		public static <K, V> Comparator<MyMap.Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
			// 此处有一个Object类中的判断cmp不是null的过程，但是该方法是包内可见的，所以无法调用
			return (Comparator<MyMap.Entry<K, V>> & Serializable) (e1, e2) -> cmp.compare(e1.getKey(), e2.getKey());
		}// 传入一个比较器，相当于指定泛型参数的可比较性

		public static <K, V> Comparator<MyMap.Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
			// 此处有一个Object类中的判断cmp不是null的过程，但是该方法是包内可见的，所以无法调用
			return (Comparator<MyMap.Entry<K, V>> & Serializable) (e1, e2) -> cmp.compare(e1.getValue(), e2.getValue());
		}
	}

	boolean equals(Object o);

	int hashCode();

	/*
	 * 以下所有的default方法全部是java1.8的新特性
	 */
	default V getOrDefault(Object key, V defaultValue) {
		V v;
		return (((v = get(key)) != null) || containsKey(key)) ? v : defaultValue;
		// 之前已经通过判断get是否为null 为什么还要判断containsKey?
	}

	default void forEach(BiConsumer<? super K, ? super V> action) {
		Objects.requireNonNull(action);// 为空直接就抛出异常
		for (MyMap.Entry<K, V> entry : entrySet()) {
			K k;
			V v;
			try {
				k = entry.getKey();
				v = entry.getValue();
			} catch (IllegalStateException ise) {
				// 通常表示该entry不在map中了
				throw new ConcurrentModificationException(ise);
			}
			action.accept(k, v);
		}
	}// 该默认方法为每一个键值对执行一个传入的函数所执行的方法

	default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		Objects.requireNonNull(function);// Object类中的判断是否为空
		for (MyMap.Entry<K, V> entry : entrySet()) {
			K k;
			V v;
			try {
				k = entry.getKey();
				v = entry.getValue();
			} catch (IllegalStateException ise) {
				// 多半是因为元素已经不在map中引起的，多线程造成
				throw new ConcurrentModificationException(ise);
			}

			v = function.apply(k, v);

			try {
				entry.setValue(v);
			} catch (IllegalStateException ise) {
				// 多半是因为元素已经不在map中引起的，多线程造成
				throw new ConcurrentModificationException(ise);
			}
		}
	}// 进行一次键值对的操作得到新的值，替换原来的值，键不变

	default V putIfAbsent(K key, V value) {
		V v = get(key);
		if (v == null) {
			v = put(key, value);
		}
		return v;
	}// 如果键值对缺少值则补上

	default boolean remove(Object key, Object value) {
		Object curValue = get(key);
		if (!Objects.equals(curValue, value) || (curValue == null && !containsKey(key))) {
			return false;
		} // 前面可能是value和curValue全都为null
		remove(key);
		return true;
	}// 必须要键相同，值也相同才能删除

	// TODO？？？？？为什么remove用Object但是replace用反省参数

	default boolean replace(K key, V oldValue, V newValue) {
		V curValue = get(key);
		if (!Objects.equals(oldValue, curValue) || (curValue == null && !containsKey(key))) {
			return false;
		}
		put(key, newValue);
		return true;
	}// 必须要键和值都相等才能够替换值

	default V replace(K key, V value) {
		V curValue;
		if ((curValue = get(key)) != null || containsKey(key)) {// 该处判断是否是存在键但是该键的值为null
			curValue = put(key, value);
		}
		return curValue;
	}// 如果有Key则替换该值，并且返回值

	default V computeIfAbsent(K key, Function<? super K, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		V v;
		if ((v = get(key)) == null) {
			V newValue;
			if ((newValue = remappingFunction.apply(key)) != null) {
				put(key, newValue);
				return newValue;
			}
		}
		return v;
	}// 如果不存在键位key的值，则通过传入的函数来计算得到值并设置该键值，并返回值

	default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		V oldValue;
		if ((oldValue = get(key)) != null) {
			V newValue = remappingFunction.apply(key, oldValue);
			if (newValue != null) {
				put(key, newValue);
				return newValue;
			}
			else {
				remove(key);
				return null;
			}
		}
		else {
			return null;
		}
	}// 如果存在某个键和值则通过传入的函数接口计算出新的值并设置和返回，如果新的函数接口返回值为null则删除该键值对

	default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		V oldValue = get(key);
		V newValue = remappingFunction.apply(key, oldValue);// 不管是否有键或值，都进行操作
		if (newValue == null) {
			if (oldValue != null || containsKey(key)) {// 如果存在键但是键的值是null
				remove(key);
				return null;
			}
			else {
				return null;
			}
		}
		else {
			put(key, newValue);
			return newValue;
		}
	}// 不管是否存在键或者值，都先用传入的函数接口先计算，如果没有得到结果则判断并删除键

	default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		Objects.requireNonNull(value);
		V oldValue = get(key);
		V newValue = oldValue == null ? value : remappingFunction.apply(oldValue, value);
		if (newValue == null) {
			remove(key);
		}
		else {
			put(key, newValue);
		}
		return newValue;
	}// 将旧的键值和新的键值通过传入的函数接口进行计算得到新值，穿回到map中，如果得不到新值，则删除键
}
