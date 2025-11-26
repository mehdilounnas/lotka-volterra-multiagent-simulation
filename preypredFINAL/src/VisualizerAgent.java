
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class VisualizerAgent extends Agent {
    private SimulationPanel panel;
    private JFrame frame;
    private Environment environment;
    private PopulationChart chart;
    private TheoreticalChart theoChart;
    private ControlPanel controlPanel;
    private ParameterPanel parameterPanel;
    private boolean isRunning = false;

    // Shared simulation parameters
    public static class SimParams {
        // Prey parameters
        public static int PREY_ENERGY_START = 85;
        public static int PREY_ENERGY_MAX = 120;
        public static int PREY_REPRO_THRESHOLD = 75;
        public static int PREY_REPRO_COST = 40;
        public static double PREY_SPEED = 2.1;

        // Predator parameters
        public static int PRED_ENERGY_START = 200;
        public static int PRED_ENERGY_MAX = 300;
        public static int PRED_ENERGY_GAIN = 70;
        public static int PRED_REPRO_THRESHOLD = 160;
        public static int PRED_REPRO_COST = 50;
        public static double PRED_SPEED = 3.5;

        // Food parameters
        public static int FOOD_ENERGY_VALUE = 35;
        public static int FOOD_SPAWN_RATE = 10;
        public static int FOOD_PER_SPAWN = 2;
    }

    protected void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        environment = Environment.getInstance();

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Simulation Proie-Predateur");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(0, 0));
            frame.getContentPane().setBackground(new Color(240, 242, 245)); // Soft gray bg

            // Top control panel - sleek and minimal
            controlPanel = new ControlPanel();

            JPanel topContainer = new JPanel(new BorderLayout());
            topContainer.setBackground(Color.WHITE);
            topContainer.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
            topContainer.add(controlPanel, BorderLayout.CENTER);
            frame.add(topContainer, BorderLayout.NORTH);

            // Center simulation on left, parameters on right
            JPanel centerContainer = new JPanel(new BorderLayout(20, 0)); // More spacing
            centerContainer.setBackground(new Color(240, 242, 245));
            centerContainer.setBorder(new EmptyBorder(20, 20, 20, 20)); // Outer padding

            // Simulation panel with nice border
            panel = new SimulationPanel();
            JPanel simWrapper = new JPanel(new BorderLayout());
            simWrapper.setBackground(new Color(240, 242, 245));
            // Card effect for simulation panel
            simWrapper.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220), 1, true),
                    new EmptyBorder(0, 0, 0, 0)
            ));
            simWrapper.add(panel, BorderLayout.CENTER);
            centerContainer.add(simWrapper, BorderLayout.CENTER);

            // Parameters panel
            parameterPanel = new ParameterPanel();
            JScrollPane paramScroll = new JScrollPane(parameterPanel);
            paramScroll.setPreferredSize(new Dimension(320, 600)); // Slightly wider
            paramScroll.setBorder(null); // Remove default scroll border
            paramScroll.getViewport().setBackground(new Color(240, 242, 245)); // Match bg
            paramScroll.getVerticalScrollBar().setUnitIncrement(16);
            centerContainer.add(paramScroll, BorderLayout.EAST);

            frame.add(centerContainer, BorderLayout.CENTER);

            // Chart at bottom with padding
            JPanel chartWrapper = new JPanel(new BorderLayout());
            chartWrapper.setBackground(new Color(240, 242, 245));
            chartWrapper.setBorder(new EmptyBorder(0, 20, 20, 20)); // Match side padding


            // Add shadow/border to chart





            chart = new PopulationChart();
            theoChart = new TheoreticalChart();

            JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 15, 0)); // 2 columns, 15px gap
            chartsContainer.setBackground(new Color(240, 242, 245));

            // Real Chart Wrapper
            JPanel realChartCard = new JPanel(new BorderLayout());
            realChartCard.add(chart);
            realChartCard.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));


            chartsContainer.add(realChartCard);
            chartsContainer.add(theoChart);

            chartWrapper.add(chartsContainer, BorderLayout.CENTER);

            frame.add(chartWrapper, BorderLayout.SOUTH);

            frame.setSize(1350, 950);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        addBehaviour(new TickerBehaviour(this, 100) {
            private int tickCount = 0;
            protected void onTick() {
                if (!isRunning) return;

                tickCount++;
                // environment.update(); // Removed: Logic is handled by individual agents

                if (tickCount % SimParams.FOOD_SPAWN_RATE == 0) {
                    for (int i = 0; i < SimParams.FOOD_PER_SPAWN; i++) {
                        double x = 50 + Math.random() * (environment.getWidth() - 100);
                        double y = 50 + Math.random() * (environment.getHeight() - 100);
                        environment.spawnFood(new Position(x, y));
                    }
                }

                if (panel != null) panel.repaint();
                if (chart != null) chart.updateData(environment.getPreyCount(), environment.getPredatorCount());
                if (theoChart != null) theoChart.nextStep();
                if (parameterPanel != null) {
                    SwingUtilities.invokeLater(() ->
                            parameterPanel.updateLiveStats(environment.getPreyCount(), environment.getPredatorCount(), environment.getFoodCount())
                    );
                }
            }
        });

        System.out.println("Visualizer Agent started");
    }

    private void startSimulation() { isRunning = true; System.out.println("Simulation started"); }
    private void stopSimulation() { isRunning = false; System.out.println("Simulation paused"); }

    private void spawnAgent(String className, String prefix) {
        try {
            Object[] args = new Object[]{
                    Math.random() * environment.getWidth(),
                    Math.random() * environment.getHeight()
            };
            String name = prefix + System.nanoTime();
            getContainerController().createNewAgent(name, className, args).start();
        } catch (Exception ex) {
            System.err.println("Error spawning agent: " + ex.getMessage());
        }
    }

    // ==========================================
    // MODERN PARAMETER PANEL (CARD LAYOUT)
    // ==========================================
    class ParameterPanel extends JPanel {
        private Map<String, JSpinner> preySpinners = new HashMap<>();
        private Map<String, JSpinner> predSpinners = new HashMap<>();
        private Map<String, JSpinner> foodSpinners = new HashMap<>();

        // Live stat labels
        private JLabel livePreyLabel, livePredatorLabel, liveFoodLabel;

        public ParameterPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(240, 242, 245));
            setBorder(new EmptyBorder(0, 0, 0, 5)); // Right padding

            // Title
            JLabel title = new JLabel("Parametres");
            title.setFont(new Font("Segoe UI", Font.BOLD, 20));
            title.setForeground(new Color(40, 40, 40));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            title.setBorder(new EmptyBorder(0, 5, 15, 0));
            add(title);

            // LIVE STATS CARD (NEW!)
            add(createLiveStatsCard());
            add(Box.createVerticalStrut(15));

            // Prey Section Card
            add(createSectionCard("Proies", new Color(34, 139, 34), "prey"));
            add(Box.createVerticalStrut(15));

            // Predator Section Card
            add(createSectionCard("Predateurs", new Color(220, 20, 60), "pred"));
            add(Box.createVerticalStrut(15));

            // Food Section Card
            add(createSectionCard("Nourriture", new Color(255, 165, 0), "food"));
            add(Box.createVerticalStrut(20));

            // Apply button
            JButton applyBtn = new JButton("Appliquer les changements");
            applyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            applyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            applyBtn.setBackground(new Color(0, 123, 255));
            applyBtn.setForeground(Color.WHITE);
            applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            applyBtn.setFocusPainted(false);
            applyBtn.setBorderPainted(false);
            applyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            applyBtn.addActionListener(e -> applyParameters());

            add(applyBtn);
            add(Box.createVerticalGlue());
        }

        public void updateLiveStats(int prey, int pred, int food) {
            livePreyLabel.setText(String.valueOf(prey));
            livePredatorLabel.setText(String.valueOf(pred));
            liveFoodLabel.setText(String.valueOf(food));
        }

        private JPanel createLiveStatsCard() {
            JPanel card = new JPanel();
            card.setLayout(new GridLayout(1, 3, 10, 0)); // Grid for 3 stats
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(230, 230, 230), 1, true),
                    new EmptyBorder(15, 10, 15, 10)
            ));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            livePreyLabel = createStatItem(card, "Proies", new Color(34, 139, 34));
            livePredatorLabel = createStatItem(card, "Pred.", new Color(220, 20, 60));
            liveFoodLabel = createStatItem(card, "Nourr.", new Color(255, 165, 0));

            return card;
        }

        private JLabel createStatItem(JPanel parent, String title, Color color) {
            JPanel item = new JPanel(new BorderLayout());
            item.setBackground(Color.WHITE);

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLbl.setForeground(Color.GRAY);
            item.add(titleLbl, BorderLayout.NORTH);

            JLabel valueLbl = new JLabel("0", SwingConstants.CENTER);
            valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Large font
            valueLbl.setForeground(color);
            item.add(valueLbl, BorderLayout.CENTER);

            parent.add(item);
            return valueLbl;
        }

        private JPanel createSectionCard(String title, Color accentColor, String type) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(230, 230, 230), 1, true),
                    new EmptyBorder(15, 15, 15, 15)
            ));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, type.equals("food") ? 180 : 300));

            // Header
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            header.setBackground(Color.WHITE);
            header.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Accent bar
            JPanel bar = new JPanel();
            bar.setPreferredSize(new Dimension(4, 16));
            bar.setBackground(accentColor);
            header.add(bar);

            JLabel label = new JLabel("  " + title);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(new Color(50, 50, 50));
            header.add(label);

            card.add(header);
            card.add(Box.createVerticalStrut(15));

            // Content
            if (type.equals("prey")) {
                addPreyParameter(card, "Energie initiale", SimParams.PREY_ENERGY_START, 10, 200, 5);
                addPreyParameter(card, "Energie max", SimParams.PREY_ENERGY_MAX, 50, 300, 10);
                addPreyParameter(card, "Seuil reprod.", SimParams.PREY_REPRO_THRESHOLD, 30, 150, 5);
                addPreyParameter(card, "Cout reprod.", SimParams.PREY_REPRO_COST, 10, 100, 5);
                addPreyParameter(card, "Vitesse (x10)", (int)(SimParams.PREY_SPEED * 10), 5, 50, 1);
            } else if (type.equals("pred")) {
                addPredParameter(card, "Energie initiale", SimParams.PRED_ENERGY_START, 50, 400, 10);
                addPredParameter(card, "Energie max", SimParams.PRED_ENERGY_MAX, 100, 500, 10);
                addPredParameter(card, "Gain capture", SimParams.PRED_ENERGY_GAIN, 20, 150, 5);
                addPredParameter(card, "Seuil reprod.", SimParams.PRED_REPRO_THRESHOLD, 50, 300, 10);
                addPredParameter(card, "Coutt reprod.", SimParams.PRED_REPRO_COST, 20, 150, 5);
                addPredParameter(card, "Vitesse (x10)", (int)(SimParams.PRED_SPEED * 10), 10, 60, 1);
            } else if (type.equals("food")) {
                addFoodParameter(card, "Valeur energ.", SimParams.FOOD_ENERGY_VALUE, 10, 100, 5);
                addFoodParameter(card, "Taux spawn", SimParams.FOOD_SPAWN_RATE, 1, 50, 1);
                addFoodParameter(card, "Qte par spawn", SimParams.FOOD_PER_SPAWN, 1, 10, 1);
            }
            return card;
        }

        private void addPreyParameter(JPanel panel, String label, int value, int min, int max, int step) {
            JSpinner spinner = createSpinner(value, min, max, step);
            preySpinners.put(label, spinner);
            panel.add(createParameterRow(label, spinner));
            panel.add(Box.createVerticalStrut(8));
        }

        private void addPredParameter(JPanel panel, String label, int value, int min, int max, int step) {
            JSpinner spinner = createSpinner(value, min, max, step);
            predSpinners.put(label, spinner);
            panel.add(createParameterRow(label, spinner));
            panel.add(Box.createVerticalStrut(8));
        }

        private void addFoodParameter(JPanel panel, String label, int value, int min, int max, int step) {
            JSpinner spinner = createSpinner(value, min, max, step);
            foodSpinners.put(label, spinner);
            panel.add(createParameterRow(label, spinner));
            panel.add(Box.createVerticalStrut(8));
        }

        private JSpinner createSpinner(int value, int min, int max, int step) {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
            return spinner;
        }

        private JPanel createParameterRow(String label, JSpinner spinner) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(new Color(80, 80, 80));
            row.add(lbl, BorderLayout.CENTER);

            spinner.setPreferredSize(new Dimension(70, 28));
            row.add(spinner, BorderLayout.EAST);
            return row;
        }

        private void applyParameters() {
            try {
                // Prey parameters
                SimParams.PREY_ENERGY_START = (Integer) preySpinners.get("A‰nergie initiale").getValue();
                SimParams.PREY_ENERGY_MAX = (Integer) preySpinners.get("A‰nergie max").getValue();
                SimParams.PREY_REPRO_THRESHOLD = (Integer) preySpinners.get("Seuil reprod.").getValue();
                SimParams.PREY_REPRO_COST = (Integer) preySpinners.get("CoA»t reprod.").getValue();
                SimParams.PREY_SPEED = (Integer) preySpinners.get("Vitesse (x10)").getValue() / 10.0;

                // Predator parameters
                SimParams.PRED_ENERGY_START = (Integer) predSpinners.get("A‰nergie initiale").getValue();
                SimParams.PRED_ENERGY_MAX = (Integer) predSpinners.get("A‰nergie max").getValue();
                SimParams.PRED_ENERGY_GAIN = (Integer) predSpinners.get("Gain capture").getValue();
                SimParams.PRED_REPRO_THRESHOLD = (Integer) predSpinners.get("Seuil reprod.").getValue();
                SimParams.PRED_REPRO_COST = (Integer) predSpinners.get("CoA»t reprod.").getValue();
                SimParams.PRED_SPEED = (Integer) predSpinners.get("Vitesse (x10)").getValue() / 10.0;

                // Food parameters
                SimParams.FOOD_ENERGY_VALUE = (Integer) foodSpinners.get("Valeur energ.").getValue();
                SimParams.FOOD_SPAWN_RATE = (Integer) foodSpinners.get("Taux spawn").getValue();
                SimParams.FOOD_PER_SPAWN = (Integer) foodSpinners.get("Qte par spawn").getValue();

                JOptionPane.showMessageDialog(this, "Parametres appliques avec succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Parameters updated successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // MODERN CONTROL PANEL
    // ==========================================
    class ControlPanel extends JPanel {
        private JLabel statusLabel;
        private JSpinner preySpinner, predatorSpinner;
        private JButton startBtn, pauseBtn;

        public ControlPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 20, 10, 20));

            // Left - Status & Config
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            leftPanel.setBackground(Color.WHITE);

            statusLabel = new JLabel("PRAŠT");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            statusLabel.setForeground(new Color(108, 117, 125));
            leftPanel.add(statusLabel);
            leftPanel.add(createSeparator());

            leftPanel.add(createLabel("Proies (init):"));
            preySpinner = createSpinner(15, 0, 100);
            leftPanel.add(preySpinner);

            leftPanel.add(createLabel("Predateurs (init):"));
            predatorSpinner = createSpinner(8, 0, 50);
            leftPanel.add(predatorSpinner);

            add(leftPanel, BorderLayout.WEST);


            JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            centerPanel.setBackground(Color.WHITE);

            startBtn = createModernButton("Demarrer", new Color(40, 167, 69));
            pauseBtn = createModernButton("Pause", new Color(220, 53, 69));
            JButton restartBtn = createModernButton("Redemarrer", new Color(0, 123, 255));

            pauseBtn.setEnabled(false);

            startBtn.addActionListener(e -> {
                if (environment.getPreyCount() == 0 && environment.getPredatorCount() == 0) {
                    spawnInitialPopulation();
                }
                startSimulation();
                startBtn.setEnabled(false);
                pauseBtn.setEnabled(true);
                preySpinner.setEnabled(false);
                predatorSpinner.setEnabled(false);
                statusLabel.setText("EN COURS");
                statusLabel.setForeground(new Color(40, 167, 69));
            });

            pauseBtn.addActionListener(e -> {
                stopSimulation();
                startBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
                statusLabel.setText("PAUSE");
                statusLabel.setForeground(new Color(220, 53, 69));
            });

            restartBtn.addActionListener(e -> {
                stopSimulation();
                for (jade.core.AID aid : new java.util.HashSet<>(environment.getAllAgents().keySet())) {
                    environment.unregisterAgent(aid);
                }
                environment.getAllFoods().clear();
                startBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
                preySpinner.setEnabled(true);
                predatorSpinner.setEnabled(true);
                statusLabel.setText("PRAŠT");
                statusLabel.setForeground(new Color(108, 117, 125));
                if (panel != null) panel.repaint();
                if (chart != null) chart.updateData(0, 0);
                if (theoChart != null) theoChart.reset();
                // Reset live stats to 0 via update
                if (parameterPanel != null) parameterPanel.updateLiveStats(0, 0, 0);
            });

            centerPanel.add(startBtn);
            centerPanel.add(pauseBtn);
            centerPanel.add(restartBtn);
            add(centerPanel, BorderLayout.CENTER);

            // Right - Quick Actions (Cleaned up)
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightPanel.setBackground(Color.WHITE);

            JButton addPreyBtn = createQuickButton("+ Proie", new Color(34, 139, 34));
            addPreyBtn.addActionListener(e -> spawnSingleAgent("PreyAgent", "Prey"));
            rightPanel.add(addPreyBtn);

            JButton addPredatorBtn = createQuickButton("+ Predateur", new Color(220, 20, 60));
            addPredatorBtn.addActionListener(e -> spawnSingleAgent("PredatorAgent", "Predator"));
            rightPanel.add(addPredatorBtn);

            JButton spawnFoodBtn = createQuickButton("+ Nourriture", new Color(255, 193, 7));
            spawnFoodBtn.setForeground(Color.BLACK);
            spawnFoodBtn.addActionListener(e -> {
                for(int i=0; i<5; i++) {
                    double x = 50 + Math.random() * (environment.getWidth() - 100);
                    double y = 50 + Math.random() * (environment.getHeight() - 100);
                    environment.spawnFood(new Position(x, y));
                }
            });
            rightPanel.add(spawnFoodBtn);

            add(rightPanel, BorderLayout.EAST);
        }

        private JLabel createLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return l;
        }

        private JSpinner createSpinner(int val, int min, int max) {
            JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, 1));
            s.setPreferredSize(new Dimension(60, 28));
            s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            return s;
        }

        private JSeparator createSeparator() {
            JSeparator s = new JSeparator(SwingConstants.VERTICAL);
            s.setPreferredSize(new Dimension(1, 24));
            s.setForeground(new Color(220, 220, 220));
            return s;
        }

        private JButton createModernButton(String text, Color bg) {
            JButton btn = new JButton(text) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(bg.darker());
                    } else {
                        g2.setColor(bg);
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setPreferredSize(new Dimension(110, 34));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private JButton createQuickButton(String text, Color bg) {
            JButton btn = new JButton(text) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(bg.darker());
                    } else {
                        g2.setColor(bg);
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setPreferredSize(new Dimension(95, 30));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private void spawnInitialPopulation() {
            int preyCount = (Integer) preySpinner.getValue();
            int predatorCount = (Integer) predatorSpinner.getValue();
            for (int i = 0; i < preyCount; i++) spawnSingleAgent("PreyAgent", "Prey");
            for (int i = 0; i < predatorCount; i++) spawnSingleAgent("PredatorAgent", "Predator");
        }

        private void spawnSingleAgent(String className, String prefix) {
            VisualizerAgent.this.spawnAgent(className, prefix);
        }
    }

    // ==========================================
    // SIMULATION PANEL
    // ==========================================
    class SimulationPanel extends JPanel {
        public SimulationPanel() {
            setPreferredSize(new Dimension(900, 650));
            setBackground(Color.WHITE);
            setBorder(null); // Border handled by wrapper
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Map<jade.core.AID, AgentInfo> agents = environment.getAllAgents();
            List<Food> foods = environment.getAllFoods();

            // Draw food with glow effect
            for (Food food : foods) {
                Position pos = food.getPosition();
                // Outer glow
                g2d.setColor(new Color(255, 220, 0, 50));
                g2d.fill(new Ellipse2D.Double(pos.getX() - 8, pos.getY() - 8, 16, 16));
                // Inner circle
                g2d.setColor(new Color(255, 193, 7));
                g2d.fill(new Ellipse2D.Double(pos.getX() - 5, pos.getY() - 5, 10, 10));
            }

            // Draw agents with shadows
            for (AgentInfo info : agents.values()) {
                Position pos = info.getPosition();
                if (info.isPrey()) {
                    // Shadow
                    g2d.setColor(new Color(0, 0, 0, 30));
                    g2d.fill(new Ellipse2D.Double(pos.getX() - 5, pos.getY() - 4, 10, 10));
                    // Agent
                    g2d.setColor(new Color(40, 167, 69));
                    g2d.fill(new Ellipse2D.Double(pos.getX() - 6, pos.getY() - 6, 12, 12));
                    // Highlight
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.fill(new Ellipse2D.Double(pos.getX() - 4, pos.getY() - 5, 4, 4));
                } else {
                    // Shadow
                    g2d.setColor(new Color(0, 0, 0, 40));
                    g2d.fill(new Ellipse2D.Double(pos.getX() - 7, pos.getY() - 6, 14, 14));
                    // Agent
                    g2d.setColor(new Color(220, 53, 69));
                    g2d.fill(new Ellipse2D.Double(pos.getX() - 8, pos.getY() - 8, 16, 16));
                    // Highlight
                    g2d.setColor(new Color(255, 255, 255, 120));
                    g2d.fill(new Ellipse2D.Double(pos.getX() - 5, pos.getY() - 6, 5, 5));
                }
            }
        }
    }

    // ==========================================
    // POPULATION CHART
    // ==========================================
    class PopulationChart extends JPanel {
        private List<Integer> preyHistory = new ArrayList<>();
        private List<Integer> predatorHistory = new ArrayList<>();
        private static final int MAX_POINTS = 200;

        public PopulationChart() {
            setPreferredSize(new Dimension(900, 200));
            setBackground(Color.WHITE);

        }

        public void updateData(int preyCount, int predatorCount) {
            preyHistory.add(preyCount);
            predatorHistory.add(predatorCount);
            if (preyHistory.size() > MAX_POINTS) {
                preyHistory.remove(0);
                predatorHistory.remove(0);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (preyHistory.isEmpty()) {
                g2d.setColor(new Color(150, 150, 150));
                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                g2d.drawString("En attente de donnees...", getWidth() / 2 - 80, getHeight() / 2);
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int padding = 40;

            int maxPop = Math.max(
                    preyHistory.stream().max(Integer::compareTo).orElse(1),
                    predatorHistory.stream().max(Integer::compareTo).orElse(1)
            );
            maxPop = Math.max(maxPop, 10);

            // Clean Grid
            g2d.setColor(new Color(245, 245, 245));
            for (int i = 0; i <= 5; i++) {
                int y = padding + i * (height - 2 * padding) / 5;
                g2d.drawLine(padding, y, width - padding, y);
            }

            // Axes
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(padding, padding, padding, height - padding);
            g2d.drawLine(padding, height - padding, width - padding, height - padding);

            // Labels
            g2d.setColor(new Color(120, 120, 120));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.drawString("0", padding - 15, height - padding + 5);
            g2d.drawString(String.valueOf(maxPop), padding - 25, padding + 5);

            double xScale = (double) (width - 2 * padding) / MAX_POINTS;
            double yScale = (double) (height - 2 * padding) / maxPop;

            // Draw prey line (Green)
            g2d.setColor(new Color(40, 167, 69));
            g2d.setStroke(new BasicStroke(2f)); // Thinner, sharper line
            drawCurve(g2d, preyHistory, xScale, yScale, padding, height);

            // Draw predator line (Red)
            g2d.setColor(new Color(220, 53, 69));
            drawCurve(g2d, predatorHistory, xScale, yScale, padding, height);

            // Legend
            int legendX = width - 140;
            int legendY = 20;

            drawLegendItem(g2d, legendX, legendY, new Color(40, 167, 69), "Proies");
            drawLegendItem(g2d, legendX, legendY + 20, new Color(220, 53, 69), "Predateurs");
        }

        private void drawLegendItem(Graphics2D g2d, int x, int y, Color c, String text) {
            g2d.setColor(c);
            g2d.fillOval(x, y, 10, 10);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.drawString(text, x + 15, y + 9);
        }

        private void drawCurve(Graphics2D g2d, List<Integer> history, double xScale, double yScale, int padding, int height) {
            for (int i = 1; i < history.size(); i++) {
                int x1 = padding + (int) ((i - 1) * xScale);
                int y1 = height - padding - (int) (history.get(i - 1) * yScale);
                int x2 = padding + (int) (i * xScale);
                int y2 = height - padding - (int) (history.get(i) * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    // ==========================================
    // THEORETICAL (LOTKA-VOLTERRA)
    // ==========================================
    class TheoreticalChart extends JPanel {
        private List<Double> preyHistory = new ArrayList<>();
        private List<Double> predatorHistory = new ArrayList<>();
        private static final int MAX_POINTS = 200;

        // Math Parameters
        private double alpha = 0.1; // Prey birth
        private double beta = 0.02; // Predation
        private double gamma = 0.1; // Predator death
        private double delta = 0.01; // Predator reproduction

        // Current state
        private double x = 20.0; // Initial Prey
        private double y = 5.0;  // Initial Predator
        private double dt = 0.1; // Time step

        public TheoreticalChart() {
            setBackground(Color.WHITE);
            setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
            // Initialize with starting values
            preyHistory.add(x);
            predatorHistory.add(y);
        }

        public void nextStep() {
            // Euler Method for Lotka-Volterra
            double dx = (alpha * x - beta * x * y) * dt;
            double dy = (delta * x * y - gamma * y) * dt;

            x += dx;
            y += dy;

            // Prevent negative populations
            x = Math.max(0, x);
            y = Math.max(0, y);

            preyHistory.add(x);
            predatorHistory.add(y);

            if (preyHistory.size() > MAX_POINTS) {
                preyHistory.remove(0);
                predatorHistory.remove(0);
            }
            repaint();
        }

        public void reset() {
            preyHistory.clear();
            predatorHistory.clear();
            x = 20.0;
            y = 5.0;
            preyHistory.add(x);
            predatorHistory.add(y);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 30;


            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString("Modele LV", padding, 20);

            // Find max for scaling
            double maxPop = 10.0;
            for(double v : preyHistory) maxPop = Math.max(maxPop, v);
            for(double v : predatorHistory) maxPop = Math.max(maxPop, v);

            // Draw axes
            g2d.setColor(new Color(230, 230, 230));
            g2d.drawLine(padding, height - padding, width - padding, height - padding); // X axis
            g2d.drawLine(padding, padding, padding, height - padding); // Y axis

            double xScale = (double) (width - 2 * padding) / MAX_POINTS;
            double yScale = (double) (height - 2 * padding) / maxPop;

            // Draw Curves
            drawCurve(g2d, preyHistory, xScale, yScale, padding, height, new Color(34, 139, 34)); // Green
            drawCurve(g2d, predatorHistory, xScale, yScale, padding, height, new Color(220, 53, 69)); // Red
        }

        private void drawCurve(Graphics2D g2d, List<Double> history, double xScale, double yScale, int padding, int height, Color c) {
            g2d.setColor(c);
            g2d.setStroke(new BasicStroke(1.5f));
            for (int i = 1; i < history.size(); i++) {
                int x1 = padding + (int) ((i - 1) * xScale);
                int y1 = height - padding - (int) (history.get(i - 1) * yScale);
                int x2 = padding + (int) (i * xScale);
                int y2 = height - padding - (int) (history.get(i) * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

}