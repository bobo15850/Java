package mylang;

import java.io.Serializable;
import java.lang.annotation.Native;

/*
 * 大量使用平行算法，0x55555555,0x33333333,0x0f0f0f0f,0x00ff00ff,是非常有用的位操作数
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
		} // 十进制的处理方法效率更高

		char buf[] = new char[33];
		boolean negative = (i < 0);
		int charPos = 32;

		if (!negative) {
			i = -i;
		} // 将i变成复数

		// 用负数来做运算，可以防止MIN_VALUE的绝对值大于MAX_VALUE绝对值的错误
		while (i < -radix) {
			buf[charPos--] = digits[-(i % radix)];
		}
		buf[charPos] = digits[-i];// 最后一位,保证charPos就是当前位

		if (negative) {
			buf[--charPos] = '-';
		}
		return new String(buf, charPos, 33 - charPos);
	}// 将一个二进制补码转化为任意进制的数，radix在2~36之间

	// 返回无符号整数转化成的字符串，十进制
	public static String toUnsignedString(int i) {
		return Long.toString(toUnsignedLong(i));
	}

	// 返回无符号整数转化成的16进制字符串
	public static String toHexString(int i) {
		return toUnsignedString0(i, 4);
	}

	// 返回无符号整数转化成的8进制字符串
	public static String toOctalString(int i) {
		return toUnsignedString0(i, 3);
	}

	// 返回无符号整数转化成的2进制字符串
	public static String toBinaryString(int i) {
		return toUnsignedString0(i, 1);
	}

	// 得到无符号的字符串，2^shift表示进制
	private static String toUnsignedString0(int val, int shift) {
		int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);// 有效位的个数
		int chars = Math.max((mag + shift - 1) / shift, 1);
		char[] buf = new char[chars];
		formatUnsignedInt(val, shift, buf, 0, chars);
		return null;
	}

	// 将无符号数转化为特定2，8，16进制的数，2^shift为进制，buf为最终的字符串，len是在toUnsignedString0中已经计算好的
	static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
		int charPos = len;
		int radix = 1 << shift;
		int mask = radix - 1;
		do {
			buf[offset + --charPos] = digits[val & mask];// 该步骤得到每一位的值
			val >>>= shift;
		} while (val != 0 && charPos > 0);
		return charPos;// 返回最低被使用的位
	}

	// 返回无符号整数转化成的字符串，后一个参数是进制
	public static String toUnsignedString(int i, int radix) {
		return Long.toUnsignedString(toUnsignedLong(i), radix);
	}

	// 转化为一个无符号长整型
	public static long toUnsignedLong(int x) {
		return ((long) x) & 0xffffffffL;
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

	// 返回该数取最高位的1之后全都补0的值
	public static int highestOneBit(int i) {
		// HD, Figure 3-1
		i |= (i >> 1);// 将最高两位设为1
		i |= (i >> 2);// 最高四位设为1
		i |= (i >> 4);// 最高8位设为1 // 左边全部是：如果有的话就设为1
		i |= (i >> 8);// 最高16位设为1
		i |= (i >> 16);// 最高32位设为1
		// 以上全部作用是将i最高位开始往下全部设为1
		return i - (i >>> 1);// 減去除最高位之后剩下的位
	}

	// 返回最低位的一其余全是0的值
	public static int lowestOneBit(int i) {
		return i & -i;
	}// 因为取反之后加上一只有原来最右边是1的位会任然变成1，该位左侧的位与原先相反，
		// 该位右侧的位原来是0，取反之后变为1，再加上1之后全都变为0，所以除了该位，其余位全部是0

	// 得到补码二进制数开头的0个数
	public static int numberOfLeadingZeros(int i) {
		if (i == 0) {
			return 32;
		}
		int n = 1;

		// 以下中心思想是将实验得出的已知存在的零消除,利用二分查找，找到零的个数
		if (i >>> 16 == 0) {// 表明i除了前面的零，其余位数小于等于16
			n += 16;
			i <<= 16;// 将i左移16位将其变大
		}
		if (i >>> 24 == 0) {
			n += 8;
			i <<= 8;
		}
		if (i >>> 28 == 0) {
			n += 4;
			i <<= 4;
		}
		if (i >>> 30 == 0) {
			n += 2;
			i <<= 2;
		}
		if (i >>> 31 == 0) {
			n += 1;
		}
		return n - 1;
	}

	// 取得最右边的零的个数
	public static int numberOfTrailingZeros(int i) {
		int y;
		if (i == 0) {
			return 32;
		}
		// 二分查找加移位算法，效率高于一个循环移位
		int n = 31;
		y = i << 16;
		if (y != 0) {
			n -= 16;
			i = y;
		}
		y = i << 8;
		if (y != 0) {
			n -= 8;
			i = y;
		}
		y = i << 4;
		if (y != 0) {
			n -= 4;
			i = y;
		}
		y = i << 2;
		if (y != 0) {
			n -= 2;
			i = y;
		}
		return n - ((i << 1) >>> 31);
	}

	// 计算‘1’的个数
	public static int bitCount(int i) {
		// HD, Figure 5-2
		i = i - ((i >>> 1) & 0x55555555);// 01010101010101010101010101010101,该步骤结果表示相邻两位有多少各1，并用该两位表示出来
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);// 00110011001100110011001100110011，该步骤结果表示相邻四位有多少个1，并用这四位表示出来
		i = (i + (i >>> 4)) & 0x0f0f0f0f;// 00001111000011110000111100001111，表示相邻八位有多少个1用这八位表示出来
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		return i & 0x3f;// 最多只有32位
	}

	// 将二进制数向左移distance位，移出去的补在右边
	public static int rotateLeft(int i, int distance) {
		return (i << distance) | (i >>> -distance);// 无符号右移-distance相当于右移32-distance位，思路就是截取为两段在拼接(|运算)
	}

	public static int rotateRight(int i, int distance) {
		return (i >>> distance) | (i << -distance);// 类似于rotateLeft
	}

	// 将int的二进制形式反转过来
	public static int myReverse(int i) {
		int tot = 0;
		int mask = 1;
		for (int j = 0; j < 32; j++) {
			int temp = mask & i;// 得到右边开始第j位
			if (j >= 16) {
				temp >>>= ((j << 1) - 31);
			}
			else {
				temp <<= (31 - (j << 1));
			}
			tot |= temp;
			mask <<= 1;
		}
		return tot;
	}

	public static int reverse(int i) {
		// HD, Figure 7-1
		i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;// 相邻两个调换
		i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;// 四个一组倒序
		i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;// 八个一组倒序
		i = (i << 24) | ((i & 0xff00) << 8) | ((i >>> 8) & 0xff00) | (i >>> 24);// 分別得到只有八位，且这八位都在该在的位置其余位都是0的数，再把这四个八位组装起来
		return i;
	}// java源码实现的reverse，呵呵哒，只能膜拜，效率是myreverse的十倍

	// 正数返回1,0返回0，负数返回-1
	public static int signum(int i) {
		// HD, Section 2-7
		return (i >> 31) | (-i >>> 31);
	}// 正数的话前者为0000...，后者为0000，负数的话，前者为111...，后者为0....

	// 一个int有四个byte把这四个byte倒序，但是不改变每一个byte内部的顺序
	public static int reverseBytes(int i) {
		return (i >>> 24) | (i >> 8 & 0x0000ff00) | (i << 8 & 0x00ff0000) | (i << 24);
	}

	// 求和
	public static int sum(int a, int b) {
		return a + b;
	}

	// 求最大值
	public static int max(int a, int b) {
		return Math.max(a, b);
	}

	// 求最小值
	public static int min(int a, int b) {
		return Math.min(a, b);
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
