package com.example;

public class T2 {
    public void test(int[] x) {
        System.out.println("test method start");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println("test method end");
    }
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
