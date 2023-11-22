package ai.geolambda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HMDGeneratorV2 {

    String idDumpBaseDir = "/home/boson/datagen/id-hl";
    String [] divNames = new String[]{"Bronx","Brooklyn","Manhattan","Queens","Staten-Island"};
    Map<String,IdMobilityProfile> mobilityProfileMap = new HashMap<>();

    BufferedWriter writer;
    Random r = new Random();

    void loadDump(final String divName,final String dataDump){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataDump));
            String line = reader.readLine();
            line = reader.readLine(); //skipp the header
            while(line!=null) {
                String [] tkns = line.trim().split(",");
                String id = tkns[0];
                String hl = tkns[1];
                StringBuilder sb = new StringBuilder();
                sb.append(id).append(",");
                sb.append(hl).append(",");
                sb.append(tkns[2]).append(",");
                sb.append(tkns[3]).append(",");
                sb.append(r.nextInt(10)).append(",");
                sb.append(divName);
                writer.write(sb.toString());
                writer.newLine();

                line = reader.readLine();
            }
        }catch (Exception e){

        }
    }

    void kickStart(){

        try{

            writer = new BufferedWriter(new FileWriter("/tmp/iddump.csv"));

        }catch (Exception e){

        }
        for(String divName : divNames){
            String dumpFile = idDumpBaseDir+ String.format("/id-hl-%s.csv", divName);
            loadDump(divName,dumpFile);
        }


        try{
            writer.flush();
            writer.close();
        }catch (Exception e){

        }
    }

    public static void main(String[] args) {

        HMDGeneratorV2 hmdGeneratorV2 = new HMDGeneratorV2();
        hmdGeneratorV2.kickStart();
    }
}
