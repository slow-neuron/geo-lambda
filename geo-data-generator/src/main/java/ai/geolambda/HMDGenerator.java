package ai.geolambda;

import com.sun.source.tree.Tree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class HMDGenerator {

    final Map<String, Map<String, List<String>>> ghMap = new TreeMap<>();

    //Assign Distributions to each gh5 or gh7
    //each gh5/gh6 has either one of the distribution of mobility
    //Gamma,ZipF, normal,Poisson

    static Map<String,DivisionCharacteristics> divisionNameFileMapping = new HashMap<>();
    String baseFilePath = "/home/boson/datagen/nyc-gh7";
    void loadDefnFile(final String defnFile){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(defnFile));
            String line = reader.readLine();
            while(line!=null){
                String [] tkns = line.trim().split(",");
                if (tkns.length>0){
                    String divName = tkns[0];
                    String dataFile = baseFilePath+"/"+tkns[1];
                    Integer population = Integer.parseInt(tkns[2]);
                    DivisionCharacteristics d = new DivisionCharacteristics();
                    d.density = 1.0d;
                    d.name = divName;
                    d.population = Long.valueOf(population);
                    d.totalGh=0;
                    d.ghCollectionDataFile = dataFile;

                    divisionNameFileMapping.put(d.name,d);


                    //System.out.println("div " + divName + " " + population);
                }
                line = reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void loadDump(final String divName,final String ghDumpFile){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ghDumpFile));
            String line = reader.readLine();
            int recordCount = 0;
            while(line!=null){

                String [] tkns  = line.trim().split(",");
                if (tkns.length>0){
                    String gh5 = tkns[0].substring(0,5);
                    ghMap.computeIfAbsent(divName,c-> new HashMap<>())
                            .computeIfAbsent(gh5, c1-> new ArrayList<>()).add(tkns[0]);
                    recordCount++;
                }

                line = reader.readLine();
            }
            reader.close();
            divisionNameFileMapping.get(divName).totalGh = recordCount;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void kickStartProcessing(final String defnFile ){
        loadDefnFile(defnFile);
        for(String divName : divisionNameFileMapping.keySet()){
            System.out.println("starte processing " + divName + " " + divisionNameFileMapping.get(divName).ghCollectionDataFile);
            loadDump(divName,divisionNameFileMapping.get(divName).ghCollectionDataFile);
            System.out.println("processed " + divName + " " + divisionNameFileMapping.get(divName).totalGh);
        }

    }

    public static void main(String[] args) {


        String defnFile = "/home/boson/geo-processing/geo-lambda/geo-data-generator/src/main/resources/DivisionFileMap.csv";
        HMDGenerator hmdGenerator = new HMDGenerator();
        hmdGenerator.kickStartProcessing(defnFile);



    }
}
