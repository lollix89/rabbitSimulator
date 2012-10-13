
import uchicago.src.sim.space.Object2DGrid;
import java.util.Vector;
import java.awt.Point;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {

	private final static int MAXGRASS = 15;
	private final static int MINGRASSENERGY= 4;
	private final static int MAXGRASSENERGY= 10;

	private Object2DGrid grassSpace;
	private Object2DGrid rabbitSpace;

	/**
	 * Constructor for the class RabbitsGrassSimulationSpace. Initializes the grass space.
	 * @param xSize
	 * @param ySize
	 */
	public RabbitsGrassSimulationSpace(int xSize, int ySize){
		//build a grid of xSize x ySize with objects Integer(0)
		grassSpace = new Object2DGrid(xSize, ySize);
		for(int i = 0; i < xSize; i++){
			for(int j = 0; j < ySize; j++){
				grassSpace.putObjectAt(i,j,new Integer(0));
			}
		}
		rabbitSpace = new Object2DGrid(xSize, ySize);
	}

	/**
	 * Adds to every field the same amount of grass.
	 * 
	 * @param grassGrowsRate
	 */
	public void growGrass(int grassGrowRate){
		for(int i = 0; i < grassGrowRate; i++) {
			int x = (int) (Math.random()*(grassSpace.getSizeX()));
			int y = (int) (Math.random()*(grassSpace.getSizeY()));
			int actualGrass = ((Integer) grassSpace.getObjectAt(x,y)).intValue();
			int grassEnergy = MINGRASSENERGY + (int)(Math.random()*MAXGRASSENERGY);
			if(actualGrass+ grassEnergy < MAXGRASS) {
				grassSpace.putObjectAt(x, y, new Integer(actualGrass+grassGrowRate));
			} else {
				grassSpace.putObjectAt(x, y, new Integer(MAXGRASS));
			}
		}
	}

	/**
	 * Eats a certain amount of grass from xy. If the amount is less than the predefined value, then all the grass is eaten from the cell.
	 * 
	 * @param x
	 * @param y
	 * @return the amount of grass eaten at position xy
	 */
	public int eatGrassAt(int x, int y) {
		int i = 0;
		int amountEated= 0;
		if(grassSpace.getObjectAt(x,y) != null){
			i = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
			amountEated= (int)(Math.random()* (i+ 0.2));
			if (amountEated> i){
				amountEated= i;
				grassSpace.putObjectAt(x, y, new Integer(0));
			}
			else{
				grassSpace.putObjectAt(x, y, new Integer(i - amountEated));
			}
		}
		return amountEated;	
	}

	/**
	 * Check if there is a rabbit at cell xy.
	 * @param x
	 * @param y
	 * @return true or false
	 */
	public boolean checkIfRabbitOn(int x, int y) {
		boolean retVal = false;
		if(rabbitSpace.getObjectAt(x, y) != null) {
			retVal = true;
		}
		return retVal;
	}

	/**
	 * Remove a rabbit from the cell at xy
	 * @param x
	 * @param y
	 */
	public void removeRabbitAt(int x, int y) {
		rabbitSpace.putObjectAt(x, y, null);
	}

	/**
	 * find an empty space for a new rabbit. Returns true if the rabbit was placed, otherwise false.
	 * @param newRabbit
	 * @return true or false
	 */
	public boolean findPlaceRabbit(RabbitsGrassSimulationAgent newRabbit) {
		boolean addedRabbit = false;
		int nbrFields = rabbitSpace.getSizeX() * rabbitSpace.getSizeY();
		int i = 0;

		Vector<Point> pointVector = new Vector<Point>();

		while(i < nbrFields && addedRabbit == false) {
			int x = (int) (Math.random()*(rabbitSpace.getSizeX()));
			int y = (int) (Math.random()*(rabbitSpace.getSizeY()));
			Point point = new Point(x, y);	
			if(false == pointVector.contains(point)) {
				if(false == checkIfRabbitOn(x, y)) {
					newRabbit.setPosXY(x, y);
					placeRabbitIn(newRabbit);
					addedRabbit = true;
				} else {
					i++;
					pointVector.add(point);
				}
			}
		}
		return addedRabbit;
	}

	/**
	 * Place rabbit at a certain position
	 * @param rabbit
	 */
	public void placeRabbitIn(RabbitsGrassSimulationAgent rabbit) {
		int x = rabbit.getPositionX();
		int y = rabbit.getPositionY();	
		rabbitSpace.putObjectAt(x, y, rabbit);
	}

	/**
	 * Check if a coordinate x is still in the grid or outside
	 * @param x
	 * @return the corrected coordinate x
	 */
	public int checkBoundryX(int x) {
		int max = rabbitSpace.getSizeX();
		if(x >= max) {
			return x - max;
		} else if (x < 0) {
			return x + max;
		} else {
			return x;
		}
	}

	/**
	 * Check if a coordinate y is still in the grid or outside
	 * @param y
	 * @return the corrected coordinate y
	 */
	public int checkBoundryY(int y) {
		int max = rabbitSpace.getSizeY();

		if(y >= max) {
			return y - 1;
		} else if (y < 0) {
			return y + 1;
		} else {
			return y;
		}
	}

	/**
	 * Counts the total number of rabbits in the system.
	 * @return nbr of rabbits
	 */
	public int RabbitPopulation(){
		int population= 0;
		for (int i = 0; i < rabbitSpace.getSizeX(); i++) {
			for (int j = 0; j < rabbitSpace.getSizeY(); j++) {
				if(rabbitSpace.getObjectAt(i, j) != null) {
					population++;
				}
			}
		}
		return population;
	}

	/**
	 * @return Object2DGrid grassSpace
	 */
	public Object2DGrid getCurrentGrassSpace() {
		return grassSpace;
	}

	/**
	 * @return Object2DGrid rabbitSpace
	 */
	public Object2DGrid getCurrentRabbitSpace() {
		return rabbitSpace;
	}
}
