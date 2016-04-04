package myutil;

import java.util.NavigableSet;

/*
 * 可导航的map，该接口提供一些对map的搜索功能
 */
public interface MyNavigableMap<K, V> extends MySortedMap<K, V> {

	// 获取小于指定key的第一个节点对象
	MyMap.Entry<K, V> lowerEntry(K key);

	K lowerKey(K key);

	// 获取小于等于指定key的第一个节点对象
	MyMap.Entry<K, V> floorEntry(K key);

	K floorKey(K key);

	// 获取大于或等于指定key的第一个节点对象
	MyMap.Entry<K, V> ceilingEntry(K key);

	K ceilingKey(K key);

	// 获取大于指定key的第一个节点对象
	MyMap.Entry<K, V> higherEntry(K key);

	K higherKey(K key);

	// 获取map按照排序的第一个（最小序）节点对象
	MyMap.Entry<K, V> firstEntry();

	// 获取map按照排序的最后一个（最大序）节点对象
	MyMap.Entry<K, V> lastEntry();

	// 获取map的第一个节点元素，并从map中移除该元素
	MyMap.Entry<K, V> pollFirstEntry();

	// 获取map的最后一个节点元素，并从map中移除该元素
	MyMap.Entry<K, V> pollLastEntry();

	// 返回当前map的逆序map
	MyNavigableMap<K, V> descendingMap();

	// 返回当前map的键的集合
	NavigableSet<K> navigableKeySet();

	// 返回当前map的键的集合的逆序集合
	NavigableSet<K> descendingKeySet();

	// 返回当前的位于fromKey和toKey之间的map，两个布尔值表示是否包含两个边界
	MyNavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

	// 返回开头到toKey的map布尔值表示是否包含边界
	MyNavigableMap<K, V> headMap(K toKey, boolean inclusive);

	// 返回从fromKey到结尾的map，布尔值表示是否包含边界
	MyNavigableMap<K, V> tailMap(K fromKey, boolean inclusive);

	// fromKey包含，toKey不包含
	MySortedMap<K, V> subMap(K fromKey, K toKey);

	// 不包含toKey
	MySortedMap<K, V> headMap(K toKey);

	// fromKey不包含
	MySortedMap<K, V> tailMap(K fromKey);
}
/*
 * 参考资料：
 * 1.http://www.importnew.com/17620.html
 */
