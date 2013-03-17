import java.util.Random;

public class Navi {

	int tag;									//0 or 1 (player type)
	int x, y;									//grid coordinate of Navi
	float posX, posY;							//pixel coordinates of Navi
	int hp;										//hp of navi
	
	//with regards to chips
	int chipsleft;								//number of chips left
	int[] chipTally = {6, 1, 6, 6, 5, 6};		//there are 5 chips per 6 types QUANTITY (tally only)
	Chip[] chipFolder = new Chip[30];			//list of presented chips ready
	Chip[] chipAvailable = new Chip[5];			//5 chips that are in the choosing list
	Chip[] chipSelected = new Chip[3];			//3 chips that are ready for battle phase
	
	public Navi (int tag) {
		//initialization
		x = 1;
		y = 1;
		hp = 150;
		chipsleft = 30;
		
		this.tag = tag;							//indicate player type
		
		if (tag == 0)							//Player 1 starting x-coordinate
			posX = 170;
		else if (tag == 1)
			posX = 570;							//Player 2 starting x-coordinate
		
		posY = 400;								//Both players starting y-coordinate
	}
	
	public void setHP (int hp) {
		this.hp = hp;
	}
	
	public int getHP () {
		return hp;
	}
	
	public float getPosX() {
		return posX;
	}
	
	public float getPosY() {
		return posY;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(float x) {
		this.x = (int) x;
	}
	
	public void setY(float y) {
		this.y = (int) y;
	}
	
	public void setPosX (float posX) {
		this.posX = posX;
	}
	
	public void setPosY (float posY) {
		this.posY = posY;
	}
	
}
