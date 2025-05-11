package pcd.ass02.reactive_programming;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public class MainFrame extends JFrame {
    private final JButton btnSelect = new JButton("Seleziona Cartella");
    private final JButton btnStart  = new JButton("Start");
    private final JTextArea logArea  = new JTextArea(10, 50);
    private final JLabel lblClasses = new JLabel("Classi/Interfacce analizzate: 0");
    private final JLabel lblDeps    = new JLabel("Dipendenze trovate: 0");
    private final JProgressBar progressBar = new JProgressBar();

    private final DependencyAnalyzer analyzer = new DependencyAnalyzer();
    private final GraphService graphService = new GraphService();
    private Path selectedRoot;

    public MainFrame() {
        super("DependencyAnalyzer");
        initComponents();
        bindActions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5 ));
        JPanel topPanel = new JPanel();
        topPanel.add(btnSelect);
        topPanel.add(btnStart);
        btnStart.setEnabled(false);

        logArea.setEditable(false);
        logArea.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane scrollPanel = new JScrollPane(logArea);
        // SmartGraph view:
        JComponent graphView = graphService.getViewPanel();

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.add(lblClasses);
        statsPanel.add(lblDeps);
        statsPanel.add(progressBar);


        // Dividi la finestra in log e grafo
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollPanel,
                graphView
        );
        splitPane.setResizeWeight(0.3);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void bindActions() {
        btnSelect.addActionListener(this::onSelect);
        btnStart.addActionListener(this::onStart);
    }

    private void onSelect(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedRoot = chooser.getSelectedFile().toPath();
            log("Cartella selezionata: " + selectedRoot);
            btnStart.setEnabled(true);
        }
    }

    private void onStart(ActionEvent event) {
        btnStart.setEnabled(false);
        progressBar.setIndeterminate(true);
        log("Avvio analisi...");

        // Avvia l'analisi sulla directory selezionata
        // Avvia il flusso reattivo
            analyzer.processDirectory(selectedRoot);

        // Esempio di polling: aggiorna stats periodicamente
        new Timer(200, evt -> {
            lblClasses.setText("Classi/Interfacce analizzate: " + analyzer.getFilesProcessed());
            lblDeps.setText("Dipendenze trovate: " + analyzer.getDependenciesFound());
            if (!progressBar.isIndeterminate()) ((Timer)evt.getSource()).stop();
        }).start();
    }

    /**
     * Aggiunge una riga di log all'area di testo
     */
    public void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }


    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

}