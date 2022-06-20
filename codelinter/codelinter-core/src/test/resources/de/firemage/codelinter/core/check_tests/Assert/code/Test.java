public class Test {
    public static void main(String[] args) {
        assert true;
        int x = 4;
        assert false: "Hi";
        if (true) {
            assert true != false;
        }
    }
}
