package ai.geolambda;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MatchGHStore {

    private static final MatchGHStore _instance = new MatchGHStore();
    private MatchGHStore(){}
    public static MatchGHStore getInstance(){return _instance;}


    RocksDB db;
    String dbPath;
    //ReentrantLock writeLock = new ReentrantLock();
     int instanceId;
     boolean isReadOnly;
     ReentrantLock lock = new ReentrantLock(); //allow mutliple writes onto a val

    /** Init DB */


    public synchronized void initDb(final String dbLoc){

        instanceId=0;//for now only one instance

        this.dbPath =dbLoc;
        File instanceDataDir = new File(dbPath+ "\\"+instanceId);


        if (!instanceDataDir.exists()){
            instanceDataDir.mkdir();
        }
        String dbPath = instanceDataDir.getAbsolutePath();
        Options options = new Options()
                .setCreateIfMissing(true)
                .setAllowMmapReads(true)
                .setAllowMmapWrites(true)
                .setCompressionType(CompressionType.ZSTD_COMPRESSION)
                .setEnablePipelinedWrite(true);

        try {
            if (!isReadOnly) {
                db = RocksDB.open(options,
                        dbPath);
            }else{
                db = RocksDB.openReadOnly(options,
                        dbPath);
            }
        } catch (Exception e) {
            //logger.error("Error in init of DB instance {} {}",instanceId, dbPath);
            e.printStackTrace();
        }
    }

    public void close(){


        try {
            if (!isReadOnly){
                db.compactRange();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //add all geohashes
    public void addRecord(final String prefix,final String gh,final String placeId){

        //keys could be 5LVL for gh5 , 6LVL: and so on
        try {
            String key = prefix+":"+gh;
            byte [] existingVal = db.get(key.getBytes(StandardCharsets.UTF_8));
            if (existingVal!=null){
                //merge it and add it to the placeid dump
                lock.lock();
                try{

                    String placeIdsOld = new String(existingVal);
                    String placeNew = placeIdsOld+"|"+placeId;
                    db.put(key.getBytes(StandardCharsets.UTF_8),placeNew.getBytes(StandardCharsets.UTF_8));

                }catch (Exception e){
                    e.printStackTrace();
                }
                lock.unlock();
            }else{
                //just add the value

                db.put(key.getBytes(StandardCharsets.UTF_8),placeId.getBytes(StandardCharsets.UTF_8));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Set<String> matchGeoHash(final String gh){

        //match at various levels
        //can be dervied from length
        String key = (gh.length()+1) +"LVL:"+gh;
        try {
            byte[] val = db.get(key.getBytes(StandardCharsets.UTF_8));
            if (val != null) {
                String  placesstr=new String(val);
                String [] ids = placesstr.split("\\|");
                if (ids.length>0){
                    Set<String> placeIds = new HashSet<>();
                    for (String id : ids) {
                        placeIds.add(id);
                    }
                    return placeIds;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return Collections.emptySet();

    }


}
