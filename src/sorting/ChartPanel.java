package sorting;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Sonuçları yatay çubuk grafik olarak çizen panel. */
public class ChartPanel extends JPanel {
    private List<Benchmark.Result> data = new ArrayList<>();
    private boolean logScale = false;

    public ChartPanel() {
        setPreferredSize(new Dimension(700, 280));
    }

    public void setData(List<Benchmark.Result> sortedResults) {
        this.data = sortedResults;
        repaint();
    }

    public void setLogScale(boolean log) {
        this.logScale = log;
        repaint();
    }

    private double scale(double ms) {
        return logScale ? Math.log10(ms * 1000 + 1) : ms;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (data.isEmpty()) {
            g.setColor(getForeground());
            g.drawString("Henüz sonuç yok. Bir veri seti seçip \"Karşılaştır\" düğmesine basın.", 20, 30);
            return;
        }

        int labelW = 170, valueW = 120, top = 10;
        int rowH = Math.max(22, Math.min(36, (getHeight() - 20) / data.size()));
        int barMax = Math.max(50, getWidth() - labelW - valueW - 20);

        double max = 0;
        for (Benchmark.Result r : data) max = Math.max(max, scale(r.ms));
        if (max <= 0) max = 1;

        Color best = new Color(46, 160, 67);
        Color worst = new Color(215, 58, 73);
        Color normal = new Color(56, 132, 224);

        for (int i = 0; i < data.size(); i++) {
            Benchmark.Result r = data.get(i);
            int y = top + i * rowH;
            int barH = rowH - 8;

            g.setColor(getForeground());
            g.drawString((i + 1) + ". " + r.name, 10, y + barH - 3);

            int w = (int) Math.max(3, barMax * scale(r.ms) / max);
            Color c = (i == 0) ? best : (i == data.size() - 1 && data.size() > 1 ? worst : normal);
            g.setColor(c);
            g.fillRoundRect(labelW, y, w, barH, 8, 8);

            g.setColor(getForeground());
            g.drawString(String.format("%.3f ms", r.ms), labelW + w + 8, y + barH - 3);
        }
        if (logScale) {
            g.setColor(Color.GRAY);
            g.drawString("(logaritmik ölçek)", getWidth() - 130, getHeight() - 8);
        }
    }
}
