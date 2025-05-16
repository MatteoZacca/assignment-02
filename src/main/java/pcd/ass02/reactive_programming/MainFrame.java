package pcd.ass02.reactive_programming;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.sql.Time;

public class MainFrame extends JFrame {
    private final JButton btnSelect = new JButton("Select Directory");
    private final JButton btnStart  = new JButton("Start");
    private final JTextArea printArea  = new JTextArea(10, 50);
    private final JLabel lblClasses = new JLabel("Classes / Interfaces analyzed: ");
    private final JLabel lblDeps    = new JLabel("Dependencies found: ");

    private final GraphService graphService = new GraphService();
    private final DependencyAnalyzer analyzer = new DependencyAnalyzer(graphService);
    private Path selectedRoot;

    public MainFrame() {
        super("DependencyAnalyzer");
        initComponents();
        bindActions();
    }

    private void initComponents() {
        log("Sono entrato nel metodo initComponents");
        setLayout(new BorderLayout(0, 5));

        // Creation topPanel with 'Select Directory' button and 'Start button'
        JPanel topPanel = new JPanel();
        topPanel.add(btnSelect);
        topPanel.add(btnStart);
        btnStart.setEnabled(false);
        printArea.setEditable(false);

        // Creation dependenciesPanel with JLabel 'Classes / Interfaces analyzed' and JLabel
        // 'Dependencies found'
        JPanel dependenciesPanel = new JPanel(new GridLayout(1, 3, 0, 0));
        dependenciesPanel.add(lblClasses);
        dependenciesPanel.add(lblDeps);


        JScrollPane scrollPanel = new JScrollPane(printArea);
        JComponent smartGraphView = graphService.getViewPanel();

        // Splitting window in two parts: left side for log and right side for dependencies view
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollPanel,
                smartGraphView
        );
        splitPane.setResizeWeight(0.3);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(dependenciesPanel, BorderLayout.SOUTH);

        setSize(800,800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void bindActions() {
        btnSelect.addActionListener(this::onSelect);
        btnStart.addActionListener(this::onStart);
    }

    private void onSelect(ActionEvent event) {
        log("Ho attivato il Listener e sono entrato nel metodo onSelect");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedRoot = chooser.getSelectedFile().toPath();
            logArea("Selected Root: " + selectedRoot);
            btnStart.setEnabled(true);
        }
    }

    private void onStart(ActionEvent event) {
        log("Ho attivato il Listener e sono entrato nel metodo onStart");
        btnStart.setEnabled(false); // disabilito il pulsante di avvio per evitare che l'utente
        // possa cliccarlo nuovamente mentre il programma è in esecuzione
        logArea("Avvio analisi...");

        // Avvia l'analisi sulla directory selezionata sfruttando logica reattiva
        analyzer.processDirectory(selectedRoot);
        this.repaint();

        // Crea un timer che esegue un'azione ogni 200 ms
        new Timer(200, evt -> {
            lblClasses.setText("Classes / Interfaces analyzed: " + analyzer.getFilesProcessed());
            lblDeps.setText("Dependencies found: " + analyzer.getDependenciesFound());
        }).start();
    }

    /**
     * Aggiunge una riga di log all'area di testo
     */
    public void logArea(String msg) {
        SwingUtilities.invokeLater(() -> printArea.append(msg + "\n"));
    }

    private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + msg);
    }


    public static void main(String[] args) {

        // SwingUtilities.invokeLater(Runnable runnable): questo metodo è utilizzato per
        // assicurarsi che il codice passato come Runnable venga eseguito nel thread
        // dell'Event Dispatch Thread, che è responsabile della gestione dell'interfaccia
        // grafica in Swing. In Swing, tutte le operazioni che modificano l'interfaccia
        // utente devono essere eseguite nel thread EDT per evitare problemi di concorrenza
        SwingUtilities.invokeLater(() -> new MainFrame());

    }

}