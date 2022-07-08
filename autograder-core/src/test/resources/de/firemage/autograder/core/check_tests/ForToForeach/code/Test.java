package de.firemage.autograder.core.check_tests.ForToForeach.code;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6);
        
        for (int i = 0; i < list.size(); i++) { // Should be for-each
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i++) { // Should be for-each
            System.out.println(list.get(i));
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i += 2) { // Ok
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size() - 1; i += 2) { // Ok
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i++) { // Ok
            System.out.println(i);
        }

        for (int i : list) { // Ok
            System.out.println(i);
        }
    }
}
