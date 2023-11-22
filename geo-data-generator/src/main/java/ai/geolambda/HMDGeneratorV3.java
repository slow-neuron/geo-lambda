package ai.geolambda;

import ch.hsr.geohash.GeoHash;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HMDGeneratorV3 {

    final String gh7DivBaseDir = "/home/boson/datagen/nyc-gh7";

    //load all GH5 and mark divnames for the same

    Random r = new Random(12010110L);
    //TOOD: make it as map lookup
    List<IdMobilityProfile> idStore = new ArrayList<>();
    BufferedWriter writer ;

    Long startEpoch=0L;
    Long prevEpoch = 0L;
    Map<String,List<String>> gh5Gh7Dump = new HashMap<>();
    Map<String,List<String>> gh6Gh7Dump = new HashMap<>();

    Set<String> gh7Dump = new HashSet<>();
    Set<String> gh6Dump = new HashSet<>();

    Set<String> gh5Dump = new HashSet<>();

    void loadDivGH(final String divName){

        String dumpFile = gh7DivBaseDir+ String.format("/gh-%s.csv", divName);

        try{
            BufferedReader reader = new BufferedReader(new FileReader(dumpFile));
            String line = reader.readLine();
            while(line!=null){
                String gh7 = line.trim();
                gh7Dump.add(gh7);
                gh5Gh7Dump.computeIfAbsent(gh7.substring(0,5),c->new ArrayList<>()).add(gh7);
                gh6Gh7Dump.computeIfAbsent(gh7.substring(0,6),c-> new ArrayList<>()).add(gh7);


                line = reader.readLine();
            }
            reader.close();
        }catch (Exception e){

        }

    }

    public void loadIdDump(final String idDumpFile){
        try{

            BufferedReader reader = new BufferedReader(new FileReader(idDumpFile));
            String line = reader.readLine();
            while(line!=null){

                String [] tkns = line.trim().split(",");
                IdMobilityProfile mfp = new IdMobilityProfile();
                mfp.hl = tkns[1];
                mfp.id=tkns[0];
                mfp.activityRank=Integer.valueOf(tkns[4]);
                idStore.add(mfp);

                IdActivity idActivity = new IdActivity();
                idActivity.epoch=startEpoch;
                idActivity.lastLoc=mfp.hl;
                idActivity.nextToLastLoc=mfp.hl;
                IdActivityStore.getInstance().idActivityMap.put(tkns[0],idActivity);
                line = reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();

        }
        System.out.println("id activity is loaded with " + IdActivityStore.getInstance().idActivityMap.size());
    }

    void generateData(final String id,final int activityRank,final DateTime ts, final int hourOfDay){

        IdActivity idActivity = IdActivityStore.getInstance().idActivityMap.get(id);
        if (idActivity!=null){

            //TODO: flow matrix and fetch from flow matrix
            //randomly pick GH6 and start the walk
            //can return to home also if required
            if (activityRank>=0 && activityRank<=3){
                if (hourOfDay >=9 && hourOfDay<=18){

                    if (r.nextBoolean()) {
                        GeoHash gh = GeoHash.fromGeohashString(idActivity.lastLoc);
                        GeoHash ghNew = gh.next(r.nextInt(10) + 1);
                        idActivity.nextToLastLoc = idActivity.lastLoc;
                        idActivity.lastLoc = ghNew.toBase32();
                        idActivity.epoch = ts.getMillis();
                        StringBuilder sb = new StringBuilder();
                        sb.append(id).append(",");
                        sb.append(ts.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:MM:SS"))).append(",");
                        sb.append(idActivity.lastLoc).append(",");
                        sb.append(ghNew.getBoundingBox().getCenter().getLatitude()).append(",");
                        sb.append(ghNew.getBoundingBox().getCenter().getLongitude());
                        try {
                            //writer.write(sb.toString());
                            //writer.newLine();

                        } catch (Exception e) {

                        }
                    }
                }

            }else if (activityRank>=4 && activityRank<=6){
                if (hourOfDay >=6 && hourOfDay<=22){
                    if (r.nextInt(19)%5==0) {
                        GeoHash gh = GeoHash.fromGeohashString(idActivity.lastLoc);
                        GeoHash ghNew = gh.next(r.nextInt(15) + 1);
                        idActivity.nextToLastLoc = idActivity.lastLoc;
                        idActivity.lastLoc = ghNew.toBase32();
                        idActivity.epoch = ts.getMillis();
                        StringBuilder sb = new StringBuilder();
                        sb.append(id).append(",");
                        sb.append(ts.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:MM:SS"))).append(",");
                        sb.append(idActivity.lastLoc).append(",");
                        sb.append(ghNew.getBoundingBox().getCenter().getLatitude()).append(",");
                        sb.append(ghNew.getBoundingBox().getCenter().getLongitude());
                        try {

                            //writer.write(sb.toString());
                            //writer.newLine();

                        } catch (Exception e) {

                        }
                    }
                }
            }else {
                GeoHash gh = GeoHash.fromGeohashString(idActivity.lastLoc);
                GeoHash ghNew=null;
                if (r.nextBoolean()){
                    ghNew = gh.next(r.nextInt(20) + 1).getEasternNeighbour();
                }else{
                    if (r.nextBoolean())
                    ghNew = gh.next(r.nextInt(20) + 1).getNorthernNeighbour();
                    else
                        ghNew = gh.next(r.nextInt(20) + 1).getSouthernNeighbour();
                }

                idActivity.nextToLastLoc = idActivity.lastLoc;
                idActivity.lastLoc = ghNew.toBase32();
                idActivity.epoch = ts.getMillis();
                StringBuilder sb = new StringBuilder();
                sb.append(id).append(",");
                sb.append(ts.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:MM:SS"))).append(",");
                sb.append(idActivity.lastLoc).append(",");
                sb.append(ghNew.getBoundingBox().getCenter().getLatitude()).append(",");
                sb.append(ghNew.getBoundingBox().getCenter().getLongitude());
                try {

                    writer.write(sb.toString());
                    writer.newLine();

                } catch (Exception e) {

                }

            }
        }
    }

    void generateData(final DateTime ts, int hourOfDay){

        /*
         * 0-3 = Low Rank
         * 4-6 = Mid Rank
         * 7-9 = High Rank
         */
        for(IdMobilityProfile idMf: idStore){

            /* For low rank generate only few pings in a day , restrict 9am to 6pm
                For Mid Rank generate only from 6am to 10pm
                For high Rank just keep generating
             */

            if (idMf.activityRank>=0 && idMf.activityRank<=3){
                if (hourOfDay >=9 && hourOfDay<=18){

                    generateData(idMf.id, idMf.activityRank,ts,ts.getHourOfDay());
                }

            }else if (idMf.activityRank>=4 && idMf.activityRank<=6){
                if (hourOfDay >=6 && hourOfDay<=22){

                    generateData(idMf.id, idMf.activityRank,ts,ts.getHourOfDay());
                }
            }else {

                generateData(idMf.id, idMf.activityRank,ts,ts.getHourOfDay());
            }



        }
    }

    public void kickStart(final String startDay, final int numDays){

        DateTime ts =DateTime.parse(startDay,DateTimeFormat.forPattern("YYYY-MM-DD"));
        DateTime tsEnd = ts.plusDays(1);
        try {
            writer = new BufferedWriter(new FileWriter("/tmp/idgendump-"+startDay+".csv"));
        }catch (Exception e){

        }

        while(ts.isBefore(tsEnd)){

            System.out.println("processing ");

            //increment by 15 min interval

            generateData(ts,ts.getHourOfDay());

            int nextMins = r.nextInt(45);
            ts = ts.plusMinutes(nextMins);
            //ts = ts.plusDays(1);
        }

        try{
            writer.flush();
            writer.close();
        }catch (Exception e){

        }


    }


    public static void main(String[] args) {

        HMDGeneratorV3 genv3 = new HMDGeneratorV3();
        String [] divNames = new String[]{"Bronx","Brooklyn","Manhattan","Queens","Staten-Island"};
        for(String divName : divNames){
            genv3.loadDivGH(divName);
        }

        genv3.loadIdDump("/home/boson/datagen/iddump.csv");
        genv3.kickStart("2023-04-01",1);


    }
}
