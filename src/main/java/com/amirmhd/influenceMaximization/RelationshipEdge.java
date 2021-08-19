package com.amirmhd.influenceMaximization;

import org.jgrapht.graph.DefaultEdge;

public class RelationshipEdge extends DefaultEdge {

    private static final long serialVersionUID = 3258408452177932856L;
    private double probability;

    public RelationshipEdge() {
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public Object getSource() {
        return super.getSource();
    }

    @Override
    public Object getTarget() {
        return super.getTarget();
    }

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget() + " : " + probability + ")";
    }
}