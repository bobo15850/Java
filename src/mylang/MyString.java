package mylang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 参考源码实现String类
 */
public final class MyString implements Serializable, Comparable<MyString>, CharSequence {

	private static final long serialVersionUID = -6679565187217629939L;

	private final char[] value;// 存储每一个字符

	private int hash;// 缓存hash值

	public MyString() {
		this.value = new char[0];
	}// 无参构造函数，将字符数组初始化为长度为零

	public MyString(MyString original) {
		this.value = original.value;
		this.hash = original.hash;
	}// 用一个字符串初始化一个字符串

	public MyString(char[] value) {
		this.value = Arrays.copyOf(value, value.length);
	}// 使用字符数组初始化，该数组要进行深拷贝

	public MyString(char[] value, int offset, int count) {
		if (offset < 0) {
			throw new StringIndexOutOfBoundsException(offset);
		}
		if (count < 0) {
			throw new StringIndexOutOfBoundsException(count);
		}
		if (offset > value.length - count) {
			throw new StringIndexOutOfBoundsException(offset + count);
		}
		this.value = Arrays.copyOfRange(value, offset, offset + count);// 做深拷贝
	}// 用一个字符数组的一部分来初始化一个String,offset表示起始字符，count表示长度

	public MyString(int[] codePoints, int offset, int count) {
		if (offset < 0) {
			throw new StringIndexOutOfBoundsException(offset);
		}
		if (count < 0) {
			throw new StringIndexOutOfBoundsException(count);
		}
		if (offset < codePoints.length - count) {
			throw new StringIndexOutOfBoundsException(offset + count);
		}
		final int end = offset + count;

		// 首先计算需要多少位，BMP占16位，但是增补字符占了两个字符的位数32，所以需要计算一共需要多少位char来表示，如果遇到增补字符，则分成高位和地位分别用一个char类型表示
		// 0x0000~0xffff之间为基本多语言面，之后的0x10000~0x10ffff为增补字符
		int n = count;
		for (int i = offset; i < end; i++) {
			int c = codePoints[i];
			if (Character.isBmpCodePoint(c)) {
				continue;
			}
			else if (Character.isValidCodePoint(c)) {
				n++;
			}
			else {
				throw new IllegalArgumentException(Integer.toString(c));
			}
		} // 计算真是所需的char字符位数

		// 再者，将没有无法识别字符的数字数组转化为char数组
		char[] v = new char[n];
		for (int i = offset, j = 0; i < end; i++, j++) {
			int c = codePoints[i];
			if (Character.isBmpCodePoint(c)) {
				v[j] = (char) c;
			}
			else {
				// 模拟toSurrogates方法
				v[j++] = Character.highSurrogate(c);// 高位
				v[j] = Character.lowSurrogate(c);// 低位
			}
		}
		this.value = v;
	}

	// TODO
	/*
	 * 省略了包含编码方式的构造函数，以及以byte数组初始化的构造函数
	 */

	// TODO
	/*
	 * 省略了以StringBuffer和StringBuilder构造String的方式
	 */

	MyString(char[] value, boolean shared) {
		this.value = value;
	}// 包内部构造函数。以共用char数组的形式创建String，其中shared的参数用来区分于另一个没有该参数的构造函数，并没有实际作用，始终未true

	public int length() {
		return value.length;
	}

	public boolean isEmpty() {
		return value.length == 0;
	}// 判断字符串是否为空

	public char charAt(int index) {
		if (index < 0 || index >= value.length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return value[index];
	}

	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof MyString) {
			MyString anotherString = (MyString) anObject;
			int n = anotherString.value.length;// 尽量减少函数调用，使用属性效率比函数调用高
			if (n == value.length) {
				int i = 0;
				char[] v1 = this.value;
				char[] v2 = anotherString.value;
				while (n-- != 0) {
					if (v1[i] != v2[i]) {
						return false;
					}
					i++;
				}
				return true;
			}
		}
		return false;
	}

	public int compareTo(MyString anotherString) {
		int len1 = value.length;
		int len2 = anotherString.value.length;
		int lim = Math.min(len1, len2);
		char[] v1 = value;
		char[] v2 = anotherString.value;

		int k = 0;
		while (k < lim) {
			char c1 = v1[k];
			char c2 = v2[k];
			if (c1 != c2) {
				return c1 - c2;
			}
			k++;
		}
		return len1 - len2;
	}// 比较字典序

	public boolean regionMatches(int toffset, MyString other, int ooffset, int len) {
		char[] ta = value;
		int to = toffset;
		char[] pa = other.value;
		int po = ooffset;
		if (to < 0 || po < 0 || (to > value.length - len) || (po > value.length - len)) {
			return false;
		}
		while (len-- > 0) {
			if (ta[to++] != pa[po++]) {
				return false;
			}
		}
		return true;
	}// 某一区域是否匹配,参数分别表示this的起始位置，另一个字符串，另一个的起始位置，区域长度

	public boolean startsWith(MyString prefix, int toffset) {
		char ta[] = value;
		int to = toffset;
		char pa[] = prefix.value;
		int po = 0;
		int pc = prefix.value.length;
		if (toffset < 0 || toffset > value.length - pc) {
			return false;
		}
		while (pc-- > 0) {
			if (ta[to++] != pa[po++]) {
				return false;
			}
		}
		return true;
	}// 判断某一个字符串是否以某个前缀

	public int hashCode() {
		int h = hash;
		if (h == 0 && value.length > 0) {
			for (int i = 0; i < value.length; i++) {
				h = 31 * h + value[i];
			}
			hash = h;
		}
		return h;
	}// 加入缓存机制,因为String是不可变类，hash的值确定之后基本不会改变，在第一次使用的时候才会初始化

	public int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset,
			int targetCount, int fromIndex) {
		// TODO
		if (fromIndex >= sourceCount) {
			return targetCount == 0 ? sourceCount : -1;
		} // 开始查找的位置已经大于了被查找字符串的长度
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}
		char first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);// 最后的开始位置，再往后就无法满足targetCount
		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			} // 寻找到一个等于要查找的第一个字符的位置
			if (i <= max) {
				// 从接下来的第二个开始
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
					;
				if (j == end) {
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}// 得到被查找字符串中第一个查找字符串开始的位置

	public MyString substring(int beginIndex) {
		if (beginIndex < 0) {
			throw new StringIndexOutOfBoundsException(beginIndex);
		}
		int subLen = value.length - beginIndex;
		if (subLen < 0) {
			throw new StringIndexOutOfBoundsException(subLen);
		}
		return beginIndex == 0 ? this : new MyString(value, beginIndex, subLen);
	}

	public MyString substring(int beginIndex, int endIndex) {
		if (beginIndex < 0) {
			throw new StringIndexOutOfBoundsException(beginIndex);
		}
		if (endIndex > value.length) {
			throw new StringIndexOutOfBoundsException(endIndex);
		}
		int subLen = endIndex - beginIndex;
		if (subLen < 0) {
			throw new StringIndexOutOfBoundsException(subLen);
		}
		return (beginIndex == 0) && (endIndex == value.length) ? this : new MyString(value, beginIndex, subLen);

	}//

	public MyString concat(MyString str) {
		int otherLen = str.length();
		if (otherLen == 0) {
			return this;
		}
		int len = value.length;
		char[] buf = Arrays.copyOf(value, otherLen + len);
		// 源码实现是通过本地方法
		/*
		 * 模拟实现
		 */
		for (int i = otherLen, j = 0; i < buf.length; i++, j++) {
			buf[i] = str.value[j];
		}
		return new MyString(buf, true);
		/**/
	}

	public MyString replace(char oldChar, char newChar) {
		if (oldChar != newChar) {
			int i = -1;
			char[] var = value;
			int len = var.length;
			while (++i < len) {
				if (var[i] == oldChar) {
					break;
				}
			}
			if (i < len) {
				char[] buf = new char[len];
				for (int j = 0; j < i; j++) {
					buf[j] = var[j];
				}
				while (i < len) {
					buf[i] = var[i] == oldChar ? newChar : var[i];
					i++;
				}
				return new MyString(buf, true);
			} // i<len表示在上一次的循环中发现了oldChar，否则i应该等于len
		}
		return this;
	}

	// 模拟split单个字符,匹配多个字符的需要用到正则表达式解析
	public MyString[] split(char c) {
		ArrayList<MyString> list = new ArrayList<MyString>();
		int pre = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] == c) {
				list.add(substring(pre, i));
				pre = i + 1;
			}
		}
		if (pre != value.length) {
			list.add(substring(pre));
		}
		MyString[] result = new MyString[list.size()];
		return list.toArray(result);
	}// 需要O（n）时间复杂度

	public MyString trim() {
		int len = value.length;
		int start = 0;
		char[] var = value;
		while (start < len && var[start] <= ' ') {
			start++;
		} // space之前的都是不可显示字符，该方法不仅仅是去除space还要去除tab 回车等所有不可显示字符
		while (len > start && var[len - 1] <= ' ') {
			len--;
		}
		return (start == 0 && len == value.length) ? this : substring(start, len);
	}

	/*
	 * String 的intern方法为本地方法，作用是：如果常量池中包含等于该String的字符串则返回运行时常量池中的String对象，如果没有的话
	 * 则将次String包含的字符串添加到常量池中
	 */
	public String toString() {
		return new String(value);
	}

	public CharSequence subSequence(int start, int end) {
		return substring(start, end);
	}

}
