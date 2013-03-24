import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class BattleNetworkGame extends StateBasedGame {
	
	public static final int MAINMENUSTATE = 0;
	public static final int GAMEPLAYSTATE = 1;
	public static final int INTROSTATE = 2;
	
	String port, ip;
	
	public BattleNetworkGame() {
		super("Battle Network");
	}

	public static void main(String[] args) throws SlickException {
		// TODO Auto-generated method stub
		AppGameContainer app = new AppGameContainer(new BattleNetworkGame());
		app.setDisplayMode(800, 600, false);
		app.setMaximumLogicUpdateInterval(31);
		app.setMinimumLogicUpdateInterval(16);
		app.start();
	}
	
	public void initStatesList(GameContainer gameContainer) throws SlickException {
	
		this.addState(new IntroState(INTROSTATE));
		this.addState(new GameplayState(GAMEPLAYSTATE));
	}

}
