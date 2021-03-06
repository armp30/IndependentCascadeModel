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
import java.util.concurrent.*;

public class Test {
    public static void main(String[] args) throws IOException, InvalidActivationValueException, SeedNotFoundException, GraphMismatchException, InterruptedException {
        DirectedMultigraph<String, RelationshipEdge> graph = new DirectedMultigraph<>(RelationshipEdge.class);
//        JFileChooser fileChooser = new JFileChooser("/home/arm/PycharmProjects/article1/datasets/txt");
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        fileChooser.showOpenDialog(null);
        File selectedFile = new File(args[0]);
//        String path = selectedFile.getPath();
        String graphString = Files.readString(Path.of(selectedFile.getPath()));
        Path fileName = Paths.get(selectedFile.getName());

//        DelimiterChooser delimiterChooser = new DelimiterChooser(graphString.substring(0, 20));
        char delimiter;
        switch (args[1]){
            case "1":
                delimiter=' ';
                break;
            case "2":
                delimiter='\t';
                break;
            default:
                delimiter='\t';
        }
//        do {
//            delimiter = delimiterChooser.getDelimiter();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        } while (delimiter == '1');


        CSVImporter<String, RelationshipEdge> importer = new CSVImporter<>(CSVFormat.EDGE_LIST, delimiter);
        importer.setVertexFactory(id -> id);
        importer.importGraph(graph, new StringReader(graphString));
        Set<String> nodes = graph.vertexSet();
        System.out.println(nodes);
        System.out.println("Nodes : " + nodes.size());
        System.out.println("Edges : " + graph.edgeSet().size());
        long startTime = System.currentTimeMillis();
        HashMap<String, Double> propagation = new HashMap<>();
        int montecarloSimulations = Integer.parseInt(args[2]);
        int steps = Integer.parseInt(args[3]);
        Files.writeString(fileName,"#Start Time : " + (startTime)/1000 + " s" + System.lineSeparator());
        Files.writeString(fileName,"#Nodes : " + nodes.size() + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"#Edges : " + graph.edgeSet().size() + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"#MonteCarloSimulations : " + montecarloSimulations + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"#Steps : " + steps + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        int n=0;
        for (String node : nodes) {
            ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(args[4]));
            Set<Callable<String>> callables = new HashSet<>();
            HashSet<String> seeds = new HashSet<>();
            seeds.add(node);
            List<Integer> values = new ArrayList<>();
            System.out.println(node+" ----------");
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
            } finally {
                executor.shutdownNow();
                while (!executor.isShutdown()){
                    System.out.println("waiting");
                    executor.awaitTermination(100, TimeUnit.MILLISECONDS);
                }
            }
            double b = (double) values.stream().mapToInt(a->a).sum()/montecarloSimulations;
            propagation.put(node, b);
            Files.writeString(fileName, node + "," + b + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if(++n%10==0){
                long time = (System.currentTimeMillis()-startTime)/1000;
                System.out.println("n : " + n);
                System.out.println("Elapsed : " +time);
                System.out.println("Remaining : " + ((double)time/n)*(nodes.size()-n));
            }
            executor.shutdownNow();
//            if(++n%50==0)
//                Files.writeString(fileName,"Time : " + (System.currentTimeMillis()-startTime)/1000 + " s" + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        Files.writeString(fileName,"#Execution Time : " + (System.currentTimeMillis()-startTime)/1000 + " s" + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    }
}