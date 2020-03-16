package com.piaopiao.ymhbutterknife;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
        ArrayList<String> datas = new ArrayList<>();
        datas.add("hahah");
        Class<? extends ArrayList> clazz = datas.getClass();
        try {
            Method addMethod = clazz.getMethod("add", Object.class);
            addMethod.invoke(datas, 12);
            System.out.println(datas.size());
            for (int i = 0; i < datas.size(); i++) {
                System.out.println(datas.get(i) + "======");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}