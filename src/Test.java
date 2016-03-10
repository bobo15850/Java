import java.io.UnsupportedEncodingException;

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

		System.out.println(MyInteger.toString(0x34345345));
	}

	public static void fn(int... is) {
		System.out.println(is[3]);
	}
}
