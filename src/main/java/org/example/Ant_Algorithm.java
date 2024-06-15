package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ant_Algorithm {
    int numberOfPoints;
    int NUMBER_OF_ANTS = 60;
    double ALPHA = 1;
    double BETA = 3;
    int EVAPORATION_FACTOR = 10;
    int RANDOM_PLACE = 5;
    int NUMBER_OF_ITERATIONS = 1000;
    int CAPACITY;
    int MAX_NUMBER_OF_VEHICLES = 50;
    public ArrayList<int[]> fileData;
    public double[][] pheromones;
    public double[][] distances;
    Ant bestAnt;
    double bestDistance;
    double worstDistance;
    double bestTime;
    int maxTime;


    Ant_Algorithm(String filePath) {
        System.out.println("////////////////PLIK " + filePath + "///////////////////");
        System.out.print("(");
        ReadFile(filePath);
        CreatePheromoneTraces();
        CalculateDistances();
        List<Ant> colony;
        bestDistance = Integer.MAX_VALUE;
        worstDistance = Integer.MIN_VALUE;
        ArrayList<Double> bests = new ArrayList<>(20);
        ArrayList<Ant> bestsAnts = new ArrayList<>(20);
        for (int po = 0; po < 5; po++) {
            for (int iter = 0; iter < NUMBER_OF_ITERATIONS; iter++) {
                colony = CreateColony();
                int temp = 0;
                while (temp != NUMBER_OF_ANTS) {
                    temp = 0;
                    for (int i = 0; i < NUMBER_OF_ANTS; i++) {
                        if (colony.get(i).remainingLocations == 0) {
                            temp++;
                            continue;
                        }
                        colony.get(i).Go();
                    }
                }
                UpdatePheromones(colony);
                UpdateBest(colony);
            }
            System.out.print("*");
            bestsAnts.add(bestAnt);
            bests.add(bestDistance);
            bestDistance = Integer.MAX_VALUE;
            bestAnt = null;
        }
        double bestbest = Integer.MAX_VALUE;
        double sum = 0;
        Ant best = new Ant();
        for (int i = 0; i < 5; i ++) {
            if (bests.get(i) < bestbest) {
                bestbest = bestsAnts.get(i).getDistance();
                best = bestsAnts.get(i);
            }
            sum += bests.get(i);
        }

        System.out.println(")" + "\nŚrednie: " + (sum / 5) + "\nNajlepsze: " + bestbest
                + "\nIlość samochodów: " + best.numberOfVehicles);
        best.plotRoute(filePath);

    }

    public void ReadFile (String filePath) {
        numberOfPoints = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            while (br.readLine() != null) {
                numberOfPoints++;
            }
            numberOfPoints -= 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int numberOfLine = 0;
        fileData = new ArrayList<>();


        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            CAPACITY = Integer.parseInt(line);
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[\\s]{2,}", " ");
                String[] dataFileString = line.split(" ");
                fileData.add(new int[7]);
                for (int i = 1; i < dataFileString.length; i++) {
                    if (i == 1) {
                        fileData.get(numberOfLine)[i - 1] = Integer.parseInt(dataFileString[i]) - 1;
                    } else {
                        fileData.get(numberOfLine)[i - 1] = (int) Double.parseDouble(dataFileString[i]);
                    }
                }
                numberOfLine++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        maxTime = fileData.get(0)[5];
    }
    public void CreatePheromoneTraces () {
        pheromones = new double[numberOfPoints][numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            Arrays.fill(pheromones[i], 1.0);
        }
    }

    public void CalculateDistances () {
        distances = new double[numberOfPoints][numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            ArrayList<Double> temp = new ArrayList<>(numberOfPoints);
            for (int j = 0; j < numberOfPoints; j++) {
                distances[i][j] = Math.sqrt(
                        Math.pow(fileData.get(j)[1] - fileData.get(i)[1], 2)
                                + Math.pow(fileData.get(j)[2] - fileData.get(i)[2], 2));

            }
        }
    }

    public ArrayList<Ant> CreateColony () {
        ArrayList<Ant> colony = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ANTS; i++) {
            colony.add(new Ant(this));
        }
        return colony;
    }

    public void UpdatePheromones (List<Ant> colony) {
        for (int i = 0; i < numberOfPoints; i++) {
            for (int j = 0; j < numberOfPoints; j++) {
                if (!(pheromones[i][j] < 4.9407E-300))
                    pheromones[i][j] = pheromones[i][j] * ((double) EVAPORATION_FACTOR / 100);
            }
        }


        for (int t = 0; t < NUMBER_OF_ANTS; t++) {
            ArrayList<int[]> pairs = colony.get(t).visitedPairs;
            for (int[] ints : pairs) {
                    pheromones[ints[0]][ints[1]] += 1 / colony.get(t).getDistance();
                    pheromones[ints[1]][ints[0]] += 1 / colony.get(t).getDistance();
            }
        }


    }

    private void UpdateBest(List<Ant> colony) {
        for (int i = 0; i < NUMBER_OF_ANTS; i++) {
            if (colony.get(i).getDistance() < bestDistance) {
                bestDistance = colony.get(i).getDistance();
                bestAnt = colony.get(i);
            }
        }
    }

}
