package com.iyr.ian.utils.geo;

import android.content.Context;
import android.util.TypedValue;

public class Convertions {


    public double convert(String originalUnit, String newUnit, double input) {
        double num1 = input;
        double num2 = 0.0d;

        String original = originalUnit.toLowerCase();
        String newU = newUnit.toLowerCase();

        switch(original)
        {
            case "inches":
            {
                switch(newU)
                {
                    case "inches":
                        num2 = num1;
                        break;
                    case "feet":

                        num2 = num1 / 12.0d;
                        break;
                    case "yards":

                        num2 = num1 / 36.0d;
                        break;
                    case "miles":

                        num2 = num1 / 63360.0d;
                        break;
                    case "millimeters":

                        num2 = num1 * 25.4d;
                        break;
                    case "centimeters":

                        num2 = num1 * 2.54d;
                        break;
                    case "meters":

                        num2 = num1 * 0.0254d;
                        break;
                    case "kilometers":

                        num2 = num1 * 0.0000254d;
                        break;
                }
                break;
            }
            case "feet":
            {
                switch(newU)
                {
                    case "inches":

                        num2 = num1*12.0d;
                        break;
                    case "feet":
                        num2 = num1;
                        break;
                    case "yards":

                        num2 = num1/3.0d;
                        break;
                    case "miles":

                        num2 = num1/5280.0d;
                        break;
                    case "millimeters":

                        num2 = num1*304.8d;
                        break;
                    case "centimeters":

                        num2 = num1*30.48d;
                        break;
                    case "meters":

                        num2 = num1*0.3048d;
                        break;
                    case "kilometers":

                        num2 = num1*0.0003048d;
                        break;
                }
                break;
            }
            case "yards":
            {
                switch(newU) {
                    case "inches":

                        num2 = num1 * 36.0d;
                        break;
                    case "feet":

                        num2 = num1 * 3.0d;
                        break;
                    case "yards":
                        num2 = num1;
                        break;
                    case "miles":

                        num2 = num1 / 1760.0d;
                        break;
                    case "millimeters":

                        num2 = num1 * 914.4d;
                        break;
                    case "centimeters":

                        num2 = num1*91.44d;
                        break;
                    case "meters":
                        num2 = num1*0.9144d;
                        break;
                    case "kilometers":

                        num2 = num1/1093.61d;
                        break;
                }
                break;
            }
            case "miles":
            {
                switch(newU)
                {
                    case "inches":

                        num2 = num1*6330.0d;
                        break;
                    case "feet":

                        num2 = num1*5280.0d;
                        break;
                    case "yards":

                        num2 = num1*1760.0d;
                        break;
                    case "miles":
                        num2 = num1;
                        break;
                    case "millimeters":

                        num2 = num1*1609340.0d;
                        break;
                    case "centimeters":

                        num2 = num1*160934.0d;
                        break;
                    case "meters":

                        num2 = num1*1609.34d;
                        break;
                    case "kilometers":

                        num2 = num1*1.60934d;
                        break;
                }
                break;
            }
            case "millimeters":
            {
                switch(newU)
                {
                    case "inches":
                        num2 = num1*25.4d;
                        break;
                    case "feet":
                        num2 = num1/304.8d;
                        break;
                    case "yards":
                        num2 = num1/914.4d;
                        break;
                    case "miles":
                        num2 = num1/1609000.0d;
                        break;
                    case "millimeters":
                        num2 = num1;
                        break;
                    case "centimeters":
                        num2 = num1/10;
                        break;
                    case "meters":
                        num2 = num1/100;
                        break;
                    case "kilometers":
                        num2 = num1/1000;
                        break;
                }
                break;
            }

            case "centimeters":
            {
                switch(newU)
                {
                    case "inches":
                        num2 = num1 / 2.54d;
                        break;
                    case "feet":
                        num2 = num1 / 30.48d;
                        break;
                    case "yards":
                        num2 = num1 / 91.44d;
                        break;
                    case "miles":
                        num2 = num1/160934.0d;
                        break;
                    case "millimeters":
                        num2 = num1*10;
                        break;
                    case "centimeters":
                        num2 = num1;
                        break;
                    case "meters":
                        num2 = num1*100;
                        break;
                    case "kilometers":
                        num2 = num1*1000;
                        break;
                }
                break;
            }
            case "meters":
            {
                switch(newU) {
                    case "inches":
                        num2 = num1 * 39.3701d;
                        break;
                    case "feet":
                        num2 = num1 * 3.28084d;
                        break;
                    case "yards":
                        num2 = num1*1.09361d;
                        break;
                    case "miles":
                        num2 = num1/1609.34d;
                        break;
                    case "millimeters":
                        num2 = num1*1000;
                        break;
                    case "centimeters":
                        num2 = num1*100;
                        break;
                    case "meters":
                        num2 = num1;
                        break;
                    case "kilometers":
                        num2 = num1/1000;
                        break;
                }
                break;
            }
            case "kilometers":
            {
                switch(newU)
                {
                    case "inches":
                        num2 = num1*39370.1d;
                        break;
                    case "feet":
                        num2 = num1*3280.84d;
                        break;
                    case "yards":
                        num2 = num1*1093.61d;
                        break;
                    case "miles":
                        num2 = num1/1.60934d;
                        break;
                    case "millimeters":
                        num2 = num1*10000;
                        break;
                    case "centimeters":
                        num2 = num1*1000;
                        break;
                    case "meters":
                        num2 = num1*100;
                        break;
                    case "kilometers":
                        num2 = num1;
                        break;
                }
                break;
            }
        }

        return num2;
    }


    public static int pxToDp(int px, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px,
                context.getResources().getDisplayMetrics());
    }

    public static int dpToPx(float dp, Context context) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
