import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

import mylang.MyInteger;
import mylang.MyString;

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

		System.out.println(MyInteger.toUnsignedString(-4335234, 16));
		System.out.println(Integer.toHexString(-4335234));
		System.out.println(MyInteger.parseInt("110", 2));
		System.out.println(MyInteger.parseUnsignedInt("-1234", 10));
	}

	public static void fn(int... is) {
		System.out.println(is[3]);
	}
}
