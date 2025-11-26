import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import java.util.List;

public class PreyAgent extends Agent {
    private Position position;
    private int energy;
    private int age;
    private Environment environment;

    // Reference to shared parameters (will be updated dynamically)
    private static final int AGE_MAX = 1500;
    private static final int VISION_RANGE = 100;
    private static final double FOOD_SEARCH_RADIUS = 120.0;
    private static final double FOOD_EAT_DISTANCE = 20.0;
    private static final int REPRO_COOLDOWN = 300;

    protected void setup() {
        environment = Environment.getInstance();
        Object[] args = getArguments();

        if (args != null && args.length >= 2) {
            position = new Position((Double) args[0], (Double) args[1]);
        } else {
            position = new Position(
                    Math.random() * environment.getWidth(),
                    Math.random() * environment.getHeight()
            );
        }

        energy = VisualizerAgent.SimParams.PREY_ENERGY_START;
        age = 0;
        environment.registerAgent(getAID(), "PREY", position, energy);

        addBehaviour(new PreyBehaviour());
    }

    protected void takeDown() {
        environment.unregisterAgent(getAID());
    }

    private class PreyBehaviour extends CyclicBehaviour {
        private int reproductionCooldown = 0;

        public void action() {
            // Check for DIE message

            // Reactive Check
            if (!environment.isRegistered(getAID())) {
                myAgent.doDelete();
                return;
            }


            // Age & Energy
            age++;

            // Lose energy every 3 cycles (slower)
            if (age % 3 == 0) {
                energy -= 1; // ENERGY_LOSS constant
            }

            reproductionCooldown--;

            // Death conditions -
            if (energy <= 0 || age > AGE_MAX) {
                myAgent.doDelete();
                return;
            }

            // Perception
            List<AgentInfo> nearby = environment.getNearbyAgents(getAID(), position, VISION_RANGE);
            List<AgentInfo> predators = nearby.stream().filter(AgentInfo::isPredator).toList();
            List<AgentInfo> nearbyPrey = nearby.stream().filter(AgentInfo::isPrey).toList();

            // BEHAVIOR
            if (!predators.isEmpty()) {
                // FLEE from predators
                flee(predators);
            } else if (nearbyPrey.size() > 8) {
                // TOO CROWDED
                disperseFromCrowd(nearbyPrey);
            } else {
                // Look for food when safe
                Food nearestFood = environment.findNearestFood(position, FOOD_SEARCH_RADIUS);

                if (nearestFood != null) {
                    // Move towards food
                    double dist = position.distance(nearestFood.getPosition());

                    if (dist <= FOOD_EAT_DISTANCE) {
                        // EAT THE FOOD
                        if (environment.consumeFood(nearestFood)) {
                            energy = Math.min(VisualizerAgent.SimParams.PREY_ENERGY_MAX,
                                    energy + nearestFood.getEnergyValue());
                            System.out.println("ðŸƒ " + getLocalName() + " ate food (E:" + energy + ")");
                        }
                    } else {
                        // Chase the food
                        double dx = nearestFood.getPosition().getX() - position.getX();
                        double dy = nearestFood.getPosition().getY() - position.getY();
                        double foodSpeed = (energy < 50) ? VisualizerAgent.SimParams.PREY_SPEED * 1.5
                                : VisualizerAgent.SimParams.PREY_SPEED;
                        position = position.moveTo(dx, dy, foodSpeed);
                    }
                } else {
                    // No food nearby
                    if (Math.random() < 0.30) { // 30% chance - easier survival
                        energy = Math.min(VisualizerAgent.SimParams.PREY_ENERGY_MAX, energy + 12);
                    }

                    // Random walk
                    position = position.randomMove(VisualizerAgent.SimParams.PREY_SPEED * 0.7,
                            environment.getWidth(),
                            environment.getHeight());
                }

                // Try to reproduce
                if (energy >= VisualizerAgent.SimParams.PREY_REPRO_THRESHOLD && reproductionCooldown <= 0) {
                    if (Math.random() < 0.20) {
                        List<AgentInfo> partners = nearby.stream().filter(AgentInfo::isPrey).toList();
                        if (!partners.isEmpty() && partners.size() < 8) {
                            reproduce();
                        }
                    }
                }
            }

            // Keep in bounds
            position.setX(Math.max(20, Math.min(environment.getWidth() - 20, position.getX())));
            position.setY(Math.max(20, Math.min(environment.getHeight() - 20, position.getY())));

            environment.updatePosition(getAID(), position);

            try { Thread.sleep(30); } catch (Exception e) {}
        }

        private void flee(List<AgentInfo> predators) {
            // Calculate average predator position
            double predX = 0, predY = 0;
            for (AgentInfo pred : predators) {
                predX += pred.getPosition().getX();
                predY += pred.getPosition().getY();
            }
            predX /= predators.size();
            predY /= predators.size();

            // Direction away from predators
            double fleeX = position.getX() - predX;
            double fleeY = position.getY() - predY;

            // Flee
            double speed = VisualizerAgent.SimParams.PREY_SPEED * 1.5;
            position = position.moveTo(fleeX, fleeY, speed);
        }

        private void reproduce() {
            // Use dynamic reproduction cost
            energy -= VisualizerAgent.SimParams.PREY_REPRO_COST;
            reproductionCooldown = REPRO_COOLDOWN;

            try {
                Object[] args = new Object[]{
                        position.getX() + (Math.random() - 0.5) * 40,
                        position.getY() + (Math.random() - 0.5) * 40
                };
                String name = "Prey_" + System.nanoTime();
                getContainerController().createNewAgent(name, "PreyAgent", args).start();
            } catch (Exception e) {}
        }

        private void disperseFromCrowd(List<AgentInfo> nearbyAgents) {
            // Calculate center of the crowd
            double avgX = 0, avgY = 0;
            for (AgentInfo other : nearbyAgents) {
                avgX += other.getPosition().getX();
                avgY += other.getPosition().getY();
            }
            avgX /= nearbyAgents.size();
            avgY /= nearbyAgents.size();

            // Move AWAY from crowd center
            double disperseX = position.getX() - avgX;
            double disperseY = position.getY() - avgY;

            // Add randomness
            disperseX += (Math.random() - 0.5) * 100;
            disperseY += (Math.random() - 0.5) * 100;

            // Move the agent slowly away
            position = new Position(
                    position.getX() + disperseX * 0.05,
                    position.getY() + disperseY * 0.05
            );
        }
    }
}