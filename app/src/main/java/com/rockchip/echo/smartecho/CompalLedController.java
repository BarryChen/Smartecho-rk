package com.rockchip.echo.smartecho;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by yhc on 16-12-23.
 */

public class CompalLedController {
    public static void setLedState(String group, int num, int brightness) {
//        LogUtil.d("LedController - setLedState - group: " + group + " num: " + num
//                + " brightness:" + brightness + " ");
        try {
            FileWriter fw = new FileWriter("/sys/class/leds/" + group + "_D" + num + "/brightness");
            fw.write(String.valueOf(brightness));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setGroupLedState(String group, int brightness) {
        for (int i = 1; i <= 9; i++) {
            if(i == 6) continue;
            CompalLedController.setLedState(group, i, brightness);
        }
    }

    public static void setAllLedOn() {
        for (int i = 1; i <= 9; i++) {
            if(i == 6) continue;
            CompalLedController.setLedState("A", i, 255);
            CompalLedController.setLedState("B", i, 255);
        }
    }

    public static void setAllLedOff() {
        for (int i = 1; i <= 9; i++) {
            if(i == 6) continue;
            CompalLedController.setLedState("A", i, 0);
            CompalLedController.setLedState("B", i, 0);
        }
    }

    public static void flashAllLed() {
        try {
            setAllLedOff();
            Thread.sleep(500);
            setAllLedOn();
            Thread.sleep(500);
            setAllLedOff();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public static void controlLedOnListerner(boolean isShowLedGroupA) {
        if (isShowLedGroupA) {
            CompalLedController.setGroupLedState("A", 255);
            CompalLedController.setGroupLedState("B", 0);
        } else {
            CompalLedController.setGroupLedState("A", 0);
            CompalLedController.setGroupLedState("B", 255);
        }
    }
}
