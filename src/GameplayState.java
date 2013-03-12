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
	
	//important GAME variables
	private int chipsLeft = 30;							//total number of chips
	private int[] chipTally = {6, 1, 6, 6, 5, 6};		//there are 5 chips per 6 types QUANTITY (tally only)
	private int[] chipCurrent = {-1, -1, -1, -1, -1};	//chips on hold during selection phase ID
	private int[] chipBattle = {-1, -1, -1};			//selected chips during battle TYPE
	private int HP = 150;								//your HP left in this game
    private int chipSelectTimer = 1000;    				//timer for chip screen round
    private int p1_x = 1;								//XGRID position of Player 1
    private int p1_y = 1;								//YGRID position of Player 2
    private Chip[] chipFolder = new Chip[30];			//Your Chip Folder
    private Chip[] chipAvailable = new Chip[5];			//Available Chips on display
    
	//other important variables
    int stateID = -1;
    int	chipScrnPtr = 0;								//pointer display at chipSelector
    private boolean chipSelectView = false;				//chipSelector view: true or false
    private float locX = 170;
    private float locY = 400;
    
    //game states
    private enum STATES {
        START_GAME_STATE, UPDATE_LOCATIONS_STATE, CHIP_SELECTION_STATE
    }
    
    private STATES currentState = null;
    
    //images used
    private Image background = null;					//background image of area
    private Image field = null;							//arena (red and blue)
    private Image megaman = null;						//player1 navi
    private Image custbar = null;						//custom gauge bar on top
    private Image chipSelectScrn = null;				//chipSelector
    	private Image chipSelectScrn_ok = null;			//subImage of OK screen
    	private Image chipSelectScrn_add = null;		//subImage of ADD screen
    
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
    	//clean board
    	currentState = STATES.START_GAME_STATE;
    	//hp start-ups
    	//chip start-ups
    }
  
    public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
    	
    	//basic images
    	background = new Image("images/back.png");
    	field = new Image("images/field.png");
    	megaman = new Image("images/megasprite.png");
    	custbar = new Image("images/custombar.png");  
    	
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
    }
  
    public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
    	//basic things
    	background.draw();
    	field.draw();
    	custbar.draw();
    	
    	//navis
    	megaman.draw(locX, locY);
    	
    	//attacks
    	
    	//during chip selection state
    	if (chipSelectView) { 		
    		chipSelectScrn.draw();
    		
    		int chipCoor;
    		
    		if (chipsLeft >= 0) {	
    			if (chipScrnPtr != 5 && chipCurrent[chipScrnPtr] != -1) {
	    			//display active chip in screen
	    			switch (chipFolder[chipCurrent[chipScrnPtr]].getChipType()) {
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
	    			
	    			if (chipCurrent[i] != -1) {
		    			switch (chipFolder[chipCurrent[i]].getChipType()) {
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
	    				g.drawString("" + chipFolder[chipCurrent[i]].getChipLetter(), chipCoor+15, 305);
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
	    			
	    			switch(chipBattle[i]) {
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
	    			
	    			if (chipBattle[i] != -1)
	    				getFromSpriteSheet.draw(247, chipCoor);
	    			chipCoor += 40;
	    		
	    		}
    		}
    	}
    }
  
    public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
    	if (chipSelectTimer < 0) {
    		currentState = STATES.CHIP_SELECTION_STATE;
    	}
    	
    	switch (currentState) {
    	
	    	//start game
    		case START_GAME_STATE:
    			currentState = STATES.UPDATE_LOCATIONS_STATE;
    			chipMaker();									//to make the 30 chip objects
    			break;
    			
   	    	//update locations    			
    		case UPDATE_LOCATIONS_STATE:
    			updateLocations(gc, delta);						//update locations of objects
    			break;
    		
    		//selection of chips for timeout
    		case CHIP_SELECTION_STATE:
    			if (chipSelectView == false) {					//to make sure once lang to gagawin
    				chipSelectLoader();							//load 5 chips from folder
    				chipBattleReset();							//empty the battle registers
    			}
    			chipSelectView = true;
    			chipSelectProcess(gc, delta);					//select chips from loaded registers
    			chipSelectTimer = 1000;
    			break;
    			
	    	//check collisions
	    	//do something collisions
	    	//final update on locations
    	}
    	
    	chipSelectTimer--;
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
    			System.out.println("Results: " + id + " " + randType + " " + randLetter);
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
    	
    	Input input = gc.getInput();
   	 
	   	if (input.isKeyPressed(Input.KEY_A)) {
	   		if (p1_x != 0) {
	   			p1_x--;
	   			locX -= 130;
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_D)) {
	   		if (p1_x != 2) {
	   			p1_x++;
	   			locX += 130;
	   		}
	   	}
	   	 
	   	if (input.isKeyPressed(Input.KEY_W)) {
	   		if (p1_y != 0) {
	   			p1_y--;
	   			locY -= 80;
	   		}
	   	} else if (input.isKeyPressed(Input.KEY_S)) {
	   		if (p1_y != 2) {
	   			p1_y++;
	   			locY += 80;
	   		}
	   	}
	   	
    }
    
    public void chipSelectProcess (GameContainer gc, int delta) {
    	
    	//wait for input for chip selections
    	//input can be via mouse or keyboard :D (KEYBOARD MUNA TAYO PLEASE)
    	//add for chips
    	Input input = gc.getInput();
    	
    	//MOUSE ACTIONS START
    	int mouseX = input.getMouseX();
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
    	}
    	
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
    		
    		if (chipScrnPtr == 5) {    					//exit selection screen
    			chipSelectView = false;
    			currentState = STATES.UPDATE_LOCATIONS_STATE;
    		} else {									//choose chip
	    		for (int i = 0; i < 3; i++) {
	    			if (chipBattle[i] == -1) {
	    				chipBattle[i] = chipFolder[chipCurrent[chipScrnPtr]].getChipType();
	    				//chipBattle[i] = chipAvailable[chipScrnPtr].getChipType();
	    				//chipFolder[chipAvailable[i].getChipId()].isUsed = true;
	    				chipFolder[chipCurrent[chipScrnPtr]].isUsed = true;
	    				chipCurrent[chipScrnPtr] = -1;
	    				chipAvailable[i].isUsed = true;
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
    			chipCurrent[loc] = chipFolder[i].getChipId();
    			loc++;
    		}
    		i++;
    	} while (loc < 5 && i < 30);
    	
    	if (loc < 5) {
    		System.out.println("Less than five yung chips OMGEH");
    		for (; loc < 5; loc++) {
    			chipAvailable[loc].isUsed = true;					//para iignore yung chip
    		}
    	}
    	
    }
    
    public void chipBattleReset () {
    	//reset the registers everytime we begin a new chipSelect phase
    	chipBattle[0] = -1;
    	chipBattle[1] = -1;
    	chipBattle[2] = -1;
    }
  
}

/*
***************************************************************
            ERRORS LOG (To Fix) / TO DO LIST
***************************************************************
Pag last chip na, dapat yung ChipSelector screen nakatanga lang
	- may error array out of bounds dito
	- may error ata in this case: CUBE SWORD SWORD, kinuha ko yung 1st sword pero bumalik din sa second run

Dapat talaga gawa tayo ng chipAvailable na Chip array. Magagawa natin yung
restriction of selection ng mas madali

Implement chip array current

Implement undo-er for chips selected in battle register during chipSelectorScrn
Implement dim chips that are not compatible with selected
Put HP bar under megaman

Kung may time pa lagyan natin ng asterisk (wild card chips)

HP bar on top left of the screen

*/