package ai.geolambda;

import net.openhft.hashing.LongTupleHashFunction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.UUID;

public class IdGenerator {

    public static void main(String[] args) {

        //Queens,Brooklyn,Manhattan,Bronx,Staten-Island
        //Bronx = 1,379,946  | 12,634
        //Brooklyn = 2,590,516 | 14,417
        //Manhattan = 1,596,273 | 27,203
        //Queens = 2,278,029 | 8,090
        //Staten-Island = 491,133 | 3,297


        String name = "Staten-Island";
        int actualPop = 491133;
        String outFile = String.format("/home/boson/datagen/nyc-iddump/id-%s.csv", name);
        int maxPop = (int) (0.584*actualPop);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            for(int i =0;i < maxPop;i++){
                String idStr = UUID.randomUUID().toString().toLowerCase();

                long [] hashedContent  = LongTupleHashFunction.murmur_3().hashBytes(idStr.getBytes());
                long id = hashedContent[0]>>>1;
                writer.write(""+id);
                writer.newLine();

            }

            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
