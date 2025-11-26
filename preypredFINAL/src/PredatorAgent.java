import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import java.util.List;

public class PredatorAgent extends Agent {
    private Position position;
    private int energy;
    private Environment environment;

    // Static constants that don't change
    private static final int ENERGY_LOSS = 1;
    private static final int VISION_RANGE = 110;
    private static final double CATCH_DISTANCE = 25.0;
    private static final int REPRO_COOLDOWN = 800;
    private static final int EATING_COOLDOWN = 100;

    protected void setup() {
        environment = Environment.getInstance();
        Object[] args = getArguments();

        if (args != null && args.length >= 2) {
            double x = (Double) args[0];
            double y = (Double) args[1];
            x = Math.max(50, Math.min(environment.getWidth() - 50, x));
            y = Math.max(50, Math.min(environment.getHeight() - 50, y));
            position = new Position(x, y);
        } else {
            position = new Position(
                    100 + Math.random() * (environment.getWidth() - 200),
                    100 + Math.random() * (environment.getHeight() - 200)
            );
        }

        energy = VisualizerAgent.SimParams.PRED_ENERGY_START;
        environment.registerAgent(getAID(), "PREDATOR", position, energy);

        addBehaviour(new PredatorBehaviour());
    }

    protected void takeDown() {
        environment.unregisterAgent(getAID());
    }

    private class PredatorBehaviour extends CyclicBehaviour {
        private int reproductionCooldown = 0;
        private int eatingCooldown = 0;
        private int cycleCount = 0;

        public void action() {
            cycleCount++;

            // Lose energy every 4 cycles
            if (cycleCount % 4 == 0) {
                energy -= ENERGY_LOSS;
            }

            if (reproductionCooldown > 0) reproductionCooldown--;
            if (eatingCooldown > 0) eatingCooldown--;

            // Death check
            if (energy <= 0) {
                System.out.println("ðŸ’€ " + getLocalName() + " starved");
                myAgent.doDelete();
                return;
            }

            // PERCEPTION
            List<AgentInfo> nearby = environment.getNearbyAgents(
                    getAID(),
                    position,
                    VISION_RANGE
            );

            List<AgentInfo> preyList = nearby.stream()
                    .filter(info -> info.isPrey() && !info.getAID().equals(getAID()))
                    .toList();

            List<AgentInfo> nearbyPredators = nearby.stream()
                    .filter(info -> info.isPredator() && !info.getAID().equals(getAID()))
                    .toList();


            boolean justAte = false;

            // if too crowded with other predators
            if (nearbyPredators.size() > 4) {
                // Too many predators here  disperse
                disperseFromCrowd(nearbyPredators);
            } else if (!preyList.isEmpty() && eatingCooldown <= 0) {
                // Find closest prey
                AgentInfo target = preyList.get(0);
                double minDist = position.distance(target.getPosition());

                for (AgentInfo prey : preyList) {
                    double dist = position.distance(prey.getPosition());
                    if (dist < minDist) {
                        minDist = dist;
                        target = prey;
                    }
                }

                // Try to catch - 1 prey at time
                if (minDist <= CATCH_DISTANCE) {
                    capture(target);
                    justAte = true;
                    eatingCooldown = EATING_COOLDOWN;
                } else {
                    // Chase
                    double dx = target.getPosition().getX() - position.getX();
                    double dy = target.getPosition().getY() - position.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        dx /= dist;
                        dy /= dist;
                    }
                    position = new Position(
                            position.getX() + dx * VisualizerAgent.SimParams.PRED_SPEED,
                            position.getY() + dy * VisualizerAgent.SimParams.PRED_SPEED
                    );
                }
            } else {

                if (energy >= VisualizerAgent.SimParams.PRED_REPRO_THRESHOLD && reproductionCooldown <= 0) {
                    if (Math.random() < 0.08) { // 8% chance - reproduce faster
                        List<AgentInfo> partners = nearby.stream()
                                .filter(AgentInfo::isPredator)
                                .toList();
                        if (!partners.isEmpty() && partners.size() < 3) {
                            reproduce();
                        }
                    }
                }

                // Always keep moving
                double angle = Math.random() * 2 * Math.PI;
                position = new Position(
                        position.getX() + Math.cos(angle) * VisualizerAgent.SimParams.PRED_SPEED * 0.6,
                        position.getY() + Math.sin(angle) * VisualizerAgent.SimParams.PRED_SPEED * 0.6
                );
            }

            // stayss strictly within bounds
            double x = position.getX();
            double y = position.getY();
            x = Math.max(30, Math.min(environment.getWidth() - 30, x));
            y = Math.max(30, Math.min(environment.getHeight() - 30, y));
            position = new Position(x, y);

            environment.updatePosition(getAID(), position);

            // Slow down after eating
            try {
                Thread.sleep(justAte ? 120 : 45);
            } catch (Exception e) {}
        }

        private void capture(AgentInfo prey) {
            // Use dynamic energy gain and max
            energy = Math.min(VisualizerAgent.SimParams.PRED_ENERGY_MAX,
                    energy + VisualizerAgent.SimParams.PRED_ENERGY_GAIN);
            environment.unregisterAgent(prey.getAID());



            System.out.println("ðŸ¦ " + getLocalName() + " ate prey (E:" + energy + ")");
        }

        private void reproduce() {
            // Use dynamic reproduction cost
            energy -= VisualizerAgent.SimParams.PRED_REPRO_COST;
            reproductionCooldown = REPRO_COOLDOWN;

            try {
                // Spawn NEAR parent
                Object[] args = new Object[]{
                        position.getX() + (Math.random() - 0.5) * 60,
                        position.getY() + (Math.random() - 0.5) * 60
                };
                String name = "Predator_" + System.nanoTime();
                getContainerController().createNewAgent(name, "PredatorAgent", args).start();
                System.out.println("ðŸ¶ " + getLocalName() + " had baby");
            } catch (Exception e) {}
        }

        private void disperseFromCrowd(List<AgentInfo> nearbyPredators) {
            // Calculate center of predator pack
            double avgX = 0, avgY = 0;
            for (AgentInfo other : nearbyPredators) {
                avgX += other.getPosition().getX();
                avgY += other.getPosition().getY();
            }
            avgX /= nearbyPredators.size();
            avgY /= nearbyPredators.size();

            // Move AWAY from pack center to spread out hunting territory
            double disperseX = position.getX() - avgX;
            double disperseY = position.getY() - avgY;

            // Add randomness
            disperseX += (Math.random() - 0.5) * 150;
            disperseY += (Math.random() - 0.5) * 150;

            position = new Position(
                    position.getX() + disperseX * 0.1,
                    position.getY() + disperseY * 0.1
            );
        }
    }
}