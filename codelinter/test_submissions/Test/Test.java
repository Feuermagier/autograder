import java.util.*;

public class Test {
	public static void main(String[] args) {
		new Test().bar();
	}

	public Object bar() {
		foo();
		return null;
	}

	public Object foo() {
		return new Object();
	}
}
