import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class IntroState extends BasicGameState {

	int stateID = -1;
	Image background = null;
	TextField porttxt, iptxt;
	
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
		iptxt = new TextField(gc, gc.getDefaultFont(), 300, 350, 200, 30);
		porttxt = new TextField(gc, gc.getDefaultFont(), 500, 350, 200, 30);
		iptxt.setFocus(true);
		
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
		
		background.draw();
		g.drawString("Press Enter to begin game!", 350, 300);
		iptxt.render(gc, g);
		porttxt.render(gc, g);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
		
		Input input = gc.getInput();
		
		if (input.isKeyPressed(Input.KEY_ENTER)) {
			BattleNetworkGame game = (BattleNetworkGame) sb;
			game.ip = iptxt.getText();
			game.port = porttxt.getText();
			sb.enterState(BattleNetworkGame.GAMEPLAYSTATE);
		} else if (input.isKeyPressed(Input.KEY_TAB)) {
			if (iptxt.hasFocus()) {
				porttxt.setFocus(true);
			} else {
				iptxt.setFocus(true);
			}
			
		}
	}

}
