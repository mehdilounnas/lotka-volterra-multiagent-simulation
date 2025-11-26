import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LotkaVolterraComparator extends JFrame {

    private double alpha = 0.1;
    private double beta = 0.02;
    private double gamma = 0.1;
    private double delta = 0.01;

    private double X0 = 20.0;
    private double Y0 = 5.0;

    private double dt = 0.1;
    private int steps = 2000;

    private List<Double> preyPopulation;
    private List<Double> predatorPopulation;

    public LotkaVolterraComparator() {
        setTitle("Modele de Lotka-Volterra - Comparaison Theorique");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        simulate();

        ChartPanel chartPanel = new ChartPanel();
        add(chartPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void simulate() {
        preyPopulation = new ArrayList<>();
        predatorPopulation = new ArrayList<>();

        double X = X0;
        double Y = Y0;

        for (int i = 0; i < steps; i++) {
            preyPopulation.add(X);
            predatorPopulation.add(Y);

            double dX = (alpha * X - beta * X * Y) * dt;
            double dY = (delta * X * Y - gamma * Y) * dt;

            X += dX;
            Y += dY;

            X = Math.max(0, X);
            Y = Math.max(0, Y);
        }
    }

    class ChartPanel extends JPanel {
        public ChartPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 60;

            double maxPrey = preyPopulation.stream().max(Double::compareTo).orElse(1.0);
            double maxPred = predatorPopulation.stream().max(Double::compareTo).orElse(1.0);
            double maxPop = Math.max(maxPrey, maxPred);

            g2d.setColor(Color.BLACK);
            g2d.drawLine(padding, padding, padding, height - padding);
            g2d.drawLine(padding, height - padding, width - padding, height - padding);

            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Population", 10, 30);
            g2d.drawString("Temps (iterations)", width / 2 - 60, height - 10);

            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 5; i++) {
                int y = height - padding - (i * (height - 2 * padding) / 5);
                double value = (maxPop * i) / 5;
                g2d.drawString(String.format("%.1f", value), 10, y + 5);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(padding, y, width - padding, y);
                g2d.setColor(Color.BLACK);
            }

            double xScale = (double) (width - 2 * padding) / steps;
            double yScale = (double) (height - 2 * padding) / maxPop;

            g2d.setColor(new Color(34, 139, 34));
            g2d.setStroke(new BasicStroke(2));
            for (int i = 1; i < preyPopulation.size(); i++) {
                int x1 = padding + (int) ((i - 1) * xScale);
                int y1 = height - padding - (int) (preyPopulation.get(i - 1) * yScale);
                int x2 = padding + (int) (i * xScale);
                int y2 = height - padding - (int) (preyPopulation.get(i) * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }

            g2d.setColor(new Color(220, 20, 60));
            for (int i = 1; i < predatorPopulation.size(); i++) {
                int x1 = padding + (int) ((i - 1) * xScale);
                int y1 = height - padding - (int) (predatorPopulation.get(i - 1) * yScale);
                int x2 = padding + (int) (i * xScale);
                int y2 = height - padding - (int) (predatorPopulation.get(i) * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }

            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(width - 180, 30, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Proies (X)", width - 150, 45);

            g2d.setColor(new Color(220, 20, 60));
            g2d.fillRect(width - 180, 60, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Predateurs (Y)", width - 150, 75);

            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            g2d.drawString(String.format("Î± = %.2f (naissance proies)", alpha), width - 250, 110);
            g2d.drawString(String.format("Î² = %.3f (predation)", beta), width - 250, 125);
            g2d.drawString(String.format("Î³ = %.2f (mort predateurs)", gamma), width - 250, 140);
            g2d.drawString(String.format("Î´ = %.3f (efficacite)", delta), width - 250, 155);
            g2d.drawString(String.format("Xa‚€ = %.0f, Ya‚€ = %.0f", X0, Y0), width - 250, 175);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LotkaVolterraComparator());
    }
}
