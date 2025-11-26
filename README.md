# 🦁 Lotka-Volterra Multi-Agent Ecosystem Simulation

An advanced multi-agent system simulating predator-prey population dynamics using the JADE framework, with real-time visualization and comparison to the classical Lotka-Volterra mathematical model.

## 🌟 Overview

This project implements an agent-based model of an ecosystem where autonomous prey and predator agents interact in a shared environment. The simulation demonstrates emergent population dynamics that align with the classical Lotka-Volterra equations from mathematical ecology.

## 🎯 Features

### Multi-Agent System
- **Autonomous Prey Agents**
  - Food-seeking behavior with spatial awareness
  - Predator detection and flee response
  - Reproduction when energy threshold is met
  - Anti-crowding dispersion behavior
  - Energy-based survival mechanics

- **Autonomous Predator Agents**
  - Active prey hunting with vision range
  - Chase and capture mechanics
  - Energy consumption and starvation
  - Reproduction based on successful hunting
  - Territorial dispersion when overcrowded

- **Dynamic Environment**
  - Singleton environment manager
  - Random food spawning system
  - Spatial agent queries (nearby agents, collision detection)
  - Boundary enforcement

### Visualization & Analysis
- **Real-time 2D Visualization**: Animated agents with smooth movement, food resources, visual distinction between species
- **Population Charts**: Live population tracking over time, dual chart system (simulation vs theory)
- **Lotka-Volterra Comparison**: Classical predator-prey differential equations, side-by-side with simulation results
- **Interactive Controls**: Start/Pause/Restart simulation, manual agent spawning, adjustable initial populations

### Emergent Behavior
- Cyclical population oscillations
- Predator-prey equilibrium dynamics
- Spatial clustering and dispersion
- Resource competition
- Ecosystem stability patterns

## 🛠️ Technologies

- **JADE** (Java Agent DEvelopment Framework) - Multi-agent platform
- **Java Swing** - GUI and visualization
- **Concurrent Programming** - Thread-safe agent management
- **Mathematical Modeling** - Lotka-Volterra differential equations

## 📊 Lotka-Volterra Model

The simulation compares agent-based results with the classical model:

dX/dt = αX - βXY (Prey growth)
dY/dt = δXY - γY (Predator growth)


Where:
- X = Prey population
- Y = Predator population
- α = Prey birth rate
- β = Predation rate
- γ = Predator death rate
- δ = Predator efficiency

## 📁 Project Structure

├── AgentInfo.java # Agent data container
├── Environment.java # Singleton environment manager
├── Food.java # Food resource class
├── Position.java # 2D position with movement utilities
├── PreyAgent.java # Autonomous prey agent
├── PredatorAgent.java # Autonomous predator agent
├── VisualizerAgent.java # GUI and simulation controller
├── LotkaVolterraComparator.java # Theoretical model comparison
├── SimulationLauncher.java # Main entry point
└── Main.java # Alternative launcher


## 🚀 Installation & Setup

### Prerequisites
- **Java JDK 8+**
- **JADE Framework** (jade.jar)

### Download JADE
1. Visit [JADE Official Website](https://jade.tilab.com/)
2. Download JADE binary distribution
3. Extract `jade.jar` to your project's `lib/` folder

### Clone & Run

Clone the repository

git clone https://github.com/mehdilounnas/lotka-volterra-multiagent-simulation.git
cd lotka-volterra-multiagent-simulation
Compile (make sure jade.jar is in classpath)

javac -cp ".:jade.jar" *.java
Run the simulation

java -cp ".:jade.jar" jade.Boot -gui -agents "Visualizer:VisualizerAgent"
Or use the launcher

java -cp ".:jade.jar" SimulationLauncher


## 🎮 Usage

### Starting a Simulation
1. Launch the application
2. Set initial prey and predator populations using spinners
3. Click **Start** to begin the simulation
4. Observe population dynamics in real-time

### Interactive Controls
- **Start**: Begin simulation with configured populations
- **Pause**: Freeze the simulation
- **Restart**: Reset environment and agents
- **+ Proie**: Spawn a single prey agent
- **+ Predateur**: Spawn a single predator agent
- **+ Nourriture**: Spawn food resources

### Parameter Tuning
Adjust simulation parameters in `VisualizerAgent.SimParams`:
- Prey/Predator energy levels
- Reproduction thresholds
- Movement speeds
- Food spawn rates
- Vision ranges

## 📈 Simulation Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| PREY_ENERGY_START | 85 | Initial prey energy |
| PREY_REPRO_THRESHOLD | 75 | Energy needed to reproduce |
| PRED_ENERGY_START | 200 | Initial predator energy |
| PRED_ENERGY_GAIN | 70 | Energy from eating prey |
| PREY_SPEED | 2.1 | Prey movement speed |
| PRED_SPEED | 3.5 | Predator movement speed |
| FOOD_SPAWN_RATE | 10 cycles | Food spawn frequency |

## 🧠 AI Concepts Demonstrated

- **Autonomous Agents**: Independent decision-making entities
- **Agent Behaviors**: Cyclic behaviors with perception-action loops
- **Multi-Agent Coordination**: Implicit coordination through shared environment
- **Emergent Behavior**: Complex patterns from simple rules
- **Agent Communication**: JADE messaging infrastructure
- **Spatial Reasoning**: Distance-based perception and movement
- **Mathematical Ecology**: Population dynamics modeling





## 🔬 Research Applications

This simulation demonstrates concepts relevant to:
- Ecological modeling and conservation biology
- Agent-based modeling (ABM) in complex systems
- Swarm intelligence and collective behavior
- Evolutionary algorithms and artificial life
- Game theory and competitive dynamics

## 🤝 Contributing

This project was developed for educational purposes as part of an M1 Artificial Intelligence program. Suggestions and improvements are welcome!

## 📚 References

- Lotka, A. J. (1925). *Elements of Physical Biology*
- Volterra, V. (1926). *Variazioni e fluttuazioni del numero d'individui in specie animali conviventi*
- [JADE Framework Documentation](https://jade.tilab.com/documentation/)

## 👨‍💻 Author

Developed for M1 Artificial Intelligence coursework

## 📄 License

Educational project - Free to use and modify
