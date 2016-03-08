package mylang;

import java.io.Serializable;
import java.lang.Comparable;
import java.util.Arrays;

/**
 * 参考源码实现String类
 */
public final class MyString implements Serializable, Comparable<MyString> {

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

	@Override
	public int compareTo(MyString anotherString) {
		return 0;
	}
}
