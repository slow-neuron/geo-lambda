package ai.geolambda;

import java.io.Serializable;

public class IdActivity implements Serializable {

    public String lastLoc;
    public String nextToLastLoc;

    public Long epoch;

    public IdActivity(){}
}
