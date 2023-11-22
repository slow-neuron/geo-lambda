package ai.geolambda;

import java.io.Serializable;

public class DivisionCharacteristics implements Serializable {

    String name;
    int totalGh;
    Long population;
    double density;

    String ghCollectionDataFile;
    String weightsFile;

    public DivisionCharacteristics(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalGh() {
        return totalGh;
    }

    public void setTotalGh(int totalGh) {
        this.totalGh = totalGh;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public String getGhCollectionDataFile() {
        return ghCollectionDataFile;
    }

    public void setGhCollectionDataFile(String ghCollectionDataFile) {
        this.ghCollectionDataFile = ghCollectionDataFile;
    }

    public String getWeightsFile() {
        return weightsFile;
    }

    public void setWeightsFile(String weightsFile) {
        this.weightsFile = weightsFile;
    }
}
