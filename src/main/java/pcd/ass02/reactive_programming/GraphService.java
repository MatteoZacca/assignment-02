package pcd.ass02.reactive_programming;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;

/**
 * Service per la visualizzazione del grafo usando SmartGraph (JavaFX) integrato in Swing.
 */
public class GraphService {

    private final Graph<String, String> graph;
    private final JFXPanel fxPanel;
    private SmartGraphPanel<String,String> smartPanel;

    public GraphService() {
        // Inizializza il container JavaFX all'interno di Swing
        fxPanel = new JFXPanel();
        graph = new DigraphEdgeList<>();

        // Avvia la piattaforma JavaFX
        Platform.runLater(() -> initFX());
    }

    // Inizializza la scena JavaFX con SmartGraphPanel
    private void initFX() {
        smartPanel = new SmartGraphPanel<>(graph);
        Scene scene = new Scene(smartPanel, 600, 400);
        fxPanel.setScene(scene);
        smartPanel.init();
    }

    /**
     * Restituisce il pannello Swing che ospita la visualizzazione JavaFX.
     */
    public JComponent getViewPanel() {
        return fxPanel;
    }

    /**
     * Aggiunge una dipendenza al grafo e aggiorna la vista. Utilizza l'oggetto Dependency
     * che contiene source e target
     */
    public void addDependency(Dependency dep) {
        String source = dep.getSource();
        String target = dep.getTarget();
        String edgeLabel = source + "->" + target;

        Platform.runLater(() -> {
            // 1) verifica/crea i vertici (come prima)
            boolean hasSource = graph.vertices().stream()
                    .map(v -> v.element())
                    .anyMatch(e -> e.equals(source));
            if (!hasSource) {
                graph.insertVertex(source);
            }

            boolean hasTarget = graph.vertices().stream()
                    .map(v -> v.element())
                    .anyMatch(e -> e.equals(target));
            if (!hasTarget) {
                graph.insertVertex(target);
            }

            // 2) VERIFICA SE L’ARCO ESISTE GIÀ
            boolean hasEdge = graph.edges().stream()
                    .map(e -> e.element())
                    .anyMatch(el -> el.equals(edgeLabel));
            if (!hasEdge) {
                graph.insertEdge(source, target, edgeLabel);
            }
        });
    }
}