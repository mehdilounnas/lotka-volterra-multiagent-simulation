import jade.core.AID;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Environment {
    private static Environment instance;
    private int width = 800;
    private int height = 600;
    private Map<AID, AgentInfo> agents;
    private List<Food> foods;  // FOOD SYSTEM

    private static final double COLLISION_DISTANCE = 10.0;
    private static final int FOOD_ENERGY = 35;  // Increased from 25 to 35

    private Environment() {
        agents = new ConcurrentHashMap<>();
        foods = new CopyOnWriteArrayList<>();
    }

    public static synchronized Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    public synchronized void registerAgent(AID aid, String type, Position position, int energy) {
        AgentInfo info = new AgentInfo(aid, type, position, energy);
        agents.put(aid, info);
        System.out.println("âœ“ Registered: " + info);
    }

    public synchronized void unregisterAgent(AID aid) {
        AgentInfo removed = agents.remove(aid);
        if (removed != null) {
            System.out.println("âœ— Removed: " + removed);
        }
    }

    public synchronized void updatePosition(AID aid, Position newPosition) {
        AgentInfo info = agents.get(aid);
        if (info != null) {
            // Garder dans les limites de l'environnement
            double x = Math.max(0, Math.min(width, newPosition.getX()));
            double y = Math.max(0, Math.min(height, newPosition.getY()));
            info.setPosition(new Position(x, y));
        }
    }

    public synchronized List<AgentInfo> getNearbyAgents(AID requester, Position position, double radius) {
        List<AgentInfo> nearby = new ArrayList<>();
        for (Map.Entry<AID, AgentInfo> entry : agents.entrySet()) {
            if (!entry.getKey().equals(requester)) {
                AgentInfo info = entry.getValue();
                if (info.getPosition().distance(position) <= radius) {
                    nearby.add(info);
                }
            }
        }
        return nearby;
    }

    public synchronized AgentInfo checkPreyCollision(Position predatorPos) {
        for (AgentInfo info : agents.values()) {
            if (info.isPrey() && info.getPosition().distance(predatorPos) <= COLLISION_DISTANCE) {
                return info;
            }
        }
        return null;
    }

    public synchronized List<AgentInfo> getAllPrey() {
        List<AgentInfo> preyList = new ArrayList<>();
        for (AgentInfo info : agents.values()) {
            if (info.isPrey()) {
                preyList.add(info);
            }
        }
        return preyList;
    }

    public synchronized List<AgentInfo> getAllPredators() {
        List<AgentInfo> predatorList = new ArrayList<>();
        for (AgentInfo info : agents.values()) {
            if (info.isPredator()) {
                predatorList.add(info);
            }
        }
        return predatorList;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Map<AID, AgentInfo> getAllAgents() {
        return new HashMap<>(agents);
    }

    public int getPreyCount() {
        return (int) agents.values().stream().filter(AgentInfo::isPrey).count();
    }

    public int getPredatorCount() {
        return (int) agents.values().stream().filter(AgentInfo::isPredator).count();
    }

    // FOOD MANAGEMENT
    public synchronized void spawnFood(Position position) {
        foods.add(new Food(position, FOOD_ENERGY));
    }

    public synchronized Food findNearestFood(Position position, double radius) {
        Food nearest = null;
        double minDist = radius;

        for (Food food : foods) {
            if (!food.isConsumed()) {
                double dist = position.distance(food.getPosition());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = food;
                }
            }
        }
        return nearest;
    }

    public synchronized boolean consumeFood(Food food) {
        if (food != null && !food.isConsumed()) {
            food.consume();
            foods.remove(food);
            return true;
        }
        return false;
    }

    public synchronized List<Food> getAllFoods() {
        return new ArrayList<>(foods);
    }

    public synchronized int getFoodCount() {
        return foods.size();
    }

    public synchronized boolean isRegistered(AID aid) {
        return agents.containsKey(aid);
    }
}