package mylang;

import java.util.Arrays;

/*
 * StringBuilder和StringBuilder的抽象父类
 */
public abstract class MyAbstractStringBuilder {

	char[] value;// 使用char数组来存放数据，类似于String，只有子类和包内可见

	int count;// 长度大小

	/*
	 * 构造方法，该类无法实例化，只能被继承
	 */
	MyAbstractStringBuilder() {
	}

	MyAbstractStringBuilder(int capacity) {
		value = new char[capacity];
	}

	// 返回长度
	public int length() {
		return count;
	}

	// 返回现在能存储的最大存储能力
	public int capacity() {
		return value.length;
	}

	// 重新分配存储容量
	public void ensureCapacity(int minimumCapacity) {
		if (minimumCapacity > 0) ensureCapacityInternal(minimumCapacity);
	}

	private void ensureCapacityInternal(int minimumCapacity) {
		// overflow-conscious code
		if (minimumCapacity - value.length > 0) expandCapacity(minimumCapacity);
	}

	// 扩大存储能力
	void expandCapacity(int minimunCapacity) {
		int newCapacity = value.length * 2 + 2;
		if (newCapacity - minimunCapacity < 0) {
			newCapacity = minimunCapacity;
		} // 确保新的存储能力至少为原来存储能力的两倍加上2，传进来的参数如果太小的话没有用
		if (newCapacity < 0) {
			if (minimunCapacity < 0) {
				throw new OutOfMemoryError();
			}
			newCapacity = Integer.MAX_VALUE;
		}
		value = Arrays.copyOf(value, newCapacity);
	}

	// 去掉多余的存储能力
	public void trimToSize() {
		if (count < value.length) {
			value = Arrays.copyOf(value, count);
		}
	}

	// 设置长度，如果不足则补充'\0'
	public void setLength(int newLength) {
		if (newLength < 0) {
			throw new StringIndexOutOfBoundsException(newLength);
		}
		ensureCapacityInternal(newLength);
		if (count < newLength) {
			Arrays.fill(value, count, newLength, '\0');
		}
		count = newLength;
	}

	// 返回特定位置的字符
	public char charAt(int index) {
		if ((index < 0) || (index >= count)) throw new StringIndexOutOfBoundsException(index);
		return value[index];
	}

	// 因为BMP字符占16位，但是补充字符占32位，所以可能占有两个位
	public int codePointAt(int index) {
		if (index < 0 || index > count) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return Character.codePointAt(value, index);
	}

	// TODO 关于codePoint的其他操作暂时先不考虑

	// 将字符串中的一段拷贝到另一个
	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		if (srcBegin < 0) {
			throw new StringIndexOutOfBoundsException(srcBegin);
		}
		if (srcEnd < 0) {
			throw new StringIndexOutOfBoundsException(srcEnd);
		}
		if (srcBegin > srcEnd) {
			throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
		}
		System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcEnd);
	}

	// 设置特定位的值
	public void setCharAt(int index, char ch) {
		if (index < 0 || index > count) {
			throw new StringIndexOutOfBoundsException(index);
		}
		value[index] = ch;
	}

	public MyAbstractStringBuilder append(String str) {
		if (str == null) {
			return appendNull();
		}
		int len = str.length();
		ensureCapacityInternal(count + len);
		str.getChars(0, len, value, count);
		count += len;
		return this;
	}

	public MyAbstractStringBuilder append(StringBuffer sb) {
		if (sb == null) {
			return appendNull();
		}
		int len = sb.length();
		ensureCapacityInternal(count + len);
		sb.getChars(0, len, value, count);
		count += len;
		return this;
	}

	// 连接一个“null”字符串
	private MyAbstractStringBuilder appendNull() {
		int c = count;
		ensureCapacityInternal(c + 4);
		final char[] value = this.value;
		value[c++] = 'n';
		value[c++] = 'u';
		value[c++] = 'l';
		value[c++] = 'l';
		count = c;
		return this;
	}

	/*
	 * 其他关于append的函数暂时先不考虑
	 */

	// 删除特定范围内的字符
	public MyAbstractStringBuilder delete(int start, int end) {
		if (start < 0) throw new StringIndexOutOfBoundsException(start);
		if (end > count) end = count;
		if (start > end) throw new StringIndexOutOfBoundsException();
		int len = end - start;
		if (len > 0) {
			System.arraycopy(value, start + len, value, start, count - end);// 将原来的count之后的空内容，复制到start开始，模拟删除
			count -= len;
		}
		return this;
	}

	// 刪除特定字符
	public MyAbstractStringBuilder deleteCharAt(int index) {
		if ((index < 0) || (index >= count)) throw new StringIndexOutOfBoundsException(index);
		System.arraycopy(value, index + 1, value, index, count - index - 1);
		count--;
		return this;
	}

	/*
	 * 中间大部分的内容都是利用Arrays.arraycopy函数来实现的，暂不考虑
	 */

	// 将内容字符串反转，就是相当于有一个对称轴，然后将然后左右对称一下
	public MyAbstractStringBuilder reverse() {
		boolean hasSurrogates = false;
		int n = count - 1;
		for (int j = (n - 1) >> 1; j >= 0; j--) {// 将长度对半分
			int k = n - j;
			char cj = value[j];
			char ck = value[k];
			if (Character.isSurrogate(cj) || Character.isSurrogate(ck)) {
				hasSurrogates = true;
			}
		}
		if (hasSurrogates) {
			reverseAllVaildSurrogatePairs();
		}
		return this;
	}

	// 将被交换了的扩展字符内部两个char的相对位置交换回来
	private void reverseAllVaildSurrogatePairs() {
		for (int i = 0; i < count - 1; i++) {// 因为扩展字符有两位，所以只需要遍历到倒数第二位就可以了
			char ch2 = value[i];
			if (Character.isSurrogate(ch2)) {
				char ch1 = value[i + 1];
				if (Character.isHighSurrogate(ch1)) {
					value[i++] = ch1;
					value[i] = ch2;
				} // 扩展字符集被交换之后，只需要改变两个合起来的位置，不需要改变两者内部的相对位置
			}
		}
	}

	@Override
	public abstract String toString();
}
