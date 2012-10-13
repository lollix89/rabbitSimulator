import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {

	private Schedule schedule;

	private RabbitsGrassSimulationSpace worldSpace;
	private DisplaySurface displaySurf;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitList;

	// Default Values
	private static final int NUMRABBITS = 20;
	private static final int WORLDXSIZE = 20;
	private static final int WORLDYSIZE = 20;
	private static final int REPRODUCTIONCOST = 6;
	private static final int INITIALGRASS = 15;
	private static final int GRASSGROWTHRATE = 10;
	private static final int ENERGYCONSUMPTION = 2;


	private int numRabbits = NUMRABBITS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int reproductionCost = REPRODUCTIONCOST;
	private int initialGrass = INITIALGRASS;
	private int GrassGrowthRate = GRASSGROWTHRATE;
	private int energyConsumption = ENERGYCONSUMPTION;

	private OpenSequenceGraph RabbitPopulation;
	class RabbitOnPlace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double)worldSpace.RabbitPopulation();
		}
	}
	
	public String getName(){
		return "Rabbits World Simulator";
	}

	/**
	 * Initialization of Displays
	 */
	public void setup(){
		System.out.println("System Setup");
		schedule = new Schedule(1);
		worldSpace = null;
		rabbitList = new ArrayList<RabbitsGrassSimulationAgent>();

		if (displaySurf != null){
			displaySurf.dispose();
		}
		displaySurf = null;
		displaySurf = new DisplaySurface(this, "Rabbit World Simulator");
		registerDisplaySurface("Rabbit World Simulator", displaySurf);

		if (RabbitPopulation != null){
			RabbitPopulation.dispose();
		}
		RabbitPopulation = null;
		RabbitPopulation = new OpenSequenceGraph("Rabbit population", this);
		this.registerMediaProducer("Plot", RabbitPopulation);
	}

	/**
	 * Creates all necessary instances and initializes the model. Starts simulation and displays the windows.
	 */
	public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurf.display();
		RabbitPopulation.display();
	}

	/**
	 * Initializes the Model (Creation of the worldSpace and adding of rabbits to the system)
	 */
	public void buildModel(){
		System.out.println("Running BuildModel");

		worldSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
		worldSpace.growGrass(initialGrass);

		for(int i = 0; i < numRabbits; i++) {
			addNewRabbit();
		}
	}

	/**
	 * Create Schedule for each step
	 */
	public void buildSchedule(){
		System.out.println("Running BuildSchedule");

		class worldStep extends BasicAction {
			public void execute() {
				System.out.println("**********************STEP**********************");
				// Let the grass grow
				worldSpace.growGrass(GrassGrowthRate);
				// Delete all death rabbits
				deleteDeadrabbits();

				SimUtilities.shuffle(rabbitList);
				for(int i = 0; i < rabbitList.size(); i++) {
					// Get rabbit instance from the list
					RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbitList.get(i);
					rabbit.report();
					// Move rabbit from position xy to another (or the rabbit stays on the same cell)
					rabbit.moveRabbit();
					rabbit.consumeEnergy(energyConsumption);	
					// Every rabbit eats some grass
					rabbit.eatGrass();
				}
				// If a rabbit has enough energy, he can reproduce itself
				makeRabbitReproduction();
				displaySurf.updateDisplay();
			}
		}

		schedule.scheduleActionBeginning(0, new worldStep());
		
		/*
		 * Method used for diagram
		 */
	    class UpdateRabbitPopulationInSpace extends BasicAction {
	        public void execute(){
	        	RabbitPopulation.step();
	        }
	      }

	      schedule.scheduleActionAtInterval(10, new UpdateRabbitPopulationInSpace()); // Only every tenth step, this is executed
	}
	
	/**
	 * Death rabbits need to be deleted out of the simulation
	 * @return number of death rabbits
	 */
	private int deleteDeadrabbits(){
		int deadRabbits = 0;		
		for(int i = (rabbitList.size() - 1); i >= 0 ; i--){
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent)rabbitList.get(i);
			if(rabbit.getEnergy() <= 0){
				worldSpace.removeRabbitAt(rabbit.getPositionX(), rabbit.getPositionY());
				rabbitList.remove(i);
				deadRabbits++;
			}
		}
		return deadRabbits;
	}
	
	/**
	 * If rabbits have enough energy, they can generate a new rabbit
	 * @return new rabbit
	 */
	private int makeRabbitReproduction(){
		int newRabbits = 0;
		for(int i = rabbitList.size()-1; i >= 0; i--){
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent)rabbitList.get(i);
			if(rabbit.getEnergy() > reproductionCost ){
				RabbitsGrassSimulationAgent newRabbit = new RabbitsGrassSimulationAgent();
				// Place rabbit in the field
				if(worldSpace.findPlaceRabbit(newRabbit)){
					newRabbit.setWorld(worldSpace);
					rabbit.setEnergy(rabbit.getEnergy()- reproductionCost);
					rabbitList.add(newRabbit);
					newRabbits++;
				}
				else{
					//grid is full, can not generate new rabbits
					newRabbit= null;
				}
			}
		}
		return newRabbits;
	}

	/**
	 * Build display (Colors, mapping between values and grid
	 */
	public void buildDisplay(){
		ColorMap map = new ColorMap();

		for(int i = 0; i < 16; i++) {
			map.mapColor(i, new Color(0, (int)(i*15+15), 0));
		}
		map.mapColor(0, Color.white);

		Value2DDisplay displayGrass = new Value2DDisplay(worldSpace.getCurrentGrassSpace(), map);
		displaySurf.addDisplayable(displayGrass, "Grass");

		// Display rabbits
		Object2DDisplay displayRabbit = new Object2DDisplay(worldSpace.getCurrentRabbitSpace());
		displayRabbit.setObjectList(rabbitList);
		displaySurf.addDisplayable(displayRabbit, "Rabbit");
		
		RabbitPopulation.addSequence("Rabbit Population", new RabbitOnPlace());
	}

	public Schedule getSchedule(){
		return schedule;
	}

	public String[] getInitParam(){
		System.out.println("Init parameters");
		String[] initParams = {"NumRabbits", "InitialEnergy", "EnergyConsumption", "WorldXSize", "WorldYSize", "ReproductionCost", "initialGrass", "GrassGrowthRate" };
		return initParams;
	}

	public int getNumRabbits(){
		return numRabbits;
	}

	public void setNumRabbits(int na){
		numRabbits = na;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public void setWorldXSize(int x) {
		worldXSize = x;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void setWorldYSize(int y) {
		worldXSize = y;
	}

	public int getReproductionCost() {
		return reproductionCost;
	}

	public void setReproductionCost(int rc) {
		reproductionCost = rc;
	}
	public int getEnergyConsumption() {
		return energyConsumption;
	}

	public void setEnergyConsumption(int ec) {
		energyConsumption = ec;
	}

	public int getGrassGrowthRate() {
		return GrassGrowthRate;
	}

	public void setGrassGrowthRate(int grassGrowthRate) {
		GrassGrowthRate = grassGrowthRate;
	}
	public int getInitialGrass() {
		return initialGrass;
	}

	public void setInitialGrass(int initialGrass) {
		this.initialGrass = initialGrass;
	}

	public void addNewRabbit() {
		RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent();
		if(worldSpace.findPlaceRabbit(r)) {
			r.setWorld(worldSpace);
			rabbitList.add(r);
		} else {
			r = null;
		}
	}

	public static void main(String[] args) {
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, null, false);
	}
}
