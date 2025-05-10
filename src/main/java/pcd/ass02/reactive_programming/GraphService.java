package pcd.ass02.reactive_programming;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;



public class GraphService {

    private final Graph graph;


    public GraphService() {
        // configura il renderer di GraphStream per Swing
        //System.setProperty("org.graphstream.ui", "swing");

        // crea un grafo con nome e abilita creazione automatica dei nodi
        graph = new SingleGraph("Dipendenze");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        // visualizza il grafo in una finestra separata
        graph.display();
    }

    /**
     * Aggiunge una dipendenza al grafo. Ogni dipendenza è un arco diretto
     * da 'source' a 'target'. I nodi vengono creati automaticamente se mancanti.
     */
    public void addDependency(Dependency dep) {
        String edgeId = dep.getSource() + "->" + dep.getTarget();
        if (graph.getEdge(edgeId) == null) {
            graph.addEdge(edgeId, dep.getSource(), dep.getTarget(), true);
        }
    }
}