
public class Chip {

	private int chipId = -1;			//tag number of this specific chip (0-29) lang
	private int chipType = -1;			//type of chip (out of the six)
	private int chipLetter = -1;		//letter of chip (0, 1, or 2 OR A, B, and C)
	private int hpDamage = 0;			//negative kung damage, positive kung life, 0 pag wala
	public boolean isUsed = false;		//para malaman kung nagamit na yung chip or hindi pa

	public Chip (int id) {
		this.chipId = id;
	}
	
	public Chip (int id, int type, int letter, int hpdamage) {
		this.chipId = id;
		this.chipType = type;
		this.hpDamage = hpdamage;
		this.chipLetter = letter;
	}
	
	public int getChipId() {
		return this.chipId;
	}
	
	public int getChipType() {
		return this.chipType;
	}
	
	public int getHpDamage() {
		return this.hpDamage;
	}
	
	public int getChipLetter() {
		return this.chipLetter;
	}

}
