package ai.geolambda.exp;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import org.locationtech.jts.geom.*;
import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.util.BoundingBoxGeoHashIterator;
import ch.hsr.geohash.util.TwoGeoHashBoundingBox;
import java.util.ArrayList;
import java.util.List;

public class GeoHashGenerator {

    //https://github.com/leandrofreire08/geohasher/blob/master/src/main/java/geohasher/GeoHasher.java

    //draw a bigger box for polygon
    //generate all geohashes at a given precision for the box
    //check if gh intersects with the original polygon
    //geohash in itself is a box


    private static GeometryFactory geometryFactory = new GeometryFactory();

    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };


    public void generateGeoHash(final Geometry g){


    }

    private static List<GeoHash> getChildHashes(GeoHash geo){
        List<GeoHash> children = new ArrayList<GeoHash>();
        for (int i = 0; i < base32.length; i++){
            try{
                if (geo == null){
                    children.add(GeoHash.fromGeohashString(String.valueOf(base32[i])));
                } else {
                    children.add(GeoHash.fromGeohashString(geo.toBase32() + base32[i]));
                }
            } catch (NullPointerException ex){
                // Hash string was invalid
            }
        }
        return children;
    }

    public static List<GeoHash> calculateGeohashes(Geometry polygon, int numCharsInHash, int depth, GeoHash baseHash){

        List<GeoHash> ret = new ArrayList<GeoHash>();

        for (GeoHash hash : getChildHashes(baseHash)){

            if (doesGeometryContainHash(hash, polygon)){
                ret.add(hash);
            }
            else if (depth < numCharsInHash && doesGeometryIntersectHash(hash, polygon)){
                List<GeoHash> hashes = calculateGeohashes(polygon, numCharsInHash, depth + 1, hash);
                ret.addAll(hashes);
            } else if (depth >= numCharsInHash && doesGeometryIntersectHash(hash, polygon)){
                ret.add(hash);
            }

        }

        return ret;
    }

    private static BoundingBox getMinimumBoundingBox(Geometry polygon){
        Envelope bb = polygon.getEnvelopeInternal();
        return new BoundingBox(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
    }
    private static boolean doesGeometryIntersectHash(GeoHash hash, Geometry polygon){
        BoundingBox hashBB = hash.getBoundingBox();

        double maxLat = Math.max(hashBB.getNorthLatitude(),hashBB.getSouthLatitude());
        double minLat = Math.min(hashBB.getNorthLatitude(),hashBB.getSouthLatitude());

        double maxLon = Math.max(hashBB.getEastLongitude(),hashBB.getWestLongitude());
        double minLon = Math.min(hashBB.getEastLongitude(),hashBB.getWestLongitude());


        Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(maxLat,minLon),
                new Coordinate(maxLat,maxLon), new Coordinate(minLat,minLon)
                , new Coordinate(minLat,maxLon), new Coordinate(maxLat,minLon)};
        LinearRing ring = geometryFactory.createLinearRing( hashBBArray );
        LinearRing holes[] = null; // use LinearRing[] to represent holes
        Polygon hashPolygon = geometryFactory.createPolygon(ring, holes );
        return polygon.intersects(hashPolygon);
    }

    private static boolean doesGeometryContainHash(GeoHash hash, Geometry polygon){
        BoundingBox hashBB = hash.getBoundingBox();

        double maxLat = Math.max(hashBB.getNorthLatitude(),hashBB.getSouthLatitude());
        double minLat = Math.min(hashBB.getNorthLatitude(),hashBB.getSouthLatitude());

        double maxLon = Math.max(hashBB.getEastLongitude(),hashBB.getWestLongitude());
        double minLon = Math.min(hashBB.getEastLongitude(),hashBB.getWestLongitude());


        Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(maxLat,minLon),
                new Coordinate(maxLat,maxLon), new Coordinate(minLat,minLon)
                , new Coordinate(minLat,maxLon), new Coordinate(maxLat,minLon)};
        LinearRing ring = geometryFactory.createLinearRing( hashBBArray );
        LinearRing holes[] = null; // use LinearRing[] to represent holes
        Polygon hashPolygon = geometryFactory.createPolygon(ring, holes );
        return polygon.contains(hashPolygon);
    }


}
