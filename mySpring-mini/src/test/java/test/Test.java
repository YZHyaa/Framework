package test;

public class Test {

    public Integer i1;

    public void test() {
        String name1 = this.getClass().getName();
        String name2 = this.getClass().getSimpleName();
        System.out.println(name1);
        System.out.println(name2);
    }
}
