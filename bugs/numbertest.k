context NumberTest {
    role Integers {} requires {
        void add(int i);
        int size();
    }

    public NumberTest(int [] intArray) {
        // NOTE: Cannot assign directly to the Role.
        Integers = new List<int>();
        for(int i : intArray) {
            Integers.add(i);
        }

        // NOTE: This line gave an error before, but perhaps fixed now?
        System.out.println(Integers.size());
    }

    public int sum() {
        int s = 0;
        // NOTE: Cannot use Number directly in loop, and even temp assignment 
        // gives error: Type mismatch in initialization of 'temp'.
        List<int> temp = Integers;
        for(int i : temp) {
            s = s + i;
        }
        return s;
    }

    public int sum2() {
        int s = 0;
        // NOTE: Cannot use Number directly in loop
        for(int i : Integers) s = s + i;
        return s;
    }
}

{
    int [] cc = new int[4];
    cc[0] = 4; cc[1] = 3; cc[2] = 2; cc[3] =  1;

    NumberTest test = new NumberTest(cc);
    System.out.println(test.sum());
    System.out.println(test.sum2());
}

/* GOLD:
___________________________________________________________
4
10
10
*/
