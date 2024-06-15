package org.example;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> pliki = new ArrayList<>();

        pliki.add("testData/R101.txt");
        pliki.add("testData/R102.txt");
        pliki.add("testData/R103.txt");

        pliki.add("testData/R201.txt");
        pliki.add("testData/R202.txt");
        pliki.add("testData/R203.txt");

        pliki.add("testData/C101.txt");
        pliki.add("testData/C102.txt");
        pliki.add("testData/C103.txt");

        pliki.add("testData/C201.txt");
        pliki.add("testData/C202.txt");
        pliki.add("testData/C203.txt");

        pliki.add("testData/RC101.txt");
        pliki.add("testData/RC102.txt");
        pliki.add("testData/RC103.txt");

        pliki.add("testData/RC201.txt");
        pliki.add("testData/RC202.txt");
        pliki.add("testData/RC203.txt");
        for (String plik : pliki) {
            Ant_Algorithm a = new Ant_Algorithm(plik);
        }
    }
}