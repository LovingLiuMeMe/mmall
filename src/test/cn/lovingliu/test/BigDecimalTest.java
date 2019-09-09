package cn.lovingliu.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-09
 */
public class BigDecimalTest {
    @Test
    public void test1(){
        System.out.println(0.5 + 0.1);
        System.out.println(1.0 - 0.42);
        System.out.println(4.015 * 100);
        System.out.println(123.3 / 100);
//        0.6
//        0.5800000000000001
//        401.49999999999994
//        1.2329999999999999
    }

    @Test
    public void test2(){
        BigDecimal b1 = new BigDecimal(0.5);
        BigDecimal b2 = new BigDecimal(0.1);

        System.out.println(b1.add(b2)); //0.6000000000000000055511151231257827021181583404541015625 好像更严重了
    }

    @Test
    public void test3(){
        // 使用String构造器 避免精度丢失
        BigDecimal b1 = new BigDecimal("0.5");
        BigDecimal b2 = new BigDecimal("0.1");

        System.out.println(b1.add(b2)); //0.6
    }

}
