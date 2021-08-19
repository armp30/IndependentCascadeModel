package com.amirmhd.influenceMaximization.example;

import com.amirmhd.influenceMaximization.Exception.GraphMismatchException;
import com.amirmhd.influenceMaximization.Exception.InvalidActivationValueException;
import com.amirmhd.influenceMaximization.Exception.SeedNotFoundException;
import com.amirmhd.influenceMaximization.IndependentCascade;
import com.amirmhd.influenceMaximization.RelationshipEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.csv.CSVFormat;
import org.jgrapht.nio.csv.CSVImporter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) throws IOException, InvalidActivationValueException, SeedNotFoundException, GraphMismatchException {
        DirectedMultigraph<String, RelationshipEdge> graph = new DirectedMultigraph<>(RelationshipEdge.class);
        JFileChooser fileChooser = new JFileChooser("/home/arm/PycharmProjects/article1/datasets/txt");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showOpenDialog(null);
        File selectedFile = fileChooser.getSelectedFile();
        String path = selectedFile.getPath();
        String graphString = Files.readString(Paths.get(path));
        Path fileName = Paths.get(selectedFile.getName());

        DelimiterChooser delimiterChooser = new DelimiterChooser(graphString.substring(0, 20));
        char delimiter;
        do {
            delimiter = delimiterChooser.getDelimiter();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (delimiter == '1');


        CSVImporter<String, RelationshipEdge> importer = new CSVImporter<>(CSVFormat.EDGE_LIST, delimiter);
        importer.setVertexFactory(id -> id);
        importer.importGraph(graph, new StringReader(graphString));
        Set<String> nodes = graph.vertexSet();
        System.out.println(nodes);
        System.out.println("Nodes : " + nodes.size());
        System.out.println("Edges : " + graph.edgeSet().size());
        long startTime = System.currentTimeMillis();
        HashMap<String, Double> propagation = new HashMap<>();
        int montecarloSimulations = 100;
        int steps = 100;
        Files.writeString(fileName,"Start Time : " + (startTime)/1000 + " s" + System.lineSeparator());
        Files.writeString(fileName,"Nodes : " + nodes.size() + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"Edges : " + graph.edgeSet().size() + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"MonteCarloSimulations : " + montecarloSimulations + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"Steps : " + steps + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        int n=0;
        for (String node : nodes) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            Set<Callable<String>> callables = new HashSet<>();
            HashSet<String> seeds = new HashSet<>();
            seeds.add(node);
            List<Integer> values = new ArrayList<>();

            for (int i = 0; i < montecarloSimulations; i++) {
                callables.add(() -> {
                    try {
                        IndependentCascade independentCascade = new IndependentCascade(graph, seeds);
                        values.add(independentCascade.diffuseKRoundsNumber(steps));

                    } catch (SeedNotFoundException e) {
                        e.printStackTrace();
                    } catch (InvalidActivationValueException e) {
                        e.printStackTrace();
                    }
                    return "finished";
                });
            }
            try {
                executor.invokeAll(callables);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double b = values.stream().mapToInt(a->a).sum();
            propagation.put(node, b);
            Files.writeString(fileName, node + "," +Double.toString(b) + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println(node + " : " +(System.currentTimeMillis()-startTime)/1000);
            if(n++%50==0)
                Files.writeString(fileName,"Time : " + (System.currentTimeMillis()-startTime)/1000 + " s" + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        Files.writeString(fileName,"Execution Time : " + (System.currentTimeMillis()-startTime)/1000 + " s" + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    }
}