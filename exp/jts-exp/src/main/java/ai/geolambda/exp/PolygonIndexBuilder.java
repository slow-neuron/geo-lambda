package ai.geolambda.exp;

import ch.hsr.geohash.GeoHash;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.operation.overlay.PolygonBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class PolygonIndexBuilder {

    public static void main(String[] args) {

        System.out.println("build polygon index");

        //read polygon and build a simple RTree


        try {
            String polygonFile = "/home/boson/geo-processing/geo-lambda/exp/jts-exp/src/test/resources/nyc-polygon.geojson";
            BufferedReader reader = new BufferedReader(new FileReader(polygonFile));
            Geometry g = new GeoJsonReader().read(reader);
            int geomCount = g.getNumGeometries();
            for(int i =0;i < geomCount;i++){
                Geometry innerGeom = g.getGeometryN(i);
                System.out.println("parsing inner geo");
                Coordinate [] coordinates = innerGeom.getCoordinates();
                for(int cIdx=0;cIdx < coordinates.length;cIdx++){
                    System.out.println(coordinates[cIdx].x +" "+coordinates[cIdx].y);
                    GeoHash baseGh = GeoHash.withCharacterPrecision(coordinates[cIdx].y,coordinates[cIdx].x,5);
                    List<GeoHash> ghCOll = GeoHashGenerator.calculateGeohashes(g,9,4,baseGh);
                    if (ghCOll!=null){
                        ghCOll.forEach(x->{
                            System.out.println(x);
                        });
                    }

                }


            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
