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
        smartPanel.init();
        Scene scene = new Scene(smartPanel, 600, 400);
        fxPanel.setScene(scene);
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

        graph.insertVertex(source);
        graph.insertVertex(target);
        graph.insertEdge(source, target, source + "->" + target);
    }
}