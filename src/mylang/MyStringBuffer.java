package mylang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public final class MyStringBuffer extends MyAbstractStringBuilder implements Serializable {
	/*
	 * 该类的几乎所有方法都是调用abstractStringBuilder的对应方法并且加上同步关键字synchronized，只是：
	 * 在需要修改值字符串的时候设置toStringCache的值为null,
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 4252559514900245238L;

	private transient char[] toStringCache;// transient表示该值在序列化的时候不被序列化，缓存，如果没有被改变可以不重新计算

	@Override
	public String toString() {
		if (toStringCache == null) {
			toStringCache = Arrays.copyOfRange(value, 0, count);
		}
		return new String(toStringCache);// 该处本应该使用String类内部的直接使用传进去的字符串的方法，但该方法只是包内可见
	}

	/*
	 * 以下内容是与序列化有关的
	 * serialVersionUID要相同才能够实现反序列化
	 * 序列化的时候：静态变量不会被序列化，因为其属于类变量，序列化是针对于对象的
	 * 父类也要实现serializable接口才能够同时被序列化，否则父类要有无参构造函数
	 * 敏感字段在序列化的时候要加密
	 */

	// 定义被序列化的域
	private static final java.io.ObjectStreamField[] serialPersistentFields = {
			new java.io.ObjectStreamField("value", char[].class), //
			new java.io.ObjectStreamField("count", Integer.TYPE), //
			new java.io.ObjectStreamField("shared", Boolean.TYPE), };

	private synchronized void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField fields = s.putFields();
		fields.put("value", value);
		fields.put("count", count);
		fields.put("shared", false);
		s.writeFields();
	}

	private synchronized void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
		ObjectInputStream.GetField fields = s.readFields();
		value = (char[]) fields.get("value", null);// 如果沒有找到就用后面的参数值
		count = fields.get("count", 0);
	}
}
