package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Ant extends JFrame {
    ArrayList<int[]> visited;
    ArrayList<int[]> notVisited;
    ArrayList<ArrayList<int[]>> visitedForVehicle = new ArrayList<>();
    ArrayList<int[]> visitedPairs = new ArrayList<>();
    Ant_Algorithm algorithm;
    int[] actualLocation;
    int[] vehiclesWithoutChoice;
    int remainingLocations;
    int[] remainingSpace;
    int numberOfVehicles;
    int actualVehicle;
    double[] time;

    public Ant(Ant_Algorithm algorithm) {
        this.algorithm = algorithm;

        actualLocation = new int[algorithm.MAX_NUMBER_OF_VEHICLES];

        visited = new ArrayList<>(algorithm.numberOfPoints);
        visited.add(algorithm.fileData.get(actualLocation[0]));

        notVisited = (ArrayList<int[]>) algorithm.fileData.clone();
        notVisited.remove(visited.get(0));

        remainingLocations = algorithm.numberOfPoints - visited.size();
        remainingSpace = new int[algorithm.MAX_NUMBER_OF_VEHICLES];
        remainingSpace[0] = algorithm.CAPACITY;

        numberOfVehicles = 1;
        actualVehicle = 0;
        vehiclesWithoutChoice = new int[algorithm.MAX_NUMBER_OF_VEHICLES];

        time = new double[algorithm.MAX_NUMBER_OF_VEHICLES];
        time[0] = 0;
    }

    public Ant() {

    }

    public void Go() {
        int temp = 0;
        for (int veh = 0; veh < numberOfVehicles; veh++)  {
            actualVehicle = veh;
            if (vehiclesWithoutChoice[actualVehicle] == 1) {
                temp++;
                continue;
            }

            ArrayList<int[]> toVisit = chooseToVisit();

            int maxDemand = Integer.MIN_VALUE;
            for (int[] ints : toVisit) {
                if (ints[3] > maxDemand) maxDemand = ints[3];
            }
            if (remainingSpace[actualVehicle] < maxDemand) {
                addVisit(0);
                continue;
            }

            if (toVisit.size() == 0) {
                temp++;
                vehiclesWithoutChoice[veh] = 1;
                if (temp == numberOfVehicles) {
                    numberOfVehicles++;
                    Go();
                    return;
                }
                continue;
            }

            if (toVisit.size() == 1) {
                addVisit(toVisit.get(0)[0]);
                return;
            }


            Random r = new Random();
            if (r.nextDouble() < (double) (algorithm.RANDOM_PLACE) / 100) {
                RandomPlace(toVisit);
            } else {
                RouletteSelection(toVisit);
            }
        }
    }

    public ArrayList<double[]> CalculatePH (ArrayList<int[]> notVisited) {
        ArrayList<double[]> PH = new ArrayList<>();
        double value;
        for (int i = 0; i < notVisited.size(); i++) {
                if (!(algorithm.distances[actualLocation[actualVehicle]][notVisited.get(i)[0]] == 0)) {
                    value = (Math.pow(algorithm.pheromones[actualLocation[actualVehicle]][notVisited.get(i)[0]], algorithm.ALPHA)
                            * Math.pow(1 / algorithm.distances[actualLocation[actualVehicle]][notVisited.get(i)[0]], algorithm.BETA));
                } else {
                    value = (Math.pow(algorithm.pheromones[actualLocation[actualVehicle]][notVisited.get(i)[0]], algorithm.ALPHA)
                            * Math.pow(1 / 0.001, algorithm.BETA));
                }
                if (value >= Double.MAX_VALUE) {
                    throw new RuntimeException("Błąd podczas wyliczenia PH");
                } else if (value == 0) {
                    throw new RuntimeException("PH przy obliczeniu wyniosło 0 (najprawdopodobniej feromony na tej ścieżce były o tyle małe że stały się zerem)");
                } else {
                    PH.add(i, new double[]{value, notVisited.get(i)[0]});
                }
        }
        return PH;
    }

    public double SumPH (ArrayList<double[]> PH) {
        double sum = 0;
        for (double[] v : PH) {
                sum += v[0];
        }
        return sum;
    }

    public void RandomPlace (ArrayList<int[]> toVisit) { //Można zoptymizować, zamiast pownego wylosowania naprzykłąd dodawanie liczby
        Random r = new Random();
        int result;
        do {
            result = r.nextInt(toVisit.size());
        } while (visited.contains(toVisit.get(result)));

        addVisit(toVisit.get(result)[0]);

    }

    public void RouletteSelection (ArrayList<int[]> toVisit) {
        ArrayList<double[]> PH = CalculatePH(toVisit);
        double[][] chance = new double[toVisit.size()][2];
        double sumPH = SumPH(PH);
        for (int i = 0; i < PH.size(); i++) {
                chance[i][0] = PH.get(i)[0] / sumPH;
                chance[i][1] = PH.get(i)[1];
        }
        double sum = 0;
        double[] ranges = new double[toVisit.size()];
        for (int i = 0; i < toVisit.size(); i++) {
            ranges[i] = sum + chance[i][0];
            sum += chance[i][0];
        }
        Random r = new Random();
        double selection = r.nextDouble();
        int result = Integer.MIN_VALUE;
        for (int i = 0; i < toVisit.size(); i++) {
            if (i == 0 && selection <= ranges[i]) {
                result = (int) chance[i][1];
                break;
            } else if (i > 0 && selection > ranges[i - 1] && selection <= ranges[i]) {
                result = (int) chance[i][1];
                break;
            } else if (i == toVisit.size() - 1 && selection >= ranges[i]) {
                result = (int) chance[i][1];
            }
        }

        if (result == Integer.MIN_VALUE) {
            throw new RuntimeException("Nie znaleziono punktu w selekcji ruletkowej " +
                    "(może być to związane z wyliczaniem feromonów)");
        } else if (visited.contains(algorithm.fileData.get(result))) {
            throw new RuntimeException("Znaleziono istniejące rozwiązanie w selekcji ruletkowej");
        } else {
            addVisit(result);
        }
    }

    public ArrayList<int[]> chooseToVisit () {

        ArrayList<int[]> toVisit = new ArrayList<>();
        for (int[] point : notVisited) {
            double t = algorithm.distances[actualLocation[actualVehicle]][point[0]];
            double timePlusT = time[actualVehicle] + t;
            if (timePlusT > point[4] &&
                    timePlusT < point[5]) {
                //jeśli time + t większe od godziny rozpoczęcia dostawy i mniejsze od godziny zakończenia
                toVisit.add(algorithm.fileData.get(point[0]));
            }
        }

        if (toVisit.size() == 0) {
            double[] differences = new double[notVisited.size()];
            int accessDifference = algorithm.maxTime / 30;

            for (int i = 0; i < notVisited.size(); i++) {
                differences[i] = notVisited.get(i)[4] -
                        (algorithm.distances[actualLocation[actualVehicle]][notVisited.get(i)[0]] + time[actualVehicle]);
                if (differences[i] >= 0 && differences[i] < accessDifference) {
                    toVisit.add(notVisited.get(i));
                }
            }

            while (toVisit.size() == 0) {
                for (int i = 0; i < differences.length; i++) {
                    if (differences[i] >= 0 && differences[i] < accessDifference) {
                        toVisit.add(notVisited.get(i));
                    }
                }
                accessDifference += algorithm.maxTime / 30;
                if (accessDifference > algorithm.maxTime) {
                    return toVisit;
                }
            }
        }
        return toVisit;
    }

    public double getDistance () {
        double sumDistance = 0;
        for (ArrayList<int[]> ints : visitedForVehicle) {
            int[] first = ints.get(0);
            double distance = 0;
            for (int i = 1; i < ints.size(); i++) {
                int[] second = ints.get(i);
                distance += algorithm.distances[first[0]][second[0]];
                first = second;
            }

            if (ints.get(ints.size() - 1)[0] != 0) {
                distance += algorithm.distances[ints.get(ints.size() - 1)[0]][0];
            }
            sumDistance += distance;
        }

        return sumDistance;
    }


    public void addVisit(int result){
        visited.add(algorithm.fileData.get(result));
        notVisited.remove(visited.get(visited.size() - 1));

        try {
            if (visitedForVehicle.get(actualVehicle) == null) {
                visitedForVehicle.add(new ArrayList<>());
            }
        } catch (IndexOutOfBoundsException err) {
            visitedForVehicle.add(new ArrayList<>());
        }
        visitedForVehicle.get(actualVehicle).add(algorithm.fileData.get(result));

        visitedPairs.add(new int[]{actualLocation[actualVehicle], result});

        if (time[actualVehicle] + algorithm.distances[actualLocation[actualVehicle]][result] > algorithm.fileData.get(result)[5]) {
            throw new RuntimeException("Spóźnienie");
        }

        if (time[actualVehicle] < algorithm.fileData.get(actualLocation[actualVehicle])[4]) {
            time[actualVehicle] += algorithm.distances[actualLocation[actualVehicle]][result];
            double dif = algorithm.fileData.get(actualLocation[actualVehicle])[4] - time[actualVehicle];
            time[actualVehicle] += dif + algorithm.fileData.get(result)[6];
        } else {
            time[actualVehicle] += algorithm.distances[actualLocation[actualVehicle]][result] + algorithm.fileData.get(result)[6];
        }
        actualLocation[actualVehicle] = result;
        if (result == 0) {
            remainingSpace[actualVehicle] = algorithm.CAPACITY;
            return;
        } else {
            remainingSpace[actualVehicle] -= algorithm.fileData.get(result)[3];
        }
        if (remainingSpace[actualVehicle] < 0) {
            throw new RuntimeException("Ujemna liczba towarów w samochodzie");
        }
        remainingLocations--;
    }

    public void plotRoute(String label) {
        label = label.substring(9);
        label = label.substring(0, label.length() - 3);
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < visitedForVehicle.size(); i++) {
                XYSeries series = new XYSeries("Samochód nr " + (i + 1), false);
            for (int[] point : visitedForVehicle.get(i)) {
                series.add(point[1], point[2]);
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                label,
                "X",
                "Y",
                dataset
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemLabelGenerator generator = new StandardXYItemLabelGenerator() {
            @Override
            public String generateLabel(XYDataset dataset, int series, int item) {
                return String.valueOf(item);
            }
        };
        plot.getRenderer().setBaseItemLabelGenerator(generator);
        plot.getRenderer().setBaseItemLabelsVisible(true);

        try {
            ChartUtilities.saveChartAsPNG(new File("wykresy/Heuristic3+Population60+Random5/" + label + "png"), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}