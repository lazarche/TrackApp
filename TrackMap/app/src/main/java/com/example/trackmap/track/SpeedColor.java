package com.example.trackmap.track;

import android.graphics.Color;

import com.google.android.gms.maps.model.StyleSpan;

import java.util.List;

public class SpeedColor {
    StyleSpan style;
    float speedLimit;

    public SpeedColor(StyleSpan style, float speedLimit) {
        this.style = style;
        this.speedLimit = speedLimit;
    }

    public StyleSpan getStyle() {
        return style;
    }

    public void setStyle(StyleSpan style) {
        this.style = style;
    }

    public float getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(float speedLimit) {
        this.speedLimit = speedLimit;
    }

    public boolean isUnder(float limit) {
        if(speedLimit < limit)
            return  true;
        return  false;
    }

    public static SpeedColor getMatchingColor(List<SpeedColor> list, float speed) {
        if(list == null)
            return null;

        if(list.size() == 0)
            return null;

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).isUnder(speed))
                return list.get(i);
        }

        //Return largest
        return  list.get(list.size()-1);
    }
}
