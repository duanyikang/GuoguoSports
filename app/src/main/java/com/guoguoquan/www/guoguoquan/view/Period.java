package com.guoguoquan.www.guoguoquan.view;

/**
 * @author 小段果果
 * @time 2016/5/13  17:36
 * @E-mail duanyikang@mumayi.com
 */

public class Period {
    public int minutes = 0;
    public int seconds = 0;
    public int tenOfSec = 0;

    Period() {
    }


    Period(int minutes, int seconds, int tenOfSec) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.tenOfSec = tenOfSec;
    }
}
