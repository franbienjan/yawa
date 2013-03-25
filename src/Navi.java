import java.util.Random;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

public class Navi {

	int tag;									//0 or 1 (player type)
	int x, y;									//grid coordinate of Navi
	int adjust = 0;								//for player 2 Navi adjustment
	int hp;										//hp of navi		
	boolean win, lose;							//if navi won
	Animation idle, bullet, sabunot, tapon, sampal, damage, energized;
	boolean is_bullet, is_damaged, is_idle, is_sabunot, is_sampal, is_tapon, is_energized;
	
	public Navi (int tag) throws SlickException {
		//initialization
		x = 1;
		y = 1;
		hp = 200;
		this.tag = tag;							//indicate player type
		String imgtxt = "";
		resetAll();
		
		if (tag == 0)
			imgtxt = "Marian";
		else if (tag == 1) {						//Player 2 adjustment
			adjust = 400;
			imgtxt = "Claudine";
		}
		
		this.win = false;
		this.lose = false;
		
		idle = new Animation(new SpriteSheet ("images/" + imgtxt + "_idle.png", 79, 113), 200);
		damage = new Animation(new SpriteSheet ("images/" + imgtxt + "_damage.png", 79, 113), 200);
		bullet = new Animation(new SpriteSheet ("images/" + imgtxt + "_bullet.png", 79, 113), 200);
		sampal = new Animation(new SpriteSheet ("images/" + imgtxt + "_sampal.png", 86, 113), 200);
		tapon = new Animation(new SpriteSheet ("images/" + imgtxt + "_tapon.png", 111, 144), 200);
		energized = new Animation(new SpriteSheet ("images/" + imgtxt + "_energized.png", 79, 113), 200);
		sabunot = new Animation(new SpriteSheet ("images/" + imgtxt + "_sabunot.png", 79, 113), 200);
	}
	
	public void resetAll() {
		this.is_bullet = false;
		this.is_damaged = false;
		this.is_energized = false;
		this.is_idle = false;
		this.is_sampal = false;
		this.is_tapon = false;
		this.is_sabunot = false;
	}
	
	public Animation getAnimation() {
		if (is_bullet)
			return bullet;
		else if (is_energized)
			return energized;
		else if (is_tapon)
			return tapon;
		else if (is_sampal)
			return sampal;
		else if (is_sabunot)
			return sabunot;
		else if (is_damaged)
			return damage;
		else
			return idle;
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
		return 275 + (this.y * 80);
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
