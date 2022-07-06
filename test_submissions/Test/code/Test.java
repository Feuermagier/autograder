import java.util.*;

public class Test {
	private List<Bar> list;
	private Bar bar;
	
	private void foo() {
		this.bar.a();
		this.bar.a();
	}
}

class Bar {
	public void a() {
		
	}
}
