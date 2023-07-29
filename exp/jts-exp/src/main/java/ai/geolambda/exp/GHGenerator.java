package ai.geolambda.exp;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;


/** GIven a polygon generates containing geohashes
 * geohashes at various levels are generated
 * level determined by area & longest common prefix geohash
 *
 */
public class GHGenerator {


    /* Algo

        Generate geo hashes around the polygon at some level based on area
        Find the Longest Common Prefix, treat it as a base gh
        Determine the max precision to be used based on area
        larger area polygon max level 7 and smaller polygons(stores) max is 9
        Find all children base gh that intersect with the polygon
        For those GH which are fully contained , don't generate any more children
        do it recursively until reach the max level
        Find all contained GH at max level
        Input: Geometry
        Output: Set of geohashes that are contained within the area at various levels
        these GH can be stored in a file or Persistent DB for lookup for matching aganist geometry
        More over this can be stored in bitmap(Roaringbitmaps) for effcient lookups
        

     */
    private static GeometryFactory geometryFactory = new GeometryFactory();

    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };


    Set<ch.hsr.geohash.GeoHash> generateChildHashes(final String ghStr){
        TreeSet<ch.hsr.geohash.GeoHash> children = new TreeSet<>();
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


    private Set<String> ghgenerated = new HashSet<>();
    Set<String> outerEnvelopeCoarseGH = new HashSet<>();
    public void findAllGhInPolygon(final Geometry g, int precision){

        for(int geomIdx=0; geomIdx<g.getNumGeometries(); geomIdx++){

            Geometry innerGeom = g.getGeometryN(geomIdx);

            Coordinate[] cords= innerGeom.getCoordinates();
            double area = innerGeom.getArea();
            System.out.println("area " + area);

            //switch based on area


            for(int cidx =0;cidx < cords.length;cidx++){
              //assume input is lon,lat
              System.out.println(cords[cidx].x + " " + cords[cidx].y);
                outerEnvelopeCoarseGH.add (GeoHash.geoHashStringWithCharacterPrecision(cords[cidx].y,cords[cidx].x,precision));

            }

            System.out.println("coarse GH");
            outerEnvelopeCoarseGH.forEach(x->{
                System.out.println(x);

            });
            String longestMatchingGH =
                    StringUtils.getCommonPrefix(outerEnvelopeCoarseGH.toArray(new String[outerEnvelopeCoarseGH.size()]));

            int maxLenGH =9;
            if (longestMatchingGH.length()< 4) {
                System.out.println("longest gh " + longestMatchingGH);
                maxLenGH=6;
            }else if (longestMatchingGH.length()>=5 && longestMatchingGH.length()<=6){
                //gh8 should be good enough
                maxLenGH=8;
            }

            //generate all child geohashes for this upper and limit to maxLen

            int startDepth =  longestMatchingGH.length();

            System.out.println("use max length " + startDepth +" use " + maxLenGH);


            //at each level collect all gh
            Map<Integer,Set<GeoHash>> ghCollContains = new HashMap<>();
            Map<Integer,Set<GeoHash>> ghIntersects = new HashMap<>();

            //generte all for current level
            Set<ch.hsr.geohash.GeoHash> allSeeds =
                    generateChildHashes(longestMatchingGH);

            int seedGHLevel = longestMatchingGH.length();
            Set<ch.hsr.geohash.GeoHash> seedGh = new TreeSet<>();


            allSeeds.forEach(childGh->{
                //System.out.println(childGh.toBase32());
                //compute aganist geometry
                BoundingBox hashBB = childGh.getBoundingBox();
                Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude()),
                        new Coordinate(hashBB.getNorthWestCorner().getLongitude(),hashBB.getNorthWestCorner().getLatitude()),
                        new Coordinate(hashBB.getSouthWestCorner().getLongitude(),hashBB.getSouthWestCorner().getLatitude())
                        , new Coordinate(hashBB.getSouthEastCorner().getLongitude(),hashBB.getSouthEastCorner().getLatitude()),
                        new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude())};
                LinearRing ring = new GeometryFactory().createLinearRing( hashBBArray );
                LinearRing holes[] = null; // use LinearRing[] to represent holes
                Polygon hashPolygon = new GeometryFactory().createPolygon(ring, holes );


               // System.out.println(minLat + " " + maxLat + " " + maxLon + " " + minLon);
                boolean intersect = hashPolygon.intersects(innerGeom);
                if (intersect){
                    //System.out.println(childGh.toBase32() + " intersects with inner geom");
                    System.out.println(hashPolygon.toString());
                    seedGh.add(childGh);


                }else{
                    //System.out.println(childGh.toBase32() + " doesnt intersect");
                }

            });

            ghIntersects.put(startDepth,seedGh);

            //generate geohash at this level and move on



            for(int depth=seedGHLevel;depth<maxLenGH;depth++) {
                Set<ch.hsr.geohash.GeoHash> seedDump = ghIntersects.get(depth);
                int newDepth = depth+1;
                System.out.println("generating at level " + depth +" there are " + seedDump.size() + " hashes ");
                for (ch.hsr.geohash.GeoHash baseGH : seedDump) {
                    //System.out.println("generate children for " + baseGH.toBase32());
                    Set<ch.hsr.geohash.GeoHash> childGHColl =
                            childGHIntersects(baseGH.toBase32(), innerGeom);
                    //System.out.println("generated gh child " + childGHColl.size());
                    if (childGHColl.size() ==32){

                        //System.out.println("check if " + childGHColl.size() + " for " + baseGH.toBase32() + " is all contained");
                        boolean allContained = true;
                     //check if every hash is contained
                        for(GeoHash innerGH : childGHColl){
                            if (!isGHContained(innerGH,innerGeom)){
                                allContained=false;
                                break;
                            }
                        }
                        if (allContained){
                            //System.out.println("all contained " + baseGH.toBase32());
                            ghCollContains.computeIfAbsent(newDepth,coll->new TreeSet<>()).add(baseGH);
                        }else{
                            ghIntersects.computeIfAbsent(newDepth,coll-> new TreeSet<>()).addAll(childGHColl);
                        }
                    }else{
                        ghIntersects.computeIfAbsent(newDepth,coll-> new TreeSet<>()).addAll(childGHColl);
                    }

                }
            }
            System.out.println("total GH generated " + ghIntersects.get(maxLenGH).size() +" setting " + maxLenGH);

            //check which all intersects which all contains

            System.out.println("hash contains fully");
            ghCollContains.forEach((level,ghcoll)->{
                System.out.println("fully contained " + level + " " + ghcoll.size());
                if (level==3){
                    ghcoll.forEach(gh->{
                        System.out.println("fully in " + gh.toBase32());
                    });
                }
            });


            ghIntersects.forEach((level,ghcoll)->{
                System.out.println("fully intersects " + level + " " + ghcoll.size());
            });

            int maxLevel = 0;
            for(int levelId : ghIntersects.keySet()){
                maxLevel=Math.max(maxLevel,levelId);
            }

            System.out.println("max level is " + maxLevel);
            for(GeoHash ghMax:ghIntersects.get(maxLevel)){
                if (isGHContained(ghMax,innerGeom)){
                    //System.out.println("contained " + ghMax.toBase32());
                }else{
                    //System.out.println("not contained " + ghMax.toBase32());
                }
            }

        }//fi geo



    }

    public static void main(String[] args) {




        String polygonFile = "/home/boson/geo-processing/geo-lambda/exp/jts-exp/src/test/resources/nyc-polygon.geojson";
       // String polygonFile = "/home/boson/geo-processing/geo-lambda/exp/jts-exp/src/test/resources/nyc-large-area.geojson";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(polygonFile));
            Geometry g = new GeoJsonReader().read(reader);
            GHGenerator ghgen = new GHGenerator();
            ghgen.findAllGhInPolygon(g,8);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
