import java.net.Socket;
import java.util.Random;
import java.lang.Math;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class GameplayState extends BasicGameState {
	
	private static class rcvthread extends Thread {
	
		MyConnection conn;
		String msg;
		int id;
		boolean ready, start, wait;				//waiting for game to begin 
		Navi[] navi;
		boolean isChipScrn;						//if its time to display the chip screen
		
		public rcvthread (Socket socket, Navi[] navi) {
			this.conn = new MyConnection(socket);
			this.navi = navi;
			this.ready = false;
			this.start = false;
			this.wait = false;
			this.isChipScrn = false;
		}
		
		public void run() {
			
			ready = true;
			
			msg = conn.getMessage();
			
			if (msg.startsWith("TAG ")) {
				id = msg.charAt(4) - 48;
			}
			
			conn.sendMessage("READY " + id);
			
			while (true) {
				
				msg = conn.getMessage();
				System.out.println("Msg: " + msg);
				
				// ====== COMMAND LIST ======
				// (1) START
				// (2) MOVE <player_ID> <direction>
				// (3) CHIPSCREEN <mode>
				// (4) FIRE <player_ID_caster> <damage>
				// (5) CHIP <player_ID_caster> <chip_type> <negative/positive>
				// (6) 
				
				if (msg.startsWith("START")) {
					this.start = true;
				} else if (msg.startsWith("MOVE ")) {
					int temp = msg.charAt(5) - 48;
					String dir = msg.substring(7);

					switch(dir) {
						case "LEFT":
							navi[temp].setX(navi[temp].getX() - 1);
							navi[temp].setPosX(navi[temp].getPosX() - 130);
							break;
						case "RIGHT":
							navi[temp].setX(navi[temp].getX() + 1);
							navi[temp].setPosX(navi[temp].getPosX() + 130);
							break;
						case "UP":
							navi[temp].setY(navi[temp].getY() - 1);
							navi[temp].setPosY(navi[temp].getPosY() - 80);
							break;
						case "DOWN":
							navi[temp].setY(navi[temp].getY() + 1);
							navi[temp].setPosY(navi[temp].getPosY() + 80);
							break;
					}
				} else if (msg.startsWith("CHIPSCREEN")) {
					int mode = msg.charAt(11) - 48;
					if (mode == 0) {
						this.isChipScrn = true;
						this.wait = true;
					} else if (mode == 1) {
						this.isChipScrn = false;
						this.wait = false;
					}
				} else if (msg.startsWith("FIRE ")) {
					int temp = msg.charAt(5) - 48;
					int damage = msg.charAt(7) - 48;
					
					if (damage == 1) {
						navi[1-temp].setHP(navi[1-temp].hp - 1);
					}
					
				} else if (msg.startsWith("CHIP ")) {
					System.out.println("CHIP message: " + msg);
					int temp = msg.charAt(5) - 48;
					int damtype = msg.charAt(7) - 48;
					int damage = Integer.parseInt(msg.substring(9));
					
					if (damtype == 1) {
						damage *= -1;
					} else if (damtype == 0) {
						damtype = 1;
					}
					
					navi[temp].setHP(navi[temp].getHP() + (damage * damtype));
				}
			}
		}
		
	}
	
	//*****************************************************************************************
	//*****************************************************************************************
	//*****************************************************************************************
	
	//threads for client
	private rcvthread rcv;
	private MyConnection conn;
	int stateID = -1;
	
    //game states
    private enum STATES {
        START_GAME_STATE, UPDATE_LOCATIONS_STATE, CHIP_SELECTION_STATE, END_GAME_STATE
    }
    
    private STATES currentState = null;
    
	//navi array
	Navi[] navi = new Navi[2];
	
	//important GAME variables
	private int playID = -1;							//your player number (either 1 or 2)
	
	private int chipsLeft = 30;							//total number of chips
	private int[] chipTally = {6, 1, 6, 6, 5, 6};		//there are 5 chips per 6 types QUANTITY (tally only)
    private Chip[] chipFolder = new Chip[30];			//Your Chip Folder
    private Chip[] chipAvailable = new Chip[5];			//Available Chips on display
    private Chip[] chipSelected = new Chip[3];			//Selected Chips for Battle (no revert!)
    
	//other important variables
    int	chipScrnPtr = 0;								//pointer display at chipSelector
    private boolean chipSelectView = false;				//chipSelector view: true or false
    boolean waitScreen = false;							//is waiting screen?
    boolean endScreen = false;							//is endscreen?
    
    //images used
    private Image background = null;					//background image of area
    private Image field = null;							//arena (red and blue)
    private Image megaman = null;						//player1 navi
    private Image numberman = null;						//player2 navi
    private Image custbar = null;						//custom gauge bar on top
    private Image chipSelectScrn = null;				//chipSelector
    	private Image chipSelectScrn_ok = null;			//subImage of OK screen
    	private Image chipSelectScrn_add = null;		//subImage of ADD screen
    private Image waitingScrn = null;					//waiting screen after chips
    private Image endingScrn = null;					//ending screen game over
    
    //spritesheets
    private SpriteSheet sprites_chips = null;			//sprites of small chips
    private SpriteSheet sprites_chipscreen = null;		//sprites of big chips
    private SpriteSheet sprites_chipCursor = null;		//sprites for blinking cursor
    private Image getFromSpriteSheet = null;			//temporary holder of images from sprites
    
    //animations
    private Animation animChipCursor = null;			//animation for blinking cursor   
    
    //*********************************************************************************************
  
    GameplayState( int stateID ) {
       this.stateID = stateID;
    }
  
    @Override
    public int getID() {
        return stateID;
    }
    
    public void enter(GameContainer gc, StateBasedGame sb) throws SlickException {
    	super.enter(gc, sb);

    	Socket socket;
		try {
			socket = new Socket("127.0.0.1", 8888);
			rcv = new rcvthread(socket, navi);
			conn = new MyConnection(socket);				//for this thread
			rcv.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		currentState = STATES.START_GAME_STATE;		
		
    }
  
    public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
    	
    	//basic images
    	background = new Image("images/back.png");
    	field = new Image("images/field.png");
    	megaman = new Image("images/megasprite.png");
    	numberman = new Image("images/numsprite.png");
    	custbar = new Image("images/custombar.png");  
    	waitingScrn = new Image("images/waitingscrn.png");
    	endingScrn = new Image("images/gameover.png");
    	
    	//sprite sheets
    	sprites_chips = new SpriteSheet("images/sprites_chips.png", 34, 34);
    	sprites_chipscreen = new SpriteSheet("images/sprites_chipscreen.png", 164, 224);
    	sprites_chipCursor = new SpriteSheet("images/sprites_chipSelectCursor.png", 48, 48);
    	
    	//animations
    	animChipCursor = new Animation();
    	
		for (int i = 0; i <= 2; i++)
			animChipCursor.addFrame(sprites_chipCursor.getSprite(i, 0), 200);
		
		//music
		
    	
    	//chip select screen sub-images
    	chipSelectScrn = new Image("images/chipSelectScrn.png");
    		chipSelectScrn_ok = chipSelectScrn.getSubImage(227,294,52,44);
    		chipSelectScrn_add = chipSelectScrn.getSubImage(0,0,0,0);
    		
    	navi[0] = new Navi(0);
    	navi[1] = new Navi(1);
    }
  
    public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
    	//basic things
    	background.draw();
    	field.draw();
    	custbar.draw();
    	
    	//navis
    	megaman.draw(navi[0].posX, navi[0].posY);
    	numberman.draw(navi[1].posX, navi[1].posY);
    	
    	//hp levels
    	g.drawString("" + navi[0].hp, navi[0].posX, navi[0].posY + 50);
    	g.drawString("" + navi[1].hp, navi[1].posX, navi[1].posY + 50);
    	
    	//draw and display chips in register
    	for (int i = 2, space = 0; i >= 0; i--, space += 10) {
    		if (chipSelected[i] != null) {
    			if (chipSelected[i].isEmpty && !chipSelected[i].isUsed) {
    				getFromSpriteSheet = sprites_chips.getSprite(0, chipSelected[i].getChipType());
    	  			getFromSpriteSheet.draw(navi[playID].posX + space, navi[playID].posY - 40);
    			}
    		}
    	}
    	
    	//attacks
    	
    	//during chip selection state
    	if (chipSelectView) { 		
    		chipSelectScrn.draw();
    		
    		if (chipsLeft >= 0) {	
    			
				//display active chip in screen
    			if (chipScrnPtr != 5 && !chipAvailable[chipScrnPtr].isEmpty) {
    				getFromSpriteSheet = sprites_chipscreen.getSprite(0, chipAvailable[chipScrnPtr].getChipType());
    			} else {
    				getFromSpriteSheet = sprites_chipscreen.getSprite(0, 6);
    			}
    			
    			getFromSpriteSheet.draw(21, 23);
    			
	    		//load available chips one by one
	    		for (int i = 0, chipCoor = 25; i < 5; i++, chipCoor += 40) {
	    			if (!chipAvailable[i].isEmpty) {
	    				getFromSpriteSheet = sprites_chips.getSprite(0, chipAvailable[i].getChipType());
	    				getFromSpriteSheet.draw(chipCoor, 270);
	    				g.drawString("" + chipAvailable[i].getChipLetter(), chipCoor+15, 305);
	    			}
	    		}
	    		
	    		//display active cursor    	
	    		if (chipScrnPtr != 5)
	    			animChipCursor.draw(40*chipScrnPtr + 18, 262);
	    		else
	    			animChipCursor.draw(220, 284, 70, 60);
	    		
	    		animChipCursor.start();
	    		
	    		//load selected chips one by one	    		
	    		for (int i = 0, chipCoor = 65; i < 3; i++, chipCoor += 40) {
	    			if (chipSelected[i].isEmpty) {
	    				getFromSpriteSheet = sprites_chips.getSprite(0, chipSelected[i].getChipType());
		    			getFromSpriteSheet.draw(247, chipCoor);
	    			}
	    		}
    		}
    	}
    	
    	//waiting stage
    	if (waitScreen || !rcv.start) {
    		waitingScrn.draw();
    	}
    	
    	//ending screen
    	if (endScreen) {
    		endingScrn.draw();
    	}
    }
  
    public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
    	
    	if (rcv.ready && rcv.start) { 
    		
    		//first entrance to chipSelection process
	    	if (rcv.isChipScrn && !chipSelectView && !waitScreen && !endScreen) {
	    		chipBattleReset();									//empty the battle registers
	    		chipSelectLoader();									//load 5 chips from folder
	    		currentState = STATES.CHIP_SELECTION_STATE;
	    	}
	    	
	    	if (navi[0].hp <= 0 || navi[1].hp <= 0) {
	    		currentState = STATES.END_GAME_STATE;
	    	}
	    	
	    	switch (currentState) {
	    	
		    	//start game
	    		case START_GAME_STATE:
	    			chipMaker();									//to make the 30 chip objects
	    			currentState = STATES.UPDATE_LOCATIONS_STATE;
	    			break;
	    			
	   	    	//update locations    			
	    		case UPDATE_LOCATIONS_STATE:
	    			updateLocations(gc, delta);						//update locations of objects
	    			break;
	    		
	    		//selection of chips for timeout
	    		case CHIP_SELECTION_STATE:	    			
	    			if (!waitScreen) {
	    				chipSelectView = true;
	    				chipSelectProcess(gc, delta);				//select chips from loaded registers
	    			} 
	    			
	    			if (!rcv.wait && waitScreen) {
	    	    		currentState = STATES.UPDATE_LOCATIONS_STATE;
	    	    		waitScreen = false;
	    	    	}
	    			
	    			break;
	    			
		    	//end game
	    		case END_GAME_STATE:
	    			endScreen = true;
	    			
	    		
	    	}
	    	
    	}
    }
    
    public void chipMaker() {
    	//in this class we generate 30 chip objects
    	//we then assign ID's and chip data types (yung letters nga)

    	//********NOTES*************
    	//Order of Chips:
    	//0 - Bomb x6
    	//1 - +80 HP x1
    	//2 - Stone Cube x6
    	//3 - Wide Sword x6
    	//4 - +30 HP x5
    	//5 - Cannon x6
    	//**************************
    	
    	//damages array
    	int[] hpdamage = {-50, 80, 0, -80, 30, -40};
    	
    	//Letters assigned to the chips is either A, B, or C lang muna. (or 0, 1, and 2)
    	Random rand = new Random();
    	int randLetter = -1;
    	int randType = -1;
    	
    	//let's make use of the tally, shall we?
    	for (int id = 0; id < 30; id++) {
    		randLetter = rand.nextInt(3);				//randomize letter
    		randType = rand.nextInt(6);					//randomize type
    		
    		if (chipTally[randType] > 0) {				//meron pang chip type na laman
    			chipTally[randType]--;
    			//System.out.println("Results: " + id + " " + randType + " " + randLetter);
    			chipFolder[id] = new Chip(id, randType, randLetter, hpdamage[randType]);
    		} else {									//ubos na allocation for chip type
    			id--;
    		}
    	}
    	
    }
    
    public void updateLocations(GameContainer gc, int delta) {
    	//wait for input from player
    	//wait for input from opposite player
    	//update other stuff like bullets and bombs and shit
    	
    	//INPUT FROM PLAYER ----------------------------
    	Input input = gc.getInput();
    	String direction = "";
   	 	playID = rcv.id;
   	 	
	   	if (input.isKeyPressed(Input.KEY_A)) {
	   		if (navi[playID].getX() > 0) {
	   			direction = "LEFT";
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_D)) {
	   		if (navi[playID].getX() < 2) {
	   			direction = "RIGHT";
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_W)) {
	   		if (navi[playID].getY() > 0) {
	   			direction = "UP";
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_S)) {
	   		if (navi[playID].getY() < 2) {
	   			direction = "DOWN";
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_G)) {
	   		//fire bullet
	   		if (navi[playID].y == navi[1-playID].y) {
	   			conn.sendMessage("FIRE " + playID + " " + 1); 
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_SPACE)){
	   		//launch chip (examine chip type, then send corresponding action)
	   		for (int i = 0; i < 3; i++) {
	   			if (chipSelected[i] != null) {
	   				if (!chipSelected[i].isUsed) {

	   					String msg = "";
	   					
	   					//do switch here
	   					switch (chipSelected[i].getChipType()) {	
	   						case 0:	//Bomb
	   							if (navi[playID].x == navi[1-playID].x) {
	   								msg = "CHIP " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   							}
	   							break;
	   						case 1: //Recov80
	   							msg = "CHIP " + playID + " 0 " + chipSelected[i].getHpDamage();
	   							break;
	   						case 2: //RockCube
	   							msg = "CHIP " + (1-playID) + " 0 " + 0;
	   							break;
	   						case 3: //WideSwrd
	   							if (Math.abs(navi[playID].x - navi[1-playID].x) == 2) {
	   								msg = "CHIP " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   							}
	   							break;
	   						case 4: //Recov30
	   							msg = "CHIP " + playID + " 0 " + chipSelected[i].getHpDamage();
	   							break;
	   						case 5: //Cannon
	   							if (navi[playID].y == navi[1-playID].y)
	   								msg = "CHIP " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   							break;
	   					}
	   					
	   					chipSelected[i].isUsed = true;
	   					if (msg != "")
	   						conn.sendMessage(msg);
	   					break;
	   				}
	   			}
	   		}
	   		
	   	}
	   	
	   	//send message for movements
	   	if (direction != "")
	   		conn.sendMessage("MOVE " + playID + " " + direction);
	   	
	   	
    }
    
    public void chipSelectProcess (GameContainer gc, int delta) {
    	
    	//wait for input for chip selections
    	//input can be via mouse or keyboard :D (KEYBOARD MUNA TAYO PLEASE)
    	//add for chips
    	
    	//NOTE: Hindi aalis sa process na ito hanggang sa mag-exit (or click OK)
    	Input input = gc.getInput();
    	
    	//MOUSE ACTIONS START
    	/*int mouseX = input.getMouseX();
    	int mouseY = input.getMouseY();
    	boolean insideOk = false;
    	
    	if( ( mouseX >= 227 && mouseX <= 227 + chipSelectScrn_ok.getWidth()) &&
    		    ( mouseY >= 294 && mouseY <= 294 + chipSelectScrn_ok.getHeight()) ){
    		    insideOk = true;
    	}
    	
    	if (insideOk) {
    		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
    			chipSelectView = false;
    			currentState = STATES.UPDATE_LOCATIONS_STATE;
    		}
    	} */
    	
    	//KEYBOARD START
    	//move chipScrnPtr
    	if (input.isKeyPressed(Input.KEY_A)) {
    		if (chipScrnPtr != 0)
    			chipScrnPtr--;
    	} else if (input.isKeyPressed(Input.KEY_D)) {
    		if (chipScrnPtr != 5)
    			chipScrnPtr++;
    	}
    	
    	//select chip!
    	if (input.isKeyPressed(Input.KEY_ENTER)) {
    		
    		if (chipScrnPtr == 5) {    								//exit selection screen
    			chipSelectView = false;
    			waitScreen = true;
    			conn.sendMessage("READYCHIP " + rcv.id);
    			
    		} else if (!chipAvailable[chipScrnPtr].isEmpty) {		//choose chip
	    		for (int i = 0; i < 3; i++) {
	    			if (!chipSelected[i].isEmpty) {
	    				chipSelected[i] = chipAvailable[chipScrnPtr];
	    				chipFolder[chipSelected[i].getChipId()].isEmpty = true;
	    				chipAvailable[chipScrnPtr].isEmpty = true;
	    				chipSelected[i].isEmpty = true;
	    				break;
	    			}
	    		}
    		}
    	}
    	
    	
    }
    
    public void chipSelectLoader () {
    	
    	//get first five chips from our chipFolder
    	int i = 0;
    	int loc = 0;
    	
    	do {
    		if (chipFolder[i].isEmpty == false) {
    			chipAvailable[loc] = chipFolder[i];					//copy from chipFolder list
    			loc++;
    		}
    		i++;
    	} while (loc < 5 && i < 30);
    	
    	if (loc < 5) {
    		System.out.println("Less than five yung chips OMGEH");
    		//for (; loc < 5; loc++) {
    		//	chipAvailable[loc].isEmpty = true;					//para later on ma-ignore yung chip		
    		//}
    	}
    	
    }
    
    public void chipBattleReset () {
    	//reset the registers every time we begin a new chipSelect phase
    	for (int i = 0; i < 3; i++)
    		chipSelected[i] = new Chip (-1, -1, -1, 0);
    }
  
}

/*
***************************************************************
            ERRORS LOG (To Fix) / TO DO LIST
***************************************************************
TIMER TASK
--> palitan natin yung loop function into a thread function
--> at saka dapat magpause din whenever chip selection screen
 
CHIP SELECTION ERROR
--> pag 3 chips ang pinili ng both players, error D:

Pag last chip na, dapat yung ChipSelector screen nakatanga lang
	- may error array out of bounds dito
	- may error ata in this case: CUBE SWORD SWORD, kinuha ko yung 1st sword pero bumalik din sa second run

Implement undo-er for chips selected in battle register during chipSelectorScrn
Implement dim chips that are not compatible with selected
Put HP bar under megaman

Kung may time pa lagyan natin ng asterisk (wild card chips)

HP bar on top left of the screen

*/