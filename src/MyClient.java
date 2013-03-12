import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.Command;
import org.newdawn.slick.command.InputProviderListener;

public class MyClient extends BasicGame implements InputProviderListener {
	
	private Image megaman = null;
    private Image field = null;
    private Image board = null;
    private Image back = null;
    private Image custombar = null;
    private int hpleft = 100;
    private int x = 1;
    private int y = 1;
    float[] xcoors = {42, 170, 300};
    float[] ycoors = {320, 400, 480};
    
    public MyClient() {
        super("MyClient");
    }
    
	public void init(GameContainer container) throws SlickException {            
    	field = new Image("images/field.png");
    	megaman = new Image("images/megasprite.png");
    	board = new Image("images/board.png");
    	custombar = new Image("images/custombar.png");
    	back = new Image("images/back.png");
    }

    public void render(GameContainer container, Graphics g) {
    	back.draw(0,0);
    	field.draw(0,0);
        megaman.draw(xcoors[x], ycoors[y]);
        g.drawString("HP: " + hpleft, xcoors[x],ycoors[y]+60);
        custombar.draw(0,0);
        
        if (hpleft == 90)
        	board.draw(0,0);
    }

    public void update(GameContainer container, int delta) {
        
    	 Input input = container.getInput();
    	 
    	 if (input.isKeyPressed(Input.KEY_A)) {
    		 if (x != 0)
    			 x--;
    	 } else if (input.isKeyPressed(Input.KEY_D)) {
    		 if (x != 2)
    			 x++;
    	 }
    	 
    	 if (input.isKeyPressed(Input.KEY_W)) {
    		 if (y != 0)
    			 y--;
    	 } else if (input.isKeyPressed(Input.KEY_S)) {
    		 if (y != 2)
    			 y++;
    	 }
    	 
    	 if (input.isKeyPressed(Input.KEY_SPACE)) {
    		 //new bullet fire (starting space)
    	 }
    }
    
    public static void main(String[] argv) {
            try {
            	AppGameContainer container = new AppGameContainer(new MyClient());
                container.setDisplayMode(800,600,false);
                container.start();
            } catch (SlickException e) {
                e.printStackTrace();
            }
    }

	@Override
	public void controlPressed(Command arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controlReleased(Command arg0) {
		// TODO Auto-generated method stub
		
	}

}
