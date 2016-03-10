package mylang;

import java.io.Serializable;
import java.lang.annotation.Native;

/*
 */
public class MyInteger extends Number implements Serializable {
	private static final long serialVersionUID = 3605746528720172077L;

	@Native // 最小的int值,该注解表示
	public static final int MIN_VALUE = 0x80000000;
	// http://stackoverflow.com/questions/28770822/why-is-the-size-constant-only-native-for-integer-and-long
	// 最大的int值
	@Native
	public static final int MAX_VALUE = 0x7fffffff;

	public static final Class<Integer> TYPE = Integer.TYPE;// 表示int类型的class实例

	// 所有能用来将数字表示为字符串的字符
	final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
			'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	// 将一个int型的数转化为一个任意进制的数
	public static String toString(int i, int radix) {
		// radix是进制，为一个2-36（10个数字加上26个字母）的整数，否则取10
		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
			radix = 10;
		}
		if (radix == 10) {
			return toString(i);
		}

		return null;
	}

	// 将一个整型数转化为十进制数
	public static String toString(int i) {
		if (i == MIN_VALUE) {
			return "-2147483648";
		} // 该数是唯一没有int型正整数绝对值的数
		int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);// 得到转化为string之后的长度，包括符号
		char[] buf = new char[size];
		getChars(i, size, buf);
		return new String(buf);// 源码是使用buf数组来创建的，该方法只有lang包内部可以访问
	}

	final static char[] DigitTens = { //
			'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
			'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', //
			'2', '2', '2', '2', '2', '2', '2', '2', '2', '2', //
			'3', '3', '3', '3', '3', '3', '3', '3', '3', '3', //
			'4', '4', '4', '4', '4', '4', '4', '4', '4', '4', //
			'5', '5', '5', '5', '5', '5', '5', '5', '5', '5', //
			'6', '6', '6', '6', '6', '6', '6', '6', '6', '6', //
			'7', '7', '7', '7', '7', '7', '7', '7', '7', '7', //
			'8', '8', '8', '8', '8', '8', '8', '8', '8', '8', //
			'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',//
	};

	final static char[] DigitOnes = { //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',//
	};

	static void getChars(int i, int index, char[] buf) {
		int q, r;
		int charPos = index;
		char sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}
		// 以下都当作正整数处理，使用位操作，提高速率

		// 两位两位的得到
		while (i >= 65536) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));// 将乘以100拆成三个位运算和加法，提高效率
			i = q;
			buf[--charPos] = DigitOnes[r];// 得到个位数
			buf[--charPos] = DigitTens[r];// 得到十位数，通过表的方式，设计的太逆天了，，，我服
		}

		for (;;) {
			q = (i * 52429) >>> (16 + 3);// 因为：2^19=524288,所以该结果近似于i/10,只有当i<65536的时候可以近似，否则i*52429会溢出
			r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...将10拆成2^3+2^1
			buf[--charPos] = digits[r];
			i = q;
			if (i == 0) break;
		}

		if (sign != 0) {
			buf[--charPos] = sign;
		} // 加上符号
	}// 该方法是将Integer转化为String的最主要算法，设计精妙，效率高

	// 表示十进制数中每个位数的最大值
	final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

	// 计算正整数的十进制长度，x必须是正整数,用表驱动的方式，sizeTable为静态常量，加快速度
	static int stringSize(int x) {
		for (int i = 0;; i++) {
			if (x <= sizeTable[i]) {
				return i + 1;
			}
		}
	}

	@Override
	public int intValue() {
		return 0;
	}

	@Override
	public long longValue() {
		return 0;
	}

	@Override
	public float floatValue() {
		return 0;
	}

	@Override
	public double doubleValue() {
		return 0;
	}
}
