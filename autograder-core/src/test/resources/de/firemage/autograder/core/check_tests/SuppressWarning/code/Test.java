import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("checkstyle:foo") // not ok
public class Test {
    @SuppressWarnings("unused") // not ok
    private int x;

    @SuppressWarnings("unchecked") // not ok
    public void dirtyThings() {
        @SuppressWarnings({"unchecked", "rawtypes"}) // not ok
        List<String> list = new ArrayList();
    }

    @Override // ok
    public String toString() {
        return "foo";
    }
}
