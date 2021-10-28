package com.amirmhd.influenceMaximization.example;

import com.amirmhd.influenceMaximization.Exception.GraphMismatchException;
import com.amirmhd.influenceMaximization.Exception.InvalidActivationValueException;
import com.amirmhd.influenceMaximization.Exception.SeedNotFoundException;
import com.amirmhd.influenceMaximization.IndependentCascade;
import com.amirmhd.influenceMaximization.NewICModel;
import com.amirmhd.influenceMaximization.RelationshipEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.csv.CSVFormat;
import org.jgrapht.nio.csv.CSVImporter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test2 {
    public static void main(String[] args) throws IOException, InvalidActivationValueException, SeedNotFoundException, GraphMismatchException, InterruptedException {
        DirectedMultigraph<String, RelationshipEdge> graph = new DirectedMultigraph<>(RelationshipEdge.class);
//        JFileChooser fileChooser = new JFileChooser("/home/arm/PycharmProjects/article1/datasets/txt");
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        fileChooser.showOpenDialog(null);
        File selectedFile = new File(args[0]);
//        String path = selectedFile.getPath();
        String graphString = Files.readString(Path.of(selectedFile.getPath()));
        String name = selectedFile.getName();
        if (name.contains(".txt"))
            name = name.replace(".txt","");
        Path path = Paths.get("Resaults" +File.separator+ name);
        if(!Files.isDirectory(path))
            Files.createDirectory(path);
        else {
            System.out.println("Directory Exists");
        }


//        DelimiterChooser delimiterChooser = new DelimiterChooser(graphString.substring(0, 20));
        char delimiter;
        switch (args[1]){
            case "1":
                delimiter=' ';
                break;
            case "2":
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
        float probability = Float.parseFloat(args[3]);
        Path fileName = Paths.get(path.toString() + File.separator +selectedFile.getName()
                + "-"
                + probability
                + "-"
                + montecarloSimulations
                + "-"
                + LocalDateTime.now().toString()
                +".csv");

        Files.writeString(fileName,"#Start Time : " + (startTime)/1000 + " s" + System.lineSeparator());
        Files.writeString(fileName,"#Nodes : " + nodes.size() + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"#Edges : " + graph.edgeSet().size() + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"#MonteCarloSimulations : " + montecarloSimulations + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.writeString(fileName,"#probability : " + probability + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        int n=0;
        ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(args[4]));
        Set<Callable<String>> callables = new HashSet<>();
        Map<String,Double> values = new HashMap<>();
        for (String node : nodes) {
            HashSet<String> seeds = new HashSet<>();
            seeds.add(node);
            System.out.println(node+" ----------");
            callables.add(() -> {
                try {
                    NewICModel icModel = new NewICModel(graph,seeds,probability, montecarloSimulations,fileName);
                    values.put(node,icModel.avgSize());
                    System.out.println(node);
                } catch (SeedNotFoundException e) {
                    e.printStackTrace();
                } catch (InvalidActivationValueException e) {
                    e.printStackTrace();
                }
                return "finished";
            });

//            if(++n%10==0){
//                long time = (System.currentTimeMillis()-startTime)/1000;
//                System.out.println("n : " + n);
//                System.out.println("Elapsed : " +time);
//                System.out.println("Remaining : " + ((double)time/n)*(nodes.size()-n));
//            }
//            if(++n%50==0)
//                Files.writeString(fileName,"Time : " + (System.currentTimeMillis()-startTime)/1000 + " s" + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        try {
            executor.invokeAll(callables);
            System.out.println("finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("finished");
            executor.shutdownNow();
            while (!executor.isShutdown()){
                System.out.println("waiting");
                executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            }
        }
//        for (String node: values.keySet()) {
//            System.out.println(node);
//            Files.writeString(fileName, node + "," + values.get(node) + System.lineSeparator(), StandardOpenOption.APPEND);
//        }
        Files.writeString(fileName,"#Execution Time : " + (System.currentTimeMillis()-startTime)/1000 + " s" + System.lineSeparator(), StandardOpenOption.APPEND);

    }
}