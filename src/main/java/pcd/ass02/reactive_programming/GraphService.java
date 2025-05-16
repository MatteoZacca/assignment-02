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
    private final JFXPanel fxPanel; // componente Swing che "ospita" una scena JavaFX.
    // Funge da ponte tra Swing e JavaFX
    private SmartGraphPanel<String,String> smartPanel; // pannello JavaFX che disegna
    // e gestisce il layout del grafo

    public GraphService() {
        // Debugging log
        log("Sono entrato nel costruttore di GraphService");

        // Inizializza il container JavaFX all'interno di Swing
        fxPanel = new JFXPanel(); //
        graph = new DigraphEdgeList<>(); // grafo vuoto

        // Avvia la piattaforma JavaFX: tutte le operazioni che manipolano la scena
        // JavaFX devono eseguirsi sul JavaFX Application Thread. Con runlater scheduli
        // initFX() su quel thread (evitando di bloccare l'EDT di Swing)
        Platform.runLater(this::initFX);

    }

    /**
     * Restituisce il pannello Swing che ospita la visualizzazione JavaFX.
     */
    public JComponent getViewPanel() {
        // Debugging log
        log("Mi occupo io del metodo getViewPanel");
        return fxPanel;
    }

    /**
     * Aggiunge una dipendenza al grafo e aggiorna la vista. Utilizza l'oggetto Dependency
     * che contiene source e target
     */
    public void addDependency(Dependency dep) {
        // Debugging log
        log("Sono entrato nel metodo addDependency");
        String source = dep.getSource();
        String target = dep.getTarget();
        String edgeLabel = source + "->" + target;

        Platform.runLater(() -> {
            // Debugging log
            log("Ho chiamato Platform.runLater all'interno del metodo addDependency");
            // 1) verifica se il vertice esiste già, in caso contrario l ocrea
            if (isNodeAbsent(source)) {
                graph.insertVertex(source);
            }
            if (isNodeAbsent(target)) {
                graph.insertVertex(target);
            }

            // 2) verifica se l'arco esiste già, in caso contrario lo crea
            if (isEdgeAbsent(edgeLabel)) {
                graph.insertEdge(source, target, edgeLabel);
            }
            smartPanel.updateAndWait();
        });
    }

    // Inizializza la scena JavaFX con SmartGraphPanel
    private void initFX() {
        log("Mi occupo io del metodo initFX!");
        smartPanel = new SmartGraphPanel<>(graph);
        smartPanel.setAutomaticLayout(true); // abilita l'algoritmo di posizionamento
        // automatico dei nodi
        Scene scene = new Scene(smartPanel, 600, 400);
        fxPanel.setScene(scene);
        smartPanel.init();
        smartPanel.updateAndWait();
    }

    private boolean isEdgeAbsent(String edgeLabel) {
        return graph.edges().stream()
                .map(Edge::element)
                .noneMatch(el -> el.equals(edgeLabel));
    }

    private boolean isNodeAbsent(String label) {
        return graph.vertices().stream()
                .map(Vertex::element)
                .noneMatch(e -> e.equals(label));
    }

    private static void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "]:" + msg);
    }
}