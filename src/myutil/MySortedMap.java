package myutil;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public interface MySortedMap<K, V> extends MyMap<K, V> {

	// 返回比较器
	Comparator<? super K> comparator();

	// 返回排好序的map的一部分
	MySortedMap<K, V> subMap(K fromKey, K toKey);

	// 返回排好序的map的前部分
	MySortedMap<K, V> headMap(K toKey);

	// 返回排好序的map的尾部部分
	MySortedMap<K, V> tailMap(K fromKey);

	// 返回第一个key
	K firstKey();

	// 返回最后一个key
	K lastKey();

	// 返回键的集合
	Set<K> keySet();

	// 返回值的collection
	Collection<V> values();

	// 返回元组集合
	Set<MyMap.Entry<K, V>> entrySet();
}
