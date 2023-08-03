package ai.geolambda;

import org.wololo.geojson.*;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Generate optimal geohash and store it in RocksDB as string
 * can also store as bitmaps
 */
public class PolygonGHLoader {

    final String polygonCollectionGeoJson;

    public PolygonGHLoader(String polygonGeoJson) {
        this.polygonCollectionGeoJson = polygonGeoJson;
    }

    public void initDb(final String dbPath){

        //init singleton
        MatchGHStore.getInstance().initDb(dbPath);


    }

    public void processPolygon(){
        //generate geohash and store it
        //file consists of one feature per polygon for now
        //process and load into geohash store

        try {
            BufferedReader reader = new BufferedReader(new FileReader(polygonCollectionGeoJson));
            String line = reader.readLine();
            while(line!=null){
                Feature feature  = (Feature) GeoJSONFactory.create(line.trim());
                if (feature!=null){
                    //assume has a place id
                    String id = (String) feature.getId();
                    //TODO: can use any property
                    org.locationtech.jts.geom.Geometry g  = new GeoJSONReader().read(line);
                    OptimGHGenerator ghGenerator = new OptimGHGenerator();
                    //this generates and stores , while another module can read & lookup(match)
                    //can generate at any level 5,6,7,8,9
                    ghGenerator.generateGH(g,9,id);
                }


                line = reader.readLine();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void buildSpatialIndex(){

        try {
            BufferedReader reader = new BufferedReader(new FileReader(polygonCollectionGeoJson));
            String line = reader.readLine();
            while(line!=null){
                Feature feature  = (Feature) GeoJSONFactory.create(line.trim());
                if (feature!=null){
                    //assume has a place id
                    String id = (String) feature.getId();
                    //add to index for matching
                    TreeBasedMatcher.getInstance().indexGeom(line,id);
                }


                line = reader.readLine();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }




    public static void main(String[] args) {


        PolygonGHLoader ghLoader = new PolygonGHLoader(args[0]);
        ghLoader.initDb(args[1]);
        ghLoader.processPolygon();
        //choose between any one of them
        //differential hashing or spatial indexing
        ghLoader.buildSpatialIndex();
        MatchGHStore.getInstance().close();


    }
}
