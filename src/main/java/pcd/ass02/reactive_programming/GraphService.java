package pcd.ass02.reactive_programming;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;

import static javax.swing.SwingUtilities.invokeLater;

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
        Platform.runLater(this::initFX);
    }

    // Inizializza la scena JavaFX con SmartGraphPanel
    private void initFX() {
        smartPanel = new SmartGraphPanel<>(graph);
        smartPanel.setAutomaticLayout(true);
        Scene scene = new Scene(smartPanel, 600, 400);
        fxPanel.setScene(scene);
        smartPanel.init();
        smartPanel.updateAndWait();
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

        invokeLater(() -> {
            // 1) verifica/crea i vertici (come prima)
            if (isNodeAbsent(source)) {
                // System.out.println("Adding " + source);
                graph.insertVertex(source);
            }
            if (isNodeAbsent(target)) {
                // System.out.println("Adding " + target);
                graph.insertVertex(target);
            }
            // 2) VERIFICA SE L’ARCO ESISTE GIÀ
            if (isEdgeAbsent(edgeLabel)) {
                graph.insertEdge(source, target, edgeLabel);
            }
            smartPanel.updateAndWait();
        });
    }

    private boolean isEdgeAbsent(String edgeLabel) {
        return graph.edges().stream()
                .map(Edge::element)
                .noneMatch(el -> el.equals(edgeLabel));
    }

    private boolean isNodeAbsent(String source) {
        return graph.vertices().stream()
                .map(Vertex::element)
                .noneMatch(e -> e.equals(source));
    }
}