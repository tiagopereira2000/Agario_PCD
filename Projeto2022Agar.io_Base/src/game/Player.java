package game;



import environment.Cell;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;

/**
 * Represents a player.
 * @author luismota
 *
 */
public abstract class Player implements Comparable<Player>, Serializable {


	protected  Game game;

	private int id;
	
	private Cell currentCell;

	private byte currentStrength;
	protected byte originalStrength;
	
	//Atributo de player que indica se ainda se encontra em jogo ou não, por exemplo um jogador que tenha ganho ou perdido ja n se encontra em jogo logo
	//isActive = false , caso contrário isActive = true
	private boolean isActive;
	
	//Atributo que indica se é um dos vencedores 
	private boolean isWinner;

	public Cell getCurrentCell() {
		return currentCell;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public boolean isWinner() {
		return isWinner;
	}

	public Player(int id, Game game, byte strength) {
		super();
		this.id = id;
		this.game=game;
		currentStrength=strength;
		originalStrength=strength;
		isActive = true;
	}

	public void died() {
		setCurrentStrength((byte)0);
		deactivate();
	}

	public void won() {
		setCurrentStrength((byte)10);
		deactivate();
		isWinner = true;
		game.addWinner(this);
	}

	public int compareTo(Player other) {
		return this.id-other.id;
	}

	//método adicionado para mudar a cell onde o player esta no momento

	public void changeCurrentCell(Cell newcell) {
		currentCell = newcell;
	}

	public abstract boolean isHumanPlayer();
	
	@Override
	public String toString() {
		return "Player [id=" + id + ", currentStrength=" + currentStrength + ", getCurrentCell()=" + getCurrentCell()
		+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public byte getCurrentStrength() {
		return currentStrength;
	}
	
	public void setCurrentStrength(byte val) {
		currentStrength = val;
	}
	
	public void addStrength(byte val) {
		val += currentStrength;
		setCurrentStrength(val);
	}

	public int getIdentification() {
		return id;
	}

	public void addPlayerToGame() {
		AtomicReference<Cell> randomCell = new AtomicReference<>();
		Runnable auxRun = () -> {
			try{
				randomCell.set(game.getRandomCell());
				randomCell.get().addPlayer(this);
			}catch (NullPointerException exception){
				System.err.println("randomCell is null");
			}
		};
		new Thread(auxRun).start();
		try{
			randomCell.get().cellBlocked.await();
		}catch(InterruptedException e){
			//interrupted
		}finally {
			game.notifyChange();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
