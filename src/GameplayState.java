import java.net.Socket;
import java.util.Random;
import java.lang.Math;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
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
		boolean isWin;							//if winner
		
		public rcvthread (Socket socket, Navi[] navi) {
			this.conn = new MyConnection(socket);
			this.navi = navi;
			this.ready = false;
			this.start = false;
			this.wait = false;
			this.isChipScrn = false;
			this.isWin = false;
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
				// (5) CHIP <player_ID_caster> <chip_type> <negative/positive> <damage>
				// (6) WIN <player_ID>
				
				if (msg.startsWith("START")) {
					this.start = true;
				} else if (msg.startsWith("MOVE ")) {
					int temp = msg.charAt(5) - 48;
					int dir = msg.charAt(7) - 48;

					switch(dir) {
						case 0:
							navi[temp].setX(navi[temp].getX() - 1);
							break;
						case 1:
							navi[temp].setX(navi[temp].getX() + 1);
							break;
						case 2:
							navi[temp].setY(navi[temp].getY() - 1);
							break;
						case 3:
							navi[temp].setY(navi[temp].getY() + 1);
							break;
					}
					
					navi[temp].resetAll();
					
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
					navi[1-temp].is_damaged = true;
					
					if (damage == 1) {
						navi[1-temp].setHP(navi[1-temp].hp - 1);
					}
					
				} else if (msg.startsWith("CHIP ")) {
					int chiptype = msg.charAt(5) - 48;
					int temp = msg.charAt(7) - 48;
					int damtype = msg.charAt(9) - 48;
					int damage = Integer.parseInt(msg.substring(11));
					
					switch (chiptype) {
						case 0:	//bomb
							break;
						case 1: //recov80
							navi[temp].is_energized = true;
							break;
						case 2: //sword
							navi[1-temp].is_sabunot = true;
							break;
						case 3: //wide sword
							navi[1-temp].is_sampal = true;
							break;
						case 4: //recov30
							navi[temp].is_energized = true;
							break;
						case 5:	//cannon
							navi[1-temp].is_tapon = true;
							break;
					}
					if (damtype == 1) {
						damage *= -1;
						navi[temp].is_damaged = true;
					} else if (damtype == 0) {
						damtype = 1;
					}
					
					navi[temp].setHP(navi[temp].getHP() + (damage * damtype));
				} else if (msg.startsWith("WIN ")) {
					int temp = msg.charAt(4) - 48;
					navi[temp].win = true;
					navi[1-temp].lose = true;
				}
			}
		}
		
	}
	
	//*****************************************************************************************
	//*****************************************************************************************
	//*****************************************************************************************
	
	//threads for client
	rcvthread rcv;
	Socket socket;
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
    private Image chipSelectScrn = null;				//chipSelector
    private Image waitingScrn = null;					//waiting screen after chips
    private Image winScrn = null;						//ending screen game over
    private Image loseScrn = null;						//ending screen game over
    
    //spritesheets
    private SpriteSheet sprites_chips = null;			//sprites of small chips
    private SpriteSheet sprites_chipscreen = null;		//sprites of big chips
    private SpriteSheet sprites_chipCursor = null;		//sprites for blinking cursor
    private SpriteSheet sprites_digits = null;			//sprites for blinking cursor
    private SpriteSheet finalscreen = null;
    private Image getFromSpriteSheet = null;			//temporary holder of images from sprites
    
    private Sound fx = null;
    private Sound ouch = null;
    private Sound slap = null;
    
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

		try {
			BattleNetworkGame bng = (BattleNetworkGame) sb;
			socket = new Socket(bng.ip, Integer.parseInt(bng.port));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		rcv = new rcvthread(socket, navi);
		rcv.start();
		
		currentState = STATES.START_GAME_STATE;
		
    }
  
    public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
    	
    	fx = new Sound("images/mars.wav");
    	ouch = new Sound("images/ouch.wav");
    	slap = new Sound("images/slap.wav");
    	
    	//basic images
    	background = new Image("images/back.png");
    	waitingScrn = new Image("images/waitingscrn.png");
    	winScrn = new Image("images/winscreen.png");
    	loseScrn = new Image("images/losescreen.png");
    	
    	//sprite sheets
    	sprites_chips = new SpriteSheet("images/sprites_chips.png", 34, 34);
    	sprites_chipscreen = new SpriteSheet("images/sprites_chipscreen.png", 164, 224);
    	sprites_chipCursor = new SpriteSheet("images/sprites_chipSelectCursor.png", 48, 48);
    	sprites_digits = new SpriteSheet("images/sprites_digits.png", 23, 38);
    	finalscreen = new SpriteSheet("images/finalscreen.png", 400, 300);
    	
    	//animations
    	animChipCursor = new Animation();
    	
		for (int i = 0; i <= 2; i++)
			animChipCursor.addFrame(sprites_chipCursor.getSprite(i, 0), 200);
		
		//music
		
    	
    	//chip select screen sub-images
    	chipSelectScrn = new Image("images/chipSelectScrn.png");
    	/*	chipSelectScrn_ok = chipSelectScrn.getSubImage(227,294,52,44);
    		chipSelectScrn_add = chipSelectScrn.getSubImage(0,0,0,0);
    	*/	
    	navi[0] = new Navi(0);
    	navi[1] = new Navi(1);
    	
    	fx.play();
    	fx.loop();
    }
  
    public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
    	//basic things
    	background.draw();
    	//field.draw();
    	
    	//navis
    	g.drawAnimation(navi[0].getAnimation(), navi[0].getPosX(), navi[0].getPosY());
    	g.drawAnimation(navi[1].getAnimation(), navi[1].getPosX(), navi[1].getPosY());
    	
    	//Print HP LEVELS
    	int hp1 = navi[0].getHP();
    	int hp2 = navi[1].getHP();
    	int looper = 179;
    	
    	while (hp1 > 0) {
    		(sprites_digits.getSprite(hp1%10, 0)).draw(looper, 37);
    		looper -= 23;
    		hp1 /= 10;
    	}
    	
    	looper = 684;
    	while (hp2 > 0) {
    		(sprites_digits.getSprite(hp2%10, 0)).draw(looper, 37);
    		looper -= 23;
    		hp2 /= 10;
    	}
    	
    	//g.drawString(navi[0].getX() + " " + navi[0].getY() + " " + navi[0].getHP(), navi[0].getPosX(), navi[0].getPosY() + 50);
    	//g.drawString(navi[1].getX() + " " + navi[1].getY() + " " + navi[1].getHP(), navi[1].getPosX(), navi[1].getPosY() + 50);
    	
    	//draw and display chips in register
    	for (int i = 2, space = 0; i >= 0; i--, space += 10) {
    		if (chipSelected[i] != null) {
    			if (chipSelected[i].isEmpty && !chipSelected[i].isUsed) {
    				getFromSpriteSheet = sprites_chips.getSprite(0, chipSelected[i].getChipType());
    	  			getFromSpriteSheet.draw(navi[playID].getPosX() + space, navi[playID].getPosY() - 40);
    			}
    		}
    	}
    	
    	//animation attacks
    	
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
    		if (navi[playID].win) {
    			winScrn.draw();
    			(finalscreen.getSprite(playID, 0)).draw(192,60);
    		} else {
    			loseScrn.draw();
    			(finalscreen.getSprite(playID, 1)).draw(192,60);
    		}
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
	    		if (navi[0].hp <= 0)
	    			rcv.conn.sendMessage("WIN 1");
	    		else if (navi[1].hp <= 0)
	    			rcv.conn.sendMessage("WIN 0");
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
    	//2 - Sampal x6
    	//3 - Wide Sword x6
    	//4 - +30 HP x5
    	//5 - Cannon x6
    	//**************************
    	
    	//damages array
    	int[] hpdamage = {-50, 80, -80, -60, 30, -50};
    	
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
    	//DIRECTION / MOVEMENTS
    	//0 - Left
    	//1 - Right
    	//2 - Up
    	//3 - Down
    	
    	//INPUT FROM PLAYER ----------------------------
    	Input input = gc.getInput();
   	 	playID = rcv.id;
   	 	
	   	if (input.isKeyPressed(Input.KEY_A)) {
	   		if (navi[playID].getX() > 0) {
	   			rcv.conn.sendMessage("MOVE " + playID + " " + 0);
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_D)) {
	   		if (navi[playID].getX() < 2) {
	   			rcv.conn.sendMessage("MOVE " + playID + " " + 1);
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_W)) {
	   		if (navi[playID].getY() > 0) {
	   			rcv.conn.sendMessage("MOVE " + playID + " " + 2);
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_S)) {
	   		if (navi[playID].getY() < 2) {
	   			rcv.conn.sendMessage("MOVE " + playID + " " + 3);
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_G)) {
	   		//fire bullet
	   		navi[playID].is_bullet = true;
	   		if (navi[playID].getY() == navi[1-playID].getY())
	   			rcv.conn.sendMessage("FIRE " + playID + " " + 1); 
	   	} else if (input.isKeyPressed(Input.KEY_SPACE)){
	   		//launch chip (examine chip type, then send corresponding action)
	   		for (int i = 0; i < 3; i++) {
	   			if (chipSelected[i] != null) {
	   				if (!chipSelected[i].isUsed) {

	   					String msg = "";
	   					int chiptype = chipSelected[i].getChipType();
	   					//do switch here
	   					switch (chiptype) {	
	   						case 0:	//Bomb
	   							if (navi[playID].getX() == navi[1-playID].getX()) {
	   								msg = "CHIP " + chiptype + " " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   								ouch.play();
	   							}
	   							break;
	   						case 1: //Recov80
	   							msg = "CHIP " + chiptype + " " + playID + " 0 " + chipSelected[i].getHpDamage();
	   							break;
	   						case 2: //Sabunot
	   							if ((Math.abs(navi[playID].getX() - navi[1-playID].getX()) == 2) && (navi[playID].getY() == navi[1-playID].getY())) {
	   								msg = "CHIP " + chiptype + " " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   								ouch.play();
	   							}
	   							break;
	   						case 3: //Sampal
	   							if (Math.abs(navi[playID].getX() - navi[1-playID].getX()) == 2) {
	   								msg = "CHIP " + chiptype + " " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   								slap.play();
	   							}
	   							break;
	   						case 4: //Recov30
	   							msg = "CHIP " + chiptype + " " + playID + " 0 " + chipSelected[i].getHpDamage();
	   							break;
	   						case 5: //Cannon
	   							if (navi[playID].getY() == navi[1-playID].getY()) {
	   								msg = "CHIP " + chiptype + " " + (1-playID) + " 1 " + (chipSelected[i].getHpDamage()) * -1;
	   								ouch.play();
	   							}
	   							break;
	   					}
	   					
	   					chipSelected[i].isUsed = true;
	   					if (msg != "")
	   						rcv.conn.sendMessage(msg);
	   					break;
	   				}
	   			}
	   		}
	   		
	   	} else if (!input.isKeyPressed(Input.KEY_G)) {
	   		navi[playID].is_bullet = false;
	   	}
	   	
	   	
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
    			rcv.conn.sendMessage("READYCHIP " + rcv.id);
    			
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

WIDE SWORD
--> Ay sword palang

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