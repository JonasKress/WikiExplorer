package net.nosuefor.wikiexplorer.helper;

public class Format {

    public static String distance(double distance) {
        String distanceLabel;

        if (distance < 1000) {
            distanceLabel = ((int) distance) + "m";
        } else {
            distanceLabel = (Math.round((distance / 100)) / 10.0) + "km";
        }

        return distanceLabel;
    }
}
