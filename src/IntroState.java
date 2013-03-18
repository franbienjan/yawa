import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class IntroState extends BasicGameState {

	int stateID = -1;
	Image background = null;
	
	boolean ready = false;
	
	IntroState( int stateID ) {
		this.stateID = stateID;
	}
	  
	public int getID() {
		return stateID;
	}

	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
		
		//basic images
		background = new Image("images/back.png");
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
		
		background.draw();
		g.drawString("Press Enter to begin game!", 350, 300);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
		
		Input input = gc.getInput();
		
		if (input.isKeyPressed(Input.KEY_ENTER)) {
			sb.enterState(BattleNetworkGame.GAMEPLAYSTATE);
		}
	}

}
