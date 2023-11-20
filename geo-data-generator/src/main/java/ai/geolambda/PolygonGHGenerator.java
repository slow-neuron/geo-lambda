package ai.geolambda;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class PolygonGHGenerator {

    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };


    Set<GeoHash> generateChildHashes(final String ghStr){
        TreeSet<GeoHash> children = new TreeSet<>();
        ch.hsr.geohash.GeoHash baseGH = ch.hsr.geohash.GeoHash.fromGeohashString(ghStr);
        for (int i = 0; i < base32.length; i++){
            try{
                if (baseGH == null){
                    children.add(ch.hsr.geohash.GeoHash.fromGeohashString(String.valueOf(base32[i])));
                } else {
                    children.add(ch.hsr.geohash.GeoHash.fromGeohashString(baseGH.toBase32() + base32[i]));
                }
            } catch (Exception ex){

            }
        }
        System.out.println("child is " + children.size());
        return children;
    }

    void generateGHPolygon(){
        var childGh = generateChildHashes("tdr1wd6");
    }

    boolean isGHContained(final GeoHash gh , final Geometry g){

        BoundingBox hashBB = gh.getBoundingBox();
        Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude()),
                new Coordinate(hashBB.getNorthWestCorner().getLongitude(),hashBB.getNorthWestCorner().getLatitude()),
                new Coordinate(hashBB.getSouthWestCorner().getLongitude(),hashBB.getSouthWestCorner().getLatitude())
                , new Coordinate(hashBB.getSouthEastCorner().getLongitude(),hashBB.getSouthEastCorner().getLatitude()),
                new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude())};
        LinearRing ring = new GeometryFactory().createLinearRing( hashBBArray );
        LinearRing holes[] = null; // use LinearRing[] to represent holes
        Polygon hashPolygon = new GeometryFactory().createPolygon(ring, holes );
        return g.contains(hashPolygon);

    }


    Set<ch.hsr.geohash.GeoHash> childGHIntersects(final String ghStr,final Geometry testGeom){
        TreeSet<ch.hsr.geohash.GeoHash> children = new TreeSet<>();
        ch.hsr.geohash.GeoHash baseGH = ch.hsr.geohash.GeoHash.fromGeohashString(ghStr);
        for (int i = 0; i < base32.length; i++){
            try{
                if (baseGH == null){
                    ch.hsr.geohash.GeoHash childGh = ch.hsr.geohash.GeoHash.fromGeohashString(String.valueOf(base32[i]));

                    BoundingBox hashBB = childGh.getBoundingBox();
                    Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude()),
                            new Coordinate(hashBB.getNorthWestCorner().getLongitude(),hashBB.getNorthWestCorner().getLatitude()),
                            new Coordinate(hashBB.getSouthWestCorner().getLongitude(),hashBB.getSouthWestCorner().getLatitude())
                            , new Coordinate(hashBB.getSouthEastCorner().getLongitude(),hashBB.getSouthEastCorner().getLatitude()),
                            new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude())};
                    LinearRing ring = new GeometryFactory().createLinearRing( hashBBArray );
                    LinearRing holes[] = null; // use LinearRing[] to represent holes
                    Polygon hashPolygon = new GeometryFactory().createPolygon(ring, holes );

                    boolean intersect = hashPolygon.intersects(testGeom);
                    if (intersect){
                        children.add(childGh);
                    }

                } else {
                    ch.hsr.geohash.GeoHash childGh = ch.hsr.geohash.GeoHash.fromGeohashString(baseGH.toBase32() + base32[i]);
                    BoundingBox hashBB = childGh.getBoundingBox();
                    Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude()),
                            new Coordinate(hashBB.getNorthWestCorner().getLongitude(),hashBB.getNorthWestCorner().getLatitude()),
                            new Coordinate(hashBB.getSouthWestCorner().getLongitude(),hashBB.getSouthWestCorner().getLatitude())
                            , new Coordinate(hashBB.getSouthEastCorner().getLongitude(),hashBB.getSouthEastCorner().getLatitude()),
                            new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude())};
                    LinearRing ring = new GeometryFactory().createLinearRing( hashBBArray );
                    LinearRing holes[] = null; // use LinearRing[] to represent holes
                    Polygon hashPolygon = new GeometryFactory().createPolygon(ring, holes );
                    boolean intersect = hashPolygon.intersects(testGeom);
                    if (intersect){
                        children.add(childGh);
                    }
                }
            } catch (Exception ex){

            }
        }
        return children;
    }

    public void findAllGhInPolygon(final String name,final String geomStr, int precision) {

        GeoJsonReader reader = new GeoJsonReader(new GeometryFactory());
        Geometry g = null;
        try {
            g = reader.read(geomStr);
            System.out.println(g.getCentroid().getX() + " " + g.getCentroid().getY() + " centroid is fixed " + name);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        BufferedWriter writer = null;
        try {
           writer = new BufferedWriter(new FileWriter(String.format("/tmp/feature-gh/gh-%s.csv", name)));

        } catch (Exception e) {

        }

        System.out.println("geometries are " + g.getNumGeometries());
        for (int geomIdx = 0; geomIdx < g.getNumGeometries(); geomIdx++) {

            Set<String> outerEnvelopeCoarseGH = new HashSet<>();

            Geometry innerGeom = g.getGeometryN(geomIdx);

            Coordinate[] cords = innerGeom.getCoordinates();
            double area = innerGeom.getArea();
            System.out.println("area " + area);

            //switch based on area


            for (int cidx = 0; cidx < cords.length; cidx++) {
                //assume input is lon,lat
                //System.out.println(cords[cidx].x + " " + cords[cidx].y);
                outerEnvelopeCoarseGH.add(GeoHash.geoHashStringWithCharacterPrecision(cords[cidx].y, cords[cidx].x, precision));

            }

            //System.out.println("coarse GH");
            // outerEnvelopeCoarseGH.forEach(x->{
            // System.out.println("GH outer Coarse " + x);

            // });
            String longestMatchingGH =
                    StringUtils.getCommonPrefix(outerEnvelopeCoarseGH.toArray(new String[outerEnvelopeCoarseGH.size()]));


            int maxLenGH = 7;
            //generate all child geohashes for this upper and limit to maxLen

            int startDepth = longestMatchingGH.length();

            System.out.println("use max length " + startDepth + " use " + maxLenGH);


            Map<Integer, Set<GeoHash>> ghIntersects = new HashMap<>();

            //generte all for current level
            Set<ch.hsr.geohash.GeoHash> allSeeds =
                    generateChildHashes(longestMatchingGH);

            int seedGHLevel = longestMatchingGH.length();
            Set<ch.hsr.geohash.GeoHash> seedGh = new TreeSet<>();


            allSeeds.forEach(childGh -> {
                //System.out.println(childGh.toBase32());
                //compute aganist geometry
                BoundingBox hashBB = childGh.getBoundingBox();
                Coordinate[] hashBBArray = new Coordinate[]{new Coordinate(hashBB.getNorthEastCorner().getLongitude(), hashBB.getNorthEastCorner().getLatitude()),
                        new Coordinate(hashBB.getNorthWestCorner().getLongitude(), hashBB.getNorthWestCorner().getLatitude()),
                        new Coordinate(hashBB.getSouthWestCorner().getLongitude(), hashBB.getSouthWestCorner().getLatitude())
                        , new Coordinate(hashBB.getSouthEastCorner().getLongitude(), hashBB.getSouthEastCorner().getLatitude()),
                        new Coordinate(hashBB.getNorthEastCorner().getLongitude(), hashBB.getNorthEastCorner().getLatitude())};
                LinearRing ring = new GeometryFactory().createLinearRing(hashBBArray);
                LinearRing holes[] = null; // use LinearRing[] to represent holes
                Polygon hashPolygon = new GeometryFactory().createPolygon(ring, holes);


                // System.out.println(minLat + " " + maxLat + " " + maxLon + " " + minLon);
                boolean intersect = hashPolygon.intersects(innerGeom);
                if (intersect) {
                    //System.out.println(childGh.toBase32() + " intersects with inner geom");
                    //System.out.println(hashPolygon.toString());
                    seedGh.add(childGh);


                } else {
                    //System.out.println(childGh.toBase32() + " doesnt intersect");
                }

            });

            ghIntersects.put(startDepth, seedGh);

            //generate geohash at this level and move on

            System.out.println("depth " + startDepth + " total GH " + seedGh.size());


            for (int depth = seedGHLevel; depth < maxLenGH; depth++) {
                Set<ch.hsr.geohash.GeoHash> seedDump = ghIntersects.get(depth);
                int newDepth = depth + 1;
                System.out.println("generating at level " + depth + " there are " + seedDump.size() + " hashes ");
                for (ch.hsr.geohash.GeoHash baseGH : seedDump) {
                    //System.out.println("generate children for " + baseGH.toBase32());
                    Set<ch.hsr.geohash.GeoHash> childGHColl =
                            childGHIntersects(baseGH.toBase32(), innerGeom);
                    //System.out.println("generated gh child " + childGHColl.size());

                    //System.out.println("check if " + childGHColl.size() + " for " + baseGH.toBase32() + " is all contained");

                    //check if every hash is contained
                    ghIntersects.computeIfAbsent(newDepth, coll -> new TreeSet<>()).addAll(childGHColl);

                }
                // System.out.println("total GH generated " + ghIntersects.get(maxLenGH).size() +" setting " + maxLenGH);


                if (ghIntersects.containsKey(precision)) {
                    System.out.println("there are " + ghIntersects.get(precision).size() + " gh collection");
                    for(GeoHash gh : ghIntersects.get(precision)){
                        if (gh.toBase32().length()>5){
                            try{
                                writer.write(gh.toBase32());
                                writer.newLine();
                                //writer.write(gh.getBoundingBox().getCenter().getLatitude()+","+gh.getBoundingBox().getCenter().getLongitude());
                                //writer.newLine();
                            }catch (Exception e){
                                e.printStackTrace();

                            }
                        }
                    }
                }


                //check which all intersects which all contains


            }//fi geo


        }


        try{
            writer.flush();
            writer.close();
        }catch (Exception e){

        }

    }

    public static void main(String[] args) {


        PolygonGHGenerator polygonGHGenerator  = new PolygonGHGenerator();
        polygonGHGenerator.generateGHPolygon();

        String polygonFile = "/home/boson/Downloads/nyc-boroughs.geojson";



        try {
            BufferedReader reader = new BufferedReader(new FileReader(polygonFile));

            String line = reader.readLine();

            FeatureCollection fcoll = (FeatureCollection)GeoJSONFactory.create(line.trim());

            for(Feature f : fcoll.getFeatures()){
                System.out.println(f.getProperties().keySet());
                org.wololo.geojson.Geometry g = f.getGeometry();
                PolygonGHGenerator ghGenerator = new PolygonGHGenerator();
                ghGenerator.findAllGhInPolygon((String)f.getProperties().get("boro_name"),g.toString(),7);
            }
            //Geometry g = new GeoJsonReader().read(reader);
            //GHGenerator ghgen = new GHGenerator();
            //ghgen.findAllGhInPolygon(g,7);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
