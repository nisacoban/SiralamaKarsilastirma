package sorting;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Ana pencere: veri seti seç, algoritmaları çalıştır, kazananı gör. */
public class MainFrame extends JFrame {

    private final List<SortAlgorithm> allAlgorithms = SortAlgorithms.all();
    private final List<JCheckBox> algoBoxes = new ArrayList<>();

    private final JComboBox<DatasetType> datasetBox = new JComboBox<>(DatasetType.values());
    private final JComboBox<Integer> sizeBox =
            new JComboBox<>(new Integer[]{100, 1000, 5000, 10000, 20000, 50000, 100000, 200000});
    private final JSpinner repeatSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 15, 1));
    private final JButton runButton = new JButton("Karşılaştır");
    private final JButton runAllButton = new JButton("Tüm veri setlerini karşılaştır");
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel statusLabel = new JLabel("Hazır. Bir veri seti seçip \"Karşılaştır\"a basın.");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Sıra", "Algoritma", "Karmaşıklık", "Süre (ms)", "Karşılaştırma", "Yazma", "Doğru?", "Kazanana oranla"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final ChartPanel chartPanel = new ChartPanel();

    private DefaultTableModel matrixModel = new DefaultTableModel();
    private final JTable matrixTable = new JTable(matrixModel);
    private int matrixAlgoCount = 0;

    public MainFrame() {
        super("Sıralama Algoritmaları Performans Karşılaştırması");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildControlPanel(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tek veri seti sonucu", buildSingleTab());
        tabs.addTab("Tüm veri setleri (matris)", buildMatrixTab());
        add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.add(statusLabel, BorderLayout.CENTER);
        progressBar.setPreferredSize(new Dimension(200, 16));
        bottom.add(progressBar, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        setSize(1000, 720);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------ arayüz kurulumu
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Veri seti:"));
        row1.add(datasetBox);
        row1.add(new JLabel("Eleman sayısı:"));
        sizeBox.setSelectedItem(10000);
        row1.add(sizeBox);
        row1.add(new JLabel("Tekrar:"));
        row1.add(repeatSpinner);
        row1.add(runButton);
        row1.add(runAllButton);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Algoritmalar:"));
        for (SortAlgorithm alg : allAlgorithms) {
            JCheckBox cb = new JCheckBox(alg.name(), true);
            algoBoxes.add(cb);
            row2.add(cb);
        }

        panel.add(row1);
        panel.add(row2);
        panel.setBorder(BorderFactory.createTitledBorder("Ayarlar"));

        runButton.addActionListener(e -> runSingle());
        runAllButton.addActionListener(e -> runAll());
        return panel;
    }

    private JComponent buildSingleTab() {
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : new int[]{0, 2, 6, 7}) table.getColumnModel().getColumn(c).setCellRenderer(center);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{3, 4, 5}) table.getColumnModel().getColumn(c).setCellRenderer(right);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);

        // En iyi satırı yeşil, en kötüyü kırmızımsı göster
        DefaultTableCellRenderer highlight = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    if (row == 0) c.setBackground(new Color(214, 245, 220));
                    else if (row == t.getRowCount() - 1 && t.getRowCount() > 1) c.setBackground(new Color(252, 222, 224));
                    else c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        highlight.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(highlight);

        JCheckBox logBox = new JCheckBox("Logaritmik ölçek (çubuklar çok farklı uzunluktaysa)");
        logBox.addActionListener(e -> chartPanel.setLogScale(logBox.isSelected()));

        JPanel chartWrap = new JPanel(new BorderLayout());
        chartWrap.add(chartPanel, BorderLayout.CENTER);
        chartWrap.add(logBox, BorderLayout.SOUTH);
        chartWrap.setBorder(BorderFactory.createTitledBorder("Süre grafiği (kısa çubuk = hızlı)"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), chartWrap);
        split.setResizeWeight(0.42);
        return split;
    }

    private JComponent buildMatrixTab() {
        matrixTable.setRowHeight(24);
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.add(new JLabel(" Süreler milisaniye cinsindendir. Yeşil hücre o veri setindeki en hızlı algoritmayı gösterir."),
                BorderLayout.NORTH);
        p.add(new JScrollPane(matrixTable), BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------ yardımcılar
    private List<SortAlgorithm> selectedAlgorithms() {
        List<SortAlgorithm> list = new ArrayList<>();
        for (int i = 0; i < allAlgorithms.size(); i++) {
            if (algoBoxes.get(i).isSelected()) list.add(allAlgorithms.get(i));
        }
        return list;
    }

    private boolean confirmSlow(List<SortAlgorithm> algs, int size) {
        if (size < 50000) return true;
        for (SortAlgorithm a : algs) {
            if (a.isSlowOnLargeData()) {
                int ans = JOptionPane.showConfirmDialog(this,
                        "Eleman sayısı " + NumberFormat.getInstance().format(size) + " iken O(n²) algoritmalar\n"
                                + "(Bubble/Selection/Insertion) birkaç saniye ile dakikalar arası sürebilir.\n"
                                + "Yine de devam edilsin mi?",
                        "Uyarı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                return ans == JOptionPane.YES_OPTION;
            }
        }
        return true;
    }

    private void setBusy(boolean busy) {
        runButton.setEnabled(!busy);
        runAllButton.setEnabled(!busy);
        progressBar.setIndeterminate(busy);
    }

    // ------------------------------------------------------------ tek veri seti
    private void runSingle() {
        List<SortAlgorithm> algs = selectedAlgorithms();
        if (algs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "En az bir algoritma seçin.");
            return;
        }
        DatasetType type = (DatasetType) datasetBox.getSelectedItem();
        int size = (Integer) sizeBox.getSelectedItem();
        int repeats = (Integer) repeatSpinner.getValue();
        if (!confirmSlow(algs, size)) return;

        setBusy(true);
        new SwingWorker<List<Benchmark.Result>, String>() {
            @Override
            protected List<Benchmark.Result> doInBackground() {
                return Benchmark.run(type, size, repeats, algs, msg -> publish(msg));
            }

            @Override
            protected void process(List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    showSingleResult(get(), type, size);
                } catch (Exception ex) {
                    statusLabel.setText("Hata: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showSingleResult(List<Benchmark.Result> results, DatasetType type, int size) {
        results.sort(Comparator.comparingDouble(r -> r.ms));
        NumberFormat nf = NumberFormat.getInstance();
        tableModel.setRowCount(0);
        double bestMs = Math.max(results.get(0).ms, 0.0005);
        for (int i = 0; i < results.size(); i++) {
            Benchmark.Result r = results.get(i);
            boolean hasCounts = r.comparisons > 0 || r.writes > 0;
            tableModel.addRow(new Object[]{
                    i + 1, r.name, r.complexity,
                    String.format("%.3f", r.ms),
                    hasCounts ? nf.format(r.comparisons) : "-",
                    hasCounts ? nf.format(r.writes) : "-",
                    r.correct ? "Evet" : "HAYIR",
                    String.format("x%.1f", Math.max(r.ms, 0.0005) / bestMs)
            });
        }
        chartPanel.setData(results);

        Benchmark.Result best = results.get(0);
        Benchmark.Result worst = results.get(results.size() - 1);
        StringBuilder sb = new StringBuilder();
        sb.append("KAZANAN: ").append(best.name).append(" (").append(String.format("%.3f", best.ms)).append(" ms)");
        if (results.size() > 1) {
            sb.append("  |  En yavaş: ").append(worst.name).append(" (")
                    .append(String.format("%.3f", worst.ms)).append(" ms)");
        }
        sb.append("  |  Veri seti: ").append(type.shortName()).append(", n=").append(nf.format(size));
        statusLabel.setText(sb.toString());
    }

    // ------------------------------------------------------------ tüm veri setleri
    private void runAll() {
        List<SortAlgorithm> algs = selectedAlgorithms();
        if (algs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "En az bir algoritma seçin.");
            return;
        }
        int size = (Integer) sizeBox.getSelectedItem();
        int repeats = (Integer) repeatSpinner.getValue();
        if (!confirmSlow(algs, size)) return;

        setBusy(true);
        new SwingWorker<Map<DatasetType, List<Benchmark.Result>>, String>() {
            @Override
            protected Map<DatasetType, List<Benchmark.Result>> doInBackground() {
                Map<DatasetType, List<Benchmark.Result>> all = new EnumMap<>(DatasetType.class);
                for (DatasetType t : DatasetType.values()) {
                    all.put(t, Benchmark.run(t, size, repeats, algs, msg -> publish(msg)));
                }
                return all;
            }

            @Override
            protected void process(List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    showMatrix(get(), algs, size);
                } catch (Exception ex) {
                    statusLabel.setText("Hata: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showMatrix(Map<DatasetType, List<Benchmark.Result>> all, List<SortAlgorithm> algs, int size) {
        DatasetType[] types = DatasetType.values();
        String[] cols = new String[types.length + 1];
        cols[0] = "Algoritma";
        for (int i = 0; i < types.length; i++) cols[i + 1] = types[i].shortName();

        matrixModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        matrixAlgoCount = algs.size();
        double[] minPerCol = new double[types.length];
        String[] winnerPerCol = new String[types.length];
        for (int c = 0; c < types.length; c++) {
            minPerCol[c] = Double.MAX_VALUE;
            for (Benchmark.Result r : all.get(types[c])) {
                if (r.ms < minPerCol[c]) {
                    minPerCol[c] = r.ms;
                    winnerPerCol[c] = r.name;
                }
            }
        }
        for (SortAlgorithm alg : algs) {
            Object[] row = new Object[types.length + 1];
            row[0] = alg.name();
            for (int c = 0; c < types.length; c++) {
                for (Benchmark.Result r : all.get(types[c])) {
                    if (r.name.equals(alg.name())) row[c + 1] = Math.round(r.ms * 1000) / 1000.0;
                }
            }
            matrixModel.addRow(row);
        }
        Object[] winnerRow = new Object[types.length + 1];
        winnerRow[0] = "KAZANAN";
        for (int c = 0; c < types.length; c++) winnerRow[c + 1] = winnerPerCol[c];
        matrixModel.addRow(winnerRow);

        matrixTable.setModel(matrixModel);
        final double[] mins = minPerCol;
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
                c.setFont(t.getFont());
                if (row == matrixAlgoCount) {
                    c.setBackground(new Color(235, 235, 235));
                    c.setFont(t.getFont().deriveFont(Font.BOLD));
                } else if (col > 0 && v instanceof Double && Math.abs((Double) v - Math.round(mins[col - 1] * 1000) / 1000.0) < 1e-9) {
                    c.setBackground(new Color(190, 235, 200));
                    c.setFont(t.getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        };
        for (int c = 0; c < matrixTable.getColumnCount(); c++) {
            matrixTable.getColumnModel().getColumn(c).setCellRenderer(renderer);
        }
        matrixTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        statusLabel.setText("Tüm veri setleri tamamlandı (n=" + NumberFormat.getInstance().format(size)
                + "). \"Tüm veri setleri (matris)\" sekmesine bakın.");
    }
}
