package com.mrfox.arrirtty.remoting;

import java.time.LocalDateTime;
import java.time.Month;

public class sds {
    public static void main(String[] args) {
        int year = LocalDateTime.now().getYear();
        LocalDateTime localDateTime = LocalDateTime.of(year, Month.DECEMBER, 25, 0, 0);

        System.out.println(localDateTime.isBefore(localDateTime.now()));
    }
}
