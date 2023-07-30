package ai.geolambda;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.TreeSet;

public class BitMapTest {


    static void generateIds(final String outFile){
        Random r = new Random();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            for (int i = 0; i < 5_000_000; i++) {
                long randomVal = r.nextLong() >>> 1;
                writer.write(""+randomVal);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    static void loadUpSet(final String idDump){

        //can be hash set too
        TreeSet<Long> idSortedSet = new TreeSet<>();

        try {
            var count =0;
            BufferedReader reader = new BufferedReader(new FileReader(idDump));
            String line = reader.readLine();

            while(line!=null){
                idSortedSet.add(Long.parseLong(line.trim()));
                line = reader.readLine();
                //System.out.println("loading " + line.trim());
                count++;
                if(count % 100000==0){
                    System.out.println("loaded "+ count);
                }
            }
            reader.close();

        }catch (Exception e){

        }
        System.out.println("idsorted val "+ idSortedSet.size());
    }


    static void loadUpLongSet(final String idDump){

        LongSet longs = new LongArraySet();
        try {
            var count =0;
            BufferedReader reader = new BufferedReader(new FileReader(idDump));
            String line = reader.readLine();

            while(line!=null){
                longs.add(Long.parseLong(line.trim()));
                line = reader.readLine();
                //System.out.println("loading " + line.trim());
                count++;
                if(count % 100000==0){
                    System.out.println("loaded "+ count);
                }
            }
            reader.close();

        }catch (Exception e){

        }
        System.out.println("lon val "+ longs.size());
    }

    static void loadupBitmap(final String idDump){

        Roaring64NavigableMap bitmap = new Roaring64NavigableMap();
        try {
            var count =0;
            BufferedReader reader = new BufferedReader(new FileReader(idDump));
            String line = reader.readLine();

            while(line!=null){
                bitmap.add(Long.parseLong(line.trim()));
                line = reader.readLine();

                //System.out.println("loading " + line.trim());
                count++;
                if(count % 100000==0){
                    System.out.println("loaded "+ count);
                }
            }
            reader.close();

        }catch (Exception e){

        }
        System.out.println("bitmap loaded  "+ bitmap.getLongCardinality());
    }


    public static void main(String[] args) {


        //generate a random 1M Long and hash it store it
        //load it to Roaringbitmap


        //generateIds("/tmp/id-dump.txt");

        String idDump = "/tmp/id-dump.txt";
        loadUpSet(idDump);
        //loadUpLongSet(idDump);
        //BIT Map can load while array set cannot load within 1GB
        //loadupBitmap(idDump);





    }
}
