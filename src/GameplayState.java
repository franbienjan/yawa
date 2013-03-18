import java.net.Socket;
import java.util.Random;

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
				// 
				//
				
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
        START_GAME_STATE, UPDATE_LOCATIONS_STATE, CHIP_SELECTION_STATE
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
    boolean waitScreen = false;							//waiting screen
    
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
    	for (int i = 0, space = 0; i < 3; i++, space += 10) {
    		if (chipSelected[i] != null) {
    			if (chipSelected[i].isUsed) {
    				switch(chipSelected[i].getChipType()) {
    				case 0:
    					getFromSpriteSheet = sprites_chips.getSprite(0, 0);
    					break;
    				case 1:
    					getFromSpriteSheet = sprites_chips.getSprite(0, 1);
    					break;
    				case 2:
    					getFromSpriteSheet = sprites_chips.getSprite(1, 0);
    					break;
    				case 3:
    					getFromSpriteSheet = sprites_chips.getSprite(1, 1);
    					break;
    				case 4:
    					getFromSpriteSheet = sprites_chips.getSprite(2, 0);
    					break;
    				case 5:
    					getFromSpriteSheet = sprites_chips.getSprite(2, 1);
    					break;
    			}
    			
    			getFromSpriteSheet.draw(navi[playID].posX + space, navi[playID].posY - 40);
    			}
    		}
    	}
    	
    	//attacks
    	
    	//during chip selection state
    	if (chipSelectView) { 		
    		chipSelectScrn.draw();
    		
    		int chipCoor;
    		
    		if (chipsLeft >= 0) {	
    			if (chipScrnPtr != 5 && !chipAvailable[chipScrnPtr].isUsed) {
	    			//display active chip in screen
	    			switch (chipAvailable[chipScrnPtr].getChipType()) {
		    			case 0: 
		    				getFromSpriteSheet = sprites_chipscreen.getSprite(0, 0);
		    				break;
		    			case 1:
			    			getFromSpriteSheet = sprites_chipscreen.getSprite(0, 1);
			    			break;
		    			case 2:
			    			getFromSpriteSheet = sprites_chipscreen.getSprite(1, 0);
			    			break;
		    			case 3:
			    			getFromSpriteSheet = sprites_chipscreen.getSprite(1, 1);
			    			break;
		    			case 4:
			    			getFromSpriteSheet = sprites_chipscreen.getSprite(2, 0);
			    			break;
		    			case 5:
			    			getFromSpriteSheet = sprites_chipscreen.getSprite(2, 1);
			    			break;   				
	    			}
    			} else {
    				getFromSpriteSheet = sprites_chipscreen.getSprite(3, 0);
    			}
    			
    			getFromSpriteSheet.draw(21, 23);
    			chipCoor = 25;
    			
	    		//load available chips one by one
	    		for (int i = 0; i < 5; i++) {
	    			
	    			if (!chipAvailable[i].isUsed) {
		    			switch (chipAvailable[i].getChipType()) {
		    				case 0: 
		    					getFromSpriteSheet = sprites_chips.getSprite(0, 0);
		    					break;
		    				case 1:
			    				getFromSpriteSheet = sprites_chips.getSprite(0, 1);
			    				break;
		    				case 2:
			    				getFromSpriteSheet = sprites_chips.getSprite(1, 0);
			    				break;
		    				case 3:
			    				getFromSpriteSheet = sprites_chips.getSprite(1, 1);
			    				break;
		    				case 4:
			    				getFromSpriteSheet = sprites_chips.getSprite(2, 0);
			    				break;
		    				case 5:
			    				getFromSpriteSheet = sprites_chips.getSprite(2, 1);
			    				break;
		    			}
	    			
	    				getFromSpriteSheet.draw(chipCoor, 270);
	    				g.drawString("" + chipAvailable[i].getChipLetter(), chipCoor+15, 305);
	    			}
	    			
	    			chipCoor += 40;
	    		}
	    		
	    		//display active cursor    	
	    		if (chipScrnPtr != 5)
	    			animChipCursor.draw(40*chipScrnPtr + 18, 262);
	    		else
	    			animChipCursor.draw(220, 284, 70, 60);
	    		
	    		animChipCursor.start();
	    		
	    		//load selected chips one by one
	    		chipCoor = 65;
	    		
	    		for (int i = 0; i < 3; i++) {
	    			
	    			if (chipSelected[i].isUsed) {
		    			switch(chipSelected[i].getChipType()) {
		    				case 0:
		    					getFromSpriteSheet = sprites_chips.getSprite(0, 0);
		    					break;
		    				case 1:
		    					getFromSpriteSheet = sprites_chips.getSprite(0, 1);
		    					break;
		    				case 2:
		    					getFromSpriteSheet = sprites_chips.getSprite(1, 0);
		    					break;
		    				case 3:
		    					getFromSpriteSheet = sprites_chips.getSprite(1, 1);
		    					break;
		    				case 4:
		    					getFromSpriteSheet = sprites_chips.getSprite(2, 0);
		    					break;
		    				case 5:
		    					getFromSpriteSheet = sprites_chips.getSprite(2, 1);
		    					break;
		    			}
		    			
		    			getFromSpriteSheet.draw(247, chipCoor);
		    			chipCoor += 40;
	    			}
	    		
	    		}
    		}
    	}
    	
    	//waiting stage
    	if (waitScreen || !rcv.start) {
    		waitingScrn.draw();
    	}
    	
    }
  
    public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
    	
    	if (rcv.ready && rcv.start) { 
    		
	    	if (rcv.isChipScrn && !chipSelectView && !waitScreen) {
	    		chipBattleReset();									//empty the battle registers
	    		chipSelectLoader();									//load 5 chips from folder
	    		currentState = STATES.CHIP_SELECTION_STATE;
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
	    			
		    	//check collisions
		    	//do something collisions
		    	//final update on locations
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
	   	} else if (input.isKeyPressed(Input.KEY_SPACE)){
	   		//do something == launch chip
	   	}
	   	
	   	//send message
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
    			
    		} else if (!chipAvailable[chipScrnPtr].isUsed) {		//choose chip
	    		for (int i = 0; i < 3; i++) {
	    			if (!chipSelected[i].isUsed) {
	    				chipSelected[i] = chipAvailable[chipScrnPtr];
	    				chipFolder[chipSelected[i].getChipId()].isUsed = true;
	    				chipAvailable[chipScrnPtr].isUsed = true;
	    				chipSelected[i].isUsed = true;
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
    		if (chipFolder[i].isUsed == false) {
    			chipAvailable[loc] = chipFolder[i];					//copy from chipFolder list
    			loc++;
    		}
    		i++;
    	} while (loc < 5 && i < 30);
    	
    	if (loc < 5) {
    		System.out.println("Less than five yung chips OMGEH");
    		//for (; loc < 5; loc++) {
    		//	chipAvailable[loc].isUsed = true;					//para later on ma-ignore yung chip		
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

OPTIMIZATION
--> yung sprite sheet, one dimension lang. Para pagcall, pwedeng:
	getFromSprite(something, x, chipType()); nalang or something :D
 
Pag last chip na, dapat yung ChipSelector screen nakatanga lang
	- may error array out of bounds dito
	- may error ata in this case: CUBE SWORD SWORD, kinuha ko yung 1st sword pero bumalik din sa second run

Implement undo-er for chips selected in battle register during chipSelectorScrn
Implement dim chips that are not compatible with selected
Put HP bar under megaman

Kung may time pa lagyan natin ng asterisk (wild card chips)

HP bar on top left of the screen

*/