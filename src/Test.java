import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import myutil.MyTreeMap;

public class Test {
	public static void main(String[] args) throws UnsupportedEncodingException {
		// MyString ms1 = new MyString(new char[] { 's', 'a', 'b', 'b', 's',
		// 'g', 'a', 'a', 'j' });
		// MyString ms2 = new MyString(new char[] { 'a', 'b', 'c' });
		// MyString[] mss = ms1.split('a');
		// for (int i = 0; i < mss.length; i++) {
		// System.out.println(mss[i]);
		// }
		// String s = "sabbsgaaj";
		// String[] ss = s.split("a");
		// for (int i = 0; i < ss.length; i++) {
		// System.out.println(ss[i]);
		// }
		// System.out.println(String.join("^", new String[] { "a", "b", "c", "d"
		// }));
		//
		// MyString ms = new MyString(new char[] { ' ', ' ', ' ', 'a', 'd', 'f',
		// ' ', 'f', ' ' });
		// System.out.println(ms.trim());

		// String s = "abc";
		// String s1 = new String("abc");
		// s1 = s1.intern();
		// System.out.println(s == s1);
		// long start = System.currentTimeMillis();
		// for (int i = 0; i < 10000000; i++) {
		// int num = Integer.reverse(i);
		// if (num == 0) {
		// System.out.println(i);
		// }
		// }
		// long cur = System.currentTimeMillis();
		// for (int i = 0; i < 10000000; i++) {
		// int num = MyInteger.reverse(i);
		// if (num == 0) {
		// System.out.println(i);
		// }
		// }
		// long end = System.currentTimeMillis();
		// System.out.println(cur - start);
		// System.out.println(end - cur);

		// System.out.println(MyInteger.signum(-23));

		// System.out.println(MyInteger.toUnsignedString(-4335234, 16));
		// System.out.println(Integer.toHexString(-4335234));
		// System.out.println(MyInteger.parseInt("110", 2));
		// System.out.println(MyInteger.parseUnsignedInt("1234", 10));
		//
		// new StringBuffer();

		// Map<String, String> map = new HashMap<String, String>();
		// map.put("1", "a");
		// map.put("2", "b");
		// Map<String, String> m = new HashMap<String, String>();
		// m.put("11", "aa");
		// m.putAll(map);
		// System.out.println(m.toString());

		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("11", "aa");
		map.put("2", "b");
		@SuppressWarnings("unchecked")
		Map<String, String> copy = (Map<String, String>) map.clone();
		System.out.println(copy);

		// map.forEach((a, b) -> System.out.println(a + b));
		// Collection<String> values = map.values();
		// System.out.println(values.getClass());
		// map.keySet().stream().sorted((a, b) -> Integer.compare(a.length(),
		// b.length())).forEach((a) -> {
		// System.out.println(a);
		// });
		// map.keySet().stream().mapToInt(k -> k.length()).sorted().forEach((k)
		// -> System.out.println(k));
		// map.keySet().stream().map(k ->
		// k.length()).collect(Collectors.toList());

		// map.computeIfAbsent("22", (k) -> (k));
		// System.out.println(map);

		// map.merge("2", "hhh", (a, b) -> (a + b));
		// System.out.println(map);
		// Collection<String> values = map.values();
		// Iterator<String> i = values.iterator();
		// i.next();
		// i.remove();
		// System.out.println(map);

		// MyTreeMap<String, Integer> myMap = new MyTreeMap<>();
		// myMap.put("a", 1);
		// myMap.put("b", 2);
		// myMap.put("y", 4);
		// System.out.println(myMap.toString());
	}
}
