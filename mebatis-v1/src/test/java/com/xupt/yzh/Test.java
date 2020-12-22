package com.xupt.yzh;

public class Test {

    /**
     * 测试Properties文件的转义字符匹配
     */
    public static void main() {
        String input = "%d %s %d";
        Object[] objs = new Object[]{1,"2",3};
        String result = String.format(input,objs);
        System.out.println(result);
    }
}
