package com.amirmhd.influenceMaximization;

import com.amirmhd.influenceMaximization.Exception.GraphMismatchException;
import com.amirmhd.influenceMaximization.Exception.InvalidActivationValueException;
import com.amirmhd.influenceMaximization.Exception.SeedNotFoundException;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.*;

public class IndependentCascade {
    DirectedMultigraph<String, RelationshipEdge> graph;
    Set<String> seeds;
    long nodeCount;

    public IndependentCascade(DirectedMultigraph graph, Set<String> seeds) throws SeedNotFoundException, InvalidActivationValueException {
//        System.out.println("Analyzing Graph");

        for (String x : seeds) {
            if (!graph.containsVertex(x)) {
                throw new SeedNotFoundException("Seed " + x + " not in Graph nodes");
            }
        }
        this.graph = (DirectedMultigraph) graph.clone();

        this.graph.edgeSet().forEach(e -> {
            e.setProbability(0.1d);
        });

        this.seeds = new HashSet<>(seeds);
        this.nodeCount = this.graph.vertexSet().size();


    }

    private boolean propSuccess(RelationshipEdge e) {
        double probability = e.getProbability();
        if (probability > 1) {
            probability = 1;
            e.setProbability(1);
        }
        return new Random().nextDouble() <= probability;
    }

    public Set<String> diffuseOneRound(Set<RelationshipEdge> triedEdges) {
        Set<String> activatedNodesOfThisRound = new HashSet<>();
        Set<RelationshipEdge> currentTriedEdges = new HashSet<>();
        for (String s : seeds) {
            for (RelationshipEdge e : graph.outgoingEdgesOf(s)) {
                if (seeds.contains(e.getTarget()) || triedEdges.contains(e) || currentTriedEdges.contains(e))
                    continue;
                if (propSuccess(e))
                    activatedNodesOfThisRound.add((String) e.getTarget());
                currentTriedEdges.add(e);
            }
        }
        triedEdges.addAll(currentTriedEdges);
        seeds.addAll(activatedNodesOfThisRound);
        return activatedNodesOfThisRound;
    }

    public List<Set<String>> diffuseKRounds(int steps) {

        Set<RelationshipEdge> triedEdges = new HashSet<>();
        List<Set<String>> layers = new ArrayList<>();
        layers.add(seeds);
        for (int i = 0; i < steps && seeds.size() < nodeCount; i++) {
            int oldSize = seeds.size();
            layers.add(diffuseOneRound(triedEdges));
            if (seeds.size() == oldSize)
                break;
        }
        return layers;
    }

    public int diffuseKRoundsNumber(int steps) {
        Set<RelationshipEdge> triedEdges = new HashSet<>();
        for (int i = 0; i < steps && seeds.size() < nodeCount; i++) {
            int oldSize = seeds.size();
            diffuseOneRound(triedEdges);
            if (seeds.size() == oldSize)
                break;
        }
        return seeds.size();
    }

    public List<Set<String>> diffuseAll() {
        Set<RelationshipEdge> triedEdges = new HashSet<>();
        List<Set<String>> layers = new ArrayList<>();
        layers.add(seeds);
        while (seeds.size() < nodeCount) {
            int oldSize = seeds.size();
            layers.add(diffuseOneRound(triedEdges));
            if (seeds.size() == oldSize)
                break;
        }
        return layers;
    }

}
