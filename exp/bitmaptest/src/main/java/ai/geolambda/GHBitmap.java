package ai.geolambda;

import ch.hsr.geohash.GeoHash;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

public class GHBitmap {


    /*
        Store GH as dots in a large bitmaps
        Instead of seeing world (polygons) with boxes
        see them as dots filled in a large bitmap
        that allows for simple lookup rather than expensive CPU ops
        these dots are at various levels GH5,GH6,GH7,GH8 & GH9
        Instead of hashmap store it in bitmap
     */
    public static void main(String[] args) {

        String ghStr="dr5rue00";
        String ghStr2="dr5rue01";
        String ghStr3="dr5rue02";


        GeoHash gh = GeoHash.fromGeohashString(ghStr);
        GeoHash gh2 = GeoHash.fromGeohashString(ghStr2);
        GeoHash gh3= GeoHash.fromGeohashString(ghStr3);


        System.out.println(gh.ord());
        System.out.println(gh2.ord());
        System.out.println(gh3.ord());

        Roaring64NavigableMap bitmap = new Roaring64NavigableMap();
        bitmap.add(gh.ord());
        bitmap.add(gh2.ord());
        bitmap.add(gh3.ord());

        System.out.println(bitmap.getIntCardinality());

        //this can be any can be hexid or quadkeys


        System.out.println("loaded all gh for matching");

    }
}
