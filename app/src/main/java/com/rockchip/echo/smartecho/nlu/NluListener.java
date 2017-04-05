package com.rockchip.echo.smartecho.nlu;


public interface NluListener {
    void onResult(NluResult result);
    void onError(NluError error);
}
