import java.util.*;

public class Test {
	public Object bar() {
		Object y = new Object();
		boolean b = y == new Object();
		if (b) {
			return null;
		} else {
			y = new Object();
		}
    	return y;
	}

	public Object foo() {
		return new Object();
	}
}
