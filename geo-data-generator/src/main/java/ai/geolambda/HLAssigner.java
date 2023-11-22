package ai.geolambda;

import ch.hsr.geohash.GeoHash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class HLAssigner {
    //max GH
    //Bronx = 1,379,946  | 12,634 |
    //Brooklyn = 2,590,516 | 14,417
    //Manhattan = 1,596,273 | 27,203
    //Queens = 2,278,029 | 8,090
    //Staten-Island = 491,133 | 3,297
    public static void main(String[] args) {

        String divName = "Staten-Island";
        String idFile = String.format("/home/boson/datagen/nyc-iddump/id-%s.csv", divName);
        String ghFile = String.format("/home/boson/datagen/nyc-gh7/gh-%s.csv",divName);

        int density = 14448;
        int maxGH = 126; //this is determined by computing GH6 and distrbuting ids

        try{
            //load into gh6 first and then re-distribute to gh7
            Map<String, List<String>> gh7Dump = new HashMap<>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(ghFile));
                String line = reader.readLine();
                while (line != null) {
                    String[] tkns = line.trim().split(",");
                    String gh6 = tkns[0].substring(0, 6);
                    gh7Dump.computeIfAbsent(gh6, c -> new ArrayList<>()).add(tkns[0]);
                    line = reader.readLine();
                }

                reader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            Map<String,String> idDump = new HashMap<>();
            try {
                BufferedReader r2 = new BufferedReader(new FileReader(idFile));
                String line = r2.readLine();
                while(line!=null){
                    String [] tkns = line.trim().split(",");
                    String id = tkns[0];
                    idDump.put(id,"0");
                    line = r2.readLine();
                }
            }catch (Exception e){

            }
            System.out.println(gh7Dump.size() + " for population of " + idDump.size() );
            //can use land parcel
            List<String> usableGh6 = new ArrayList<>();

            List<String> usableGh6Set2 = new ArrayList<>();
            Random r = new Random();

                    gh7Dump.keySet().forEach(
                            x->{if (r.nextInt(10)%2==0)usableGh6.add(x);});

            gh7Dump.keySet().forEach(
                    x->{if (r.nextInt(120)%3==0)usableGh6Set2.add(x);});

            System.out.println("GH6 to use " + usableGh6.size() + " set2 is " + usableGh6Set2.size());
            Map<String,String> gh6Assigned = new HashMap<>();
            for(String id : idDump.keySet()){
                String gh6="";
                if (r.nextBoolean()) {
                    int idx = r.nextInt(usableGh6.size());
                    gh6 = usableGh6.get(idx);
                }else{
                    int idx = r.nextInt(usableGh6Set2.size());
                    gh6=usableGh6Set2.get(idx); //dense ones
                }
                gh6Assigned.put(id,gh6);
            }

            BufferedWriter writer=  new BufferedWriter(new FileWriter(String.format("/tmp/id-hl-%s.csv", divName)));
            writer.write("id,hl,lat,lon");
            writer.newLine();
            for(String id : idDump.keySet()){
                String gh6 = gh6Assigned.get(id);
                List<String> gh7All = gh7Dump.get(gh6);
                int rIdx = r.nextInt(gh7All.size());


                    String gh7 = gh7All.get(rIdx);

                    try{
                        GeoHash gh = GeoHash.fromGeohashString(gh7);
                        writer.write(id+","+gh7+","+gh.getBoundingBox().getCenter().getLatitude()+","+gh.getBoundingBox().getCenter().getLongitude());
                        writer.newLine();
                    }catch (Exception e){
                        e.printStackTrace();

                    }

            }

            writer.flush();
            writer.newLine();

        }catch (Exception e){

        }

    }
}
