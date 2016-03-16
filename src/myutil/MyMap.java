package myutil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

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

	void putAll(Map<? extends K, ? extends V> map);// 将一个map中的元素全部添加到另一个中，不是线程安全的

	void clear();// 将map中的所有元素删除

	Set<K> keySet();// 返回所有键的集合

	// TODO 因为key没有重复的值所以可以当作一个set返回，但是value可能重复，还有其他原因吗？？？？？？？
	// 基本上每一个实现类都会自己实现一个values的类型作为返回值

	Collection<V> values();// 返回所有值，因为可能会存在重复的值，所以不能不用Set

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
}
