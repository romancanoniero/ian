package com.iyr.ian.utils;

import java.util.HashMap;

public class MathUtils {

    public static HashMap calculateNewSizeWithRatio(int newSize, String ratio) throws Exception {
        String[] orientationSplitter = ratio.split(",");
        if (orientationSplitter.length != 2) {
            throw new Exception("ratio orientation String malformed");
        } else {
            String dimensionType = orientationSplitter[0].toUpperCase();
            String[] ratioSplitter = orientationSplitter[1].split(":");
            if (orientationSplitter.length != 2) {
                throw new Exception("ratio String malformed");
            } else {
                Double widthRatio = Double.valueOf(ratioSplitter[0]);
                Double heightRatio = Double.valueOf(ratioSplitter[1]);
                if (dimensionType.compareTo("W") == 0) {
                } else if (dimensionType.compareTo("H") == 0) {


                    Integer newHeight = (int) (newSize * (heightRatio / widthRatio));
                    Integer newWidth = newSize;
                    HashMap newDimensions = new HashMap<String, Integer>();
                    newDimensions.put("width", newWidth);
                    newDimensions.put("height", newHeight);
                    return newDimensions;
                }

            }
        }

        return null;
    }


}

