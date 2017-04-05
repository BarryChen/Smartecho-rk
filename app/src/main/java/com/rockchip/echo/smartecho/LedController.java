package com.rockchip.echo.smartecho;

import com.rockchip.echo.util.LogUtil;

import java.io.FileWriter;
import java.io.IOException;

public class LedController {

    public static final String FILE_LED_SWITCH = "/sys/bus/i2c/drivers/sn3199_i2c/1-0067/led_switch";
    public static final String FILE_LED_MODE = "/sys/bus/i2c/drivers/sn3199_i2c/1-0067/led_mode";

    public static void setLedState(int num, int isOn) {
//        LogUtil.d("LedController - setLedState - group: " + group + " num: " + num
//                + " brightness:" + brightness + " ");
        try {
            FileWriter fw = new FileWriter(FILE_LED_SWITCH);
            fw.write(String.valueOf(""+num+isOn));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setGreenLedState(int isOn) {
        setLedState(9, isOn);
    }

    public static void setAllBlueLedOn() {
        for (int i = 1; i <= 6; i++) {
            setLedState(i, 1);
        }
    }

    public static void setAllBlueLedOff() {
        for (int i = 1; i <= 6; i++) {
            setLedState(i, 0);
        }
    }

}
