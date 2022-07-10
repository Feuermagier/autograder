import java.util.*;

public class Test {
	private final Object x;

	public Test() {
		this.x = null;
	}

	public static void main(String[] args) {
		new Test().foo();
	}

	private void foo() {
		Object a = null;
		a = bar();
		Object b;
		if (a == null) {
			b = null;
		} else {
			b = new Object();
		}
		System.out.println(b);
		System.out.println(this.x);
	}

	private Object bar() {
		return new Object();
	}
}
