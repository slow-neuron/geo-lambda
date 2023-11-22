package ai.geolambda;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdActivityStore {
    public static final IdActivityStore _instance = new IdActivityStore();

    private IdActivityStore(){}

    public static IdActivityStore getInstance(){return _instance;}

    Map<String,IdActivity> idActivityMap = new ConcurrentHashMap<>();



}
