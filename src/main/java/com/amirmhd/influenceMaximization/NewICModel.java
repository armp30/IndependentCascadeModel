package com.amirmhd.influenceMaximization;

import com.amirmhd.influenceMaximization.Exception.InvalidActivationValueException;
import com.amirmhd.influenceMaximization.Exception.SeedNotFoundException;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class NewICModel {
    private final Path fileName;
    DirectedMultigraph<String, RelationshipEdge> graph;
    Set<String> seeds;
    long nodeCount;
    double p;
    int montecarloSimulations;
    public NewICModel(DirectedMultigraph graph, Set<String> seeds, double p, int montecarloSimulations, Path fileName) throws SeedNotFoundException, InvalidActivationValueException {
//        System.out.println("Analyzing Graph");

        for (String x : seeds) {
            if (!graph.containsVertex(x)) {
                throw new SeedNotFoundException("Seed " + x + " not in Graph nodes");
            }
        }
        this.graph = (DirectedMultigraph) graph.clone();

        this.seeds = new HashSet<>(seeds);
        this.nodeCount = this.graph.vertexSet().size();
        this.p = p;
        this.montecarloSimulations = montecarloSimulations;
        this.fileName = fileName;
    }

    public List<String> diffuseAll() {
//        Set<RelationshipEdge> triedEdges = new HashSet<>();
        List<String> newSeeds = new ArrayList<>();
        newSeeds.addAll(seeds);
        int i = 0;
        while (i < newSeeds.size()) {
            String source = newSeeds.get(i);
            for (RelationshipEdge e: graph.edgesOf(source)){
                String target = (String) e.getTarget();
                if(!newSeeds.contains(target)){
                    int w = graph.getAllEdges(source,target).size();
                    if(ThreadLocalRandom.current().nextDouble()<(1-Math.pow(1-p,w)))
                        newSeeds.add(target);
                }
            }
            i++;
        }
        return newSeeds;
    }

    public double avgSize(){
        double avg = 0;
        for (int i = 0; i < montecarloSimulations; i++) {
            avg+= diffuseAll().size();
        }
        double v = avg / montecarloSimulations;
        try {
            Files.writeString(fileName, seeds.toArray()[0] + "," + v + System.lineSeparator(),  StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;
    }
}
