package ai.geolambda;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton uses QuadTree, SRTTree for matching
 * Build the tree inmemory and push through data
 */
public class TreeBasedMatcher {

    private static final TreeBasedMatcher _instance = new TreeBasedMatcher();
    private TreeBasedMatcher(){}

    public static TreeBasedMatcher getInstance(){return _instance;}

    //multiple implmentations exists
    //Hibert tree (hpr), Str & QUAD TREE
    private SpatialIndex spatialIndex = new Quadtree();
    //store the inner geometry for acuurate matching
    private Map<String, Envelope> innerGeom = new HashMap<>();
    public synchronized void init(final String polygonFile){


        try {
            BufferedReader reader = new BufferedReader(new FileReader(polygonFile));
            String line = reader.readLine();

            while(line!=null){

                Feature feature  = (Feature) GeoJSONFactory.create(line.trim());
                indexGeom(line, (String) feature.getId());
                line = reader.readLine();

            }
        }catch (Exception e){

        }
    }
    public synchronized boolean indexGeom(final String line, final String placeId){
        try {

            org.locationtech.jts.geom.Geometry g = new GeoJSONReader().read(line);
            spatialIndex.insert(g.getEnvelopeInternal(),placeId);
            //store it for accurate matching
            innerGeom.put(placeId,g.getEnvelopeInternal());
            return true;
        }catch (Exception e){

            e.printStackTrace();
        }
        return false;
    }

    //match aganist a geohash
    public List<String> match(final String geohash){
        //need to make a box and then match

        GeoHash gh = GeoHash.fromGeohashString(geohash);

        BoundingBox hashBB = gh.getBoundingBox();
        Coordinate[] hashBBArray = new Coordinate[] {new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude()),
                new Coordinate(hashBB.getNorthWestCorner().getLongitude(),hashBB.getNorthWestCorner().getLatitude()),
                new Coordinate(hashBB.getSouthWestCorner().getLongitude(),hashBB.getSouthWestCorner().getLatitude())
                , new Coordinate(hashBB.getSouthEastCorner().getLongitude(),hashBB.getSouthEastCorner().getLatitude()),
                new Coordinate(hashBB.getNorthEastCorner().getLongitude(),hashBB.getNorthEastCorner().getLatitude())};
        LinearRing ring = new GeometryFactory().createLinearRing( hashBBArray );
        LinearRing holes[] = null; // use LinearRing[] to represent holes
        Polygon hashPolygon = new GeometryFactory().createPolygon(ring, holes );
        List<String> matchedPlaces = new ArrayList<>();
        spatialIndex.query(hashPolygon.getEnvelopeInternal(), new ItemVisitor() {
            @Override
            public void visitItem(Object item) {
                //asume id is string
                String placeId = (String)item;
                if (innerGeom.containsKey(placeId)){
                    if (innerGeom.get(placeId).contains(hashPolygon.getEnvelopeInternal())){
                        //can be contains or intersects
                        matchedPlaces.add(placeId);
                    }
                }
            }
        });
        return matchedPlaces;
    }

}
