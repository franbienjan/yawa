import java.util.Random;

public class Navi {

	int tag;									//0 or 1 (player type)
	int x, y;									//grid coordinate of Navi
	int adjust = 0;								//for player 2 Navi adjustment
	int hp;										//hp of navi		
	boolean win, lose;							//if navi won
	
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
		
		if (tag == 1)							//Player 2 adjustment
			adjust = 400;
		
		this.win = false;
		this.lose = false;
			
	}
	
	public void setHP (int hp) {
		this.hp = hp;
	}
	
	public int getHP () {
		return hp;
	}
	
	public float getPosX() {
		return 40 + (this.x * 130) + adjust;
	}
	
	public float getPosY() {
		return 320 + (this.y * 80);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(float x) {
		this.x = (int) x;
		if (this.x < 0)
			this.x = 0;
		else if (this.x > 2)
			this.x = 2;
	}
	
	public void setY(float y) {
		this.y = (int) y;
		if (this.y < 0)
			this.y = 0;
		else if (this.y > 2)
			this.y = 2;
	}
	
	/*
	public void setPosX (float posX) {
		this.posX = posX;
	}
	
	public void setPosY (float posY) {
		this.posY = posY;
	}
	*/
	
}
