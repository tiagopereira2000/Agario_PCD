package environment;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import game.Game;
import game.PhoneyAutomaticPlayer;
import game.Player;
/**
 * @author Tiago, Gonçalo
 */

public class Cell implements Serializable {

	private Coordinate position;
	private Game game;
	private Player player = null;
	private Lock lock = new ReentrantLock();
	public Condition cellLivre = lock.newCondition();
	public Condition cellBlocked = lock.newCondition();

	public Cell(Coordinate position,Game g) {
		super();
		this.position = position;
		this.game=g;
	}

	public Coordinate getPosition() {
		return position;
	}

	public boolean isOcupied() {
		return player != null;
	}

	public Player getPlayer() {
		return player;
	}
	
	//Método criado para remover player da celula
    public void removePlayer() {
        lock.lock();
        try {
            player = null;
            cellLivre.signalAll();
        }finally {lock.unlock();}
    }
	
	/**
	 * Tenta mover o player para uma nova cell. Se esta se encontrar ocupada por um player ativo é aplicado o método de confronto
	 * entre o jogador que quer ocupar a cell e aquele que já lá se encontra,
	 * quem tiver mais força é desativado do jogo.
	 * Se estiver ocupado por um player não ativo espera 2s cancela o movimento.
	 * Caso a célula não esteja ocupada ficará preenchida por esse jogador.
	 * @param player
	 * @throws InterruptedException
	 * 
	 * Mexi no método game.confrotation() por isso não precisa os dois blocos if comentados
	 */
	public void movePlayerTo(Player player) {
		Cell prevCell = player.getCurrentCell();
		prevCell.lock.lock();
		lock.lock();
		try {
			if(isOcupied()){	
				if(this.player.isActive()) {
					itsBattlefield(this.player, player);
				}else if(player instanceof PhoneyAutomaticPlayer){ //Se o player nao estiver activo
					new Thread(){
						@Override
						public void run() {
							super.run();
							try {
								Thread.sleep(2000); //vou dormir dois segundos há espera que este gajo saia
							} catch (InterruptedException e) {
								System.out.println("thread interrompida");
							} finally {
								if(isBlocked()){ //ah este está morto vou avisar que não posso vir para aqui
									cellBlocked.notifyAll();
								}
							}
						}
					}.start();
//					System.out.println("Desbloqueio");
				}
			}else{ // Se a cell nao estiver ocupada
				doChanges(player);
			}
			game.notifyChange();
		}catch (InterruptedException e){
			//
		} finally {
			lock.unlock();
			prevCell.lock.unlock();
		}
	}

	void itsBattlefield(Player p1, Player p2){
		Player battleWinner = game.confrontation(p1, p2);
		if(battleWinner.getCurrentStrength() >= 10) {
			battleWinner.won();
		}
	}

	void doChanges(Player player) throws InterruptedException {
		player.getCurrentCell().removePlayer();
		this.player = player;
		player.changeCurrentCell(this);
	}

	/**
	 * 	 Should not be used like this in the initial state: cell might be occupied, must coordinate this operation
	 * Confirmar com a professora sobre o funcionamento
	 * Método para adicionar um novo player numa cell
	 * @param player
	 * @throws InterruptedException
	 */
	public void setPlayer(Player player) throws InterruptedException {
		lock.lock();
		try {
			while(isOcupied()) {
				cellLivre.await();
			}
			this.player = player;
		} finally {lock.unlock();}
	}

	public void addPlayer(Player player){
		lock.lock();
		try {
			if(isBlocked()){
				Thread.sleep(2000);
				cellBlocked.notifyAll();
			}
			else this.player = player;
		} catch (InterruptedException e) {
			System.out.println("sono interrompido ao adicionar jogador");
		} finally {
			lock.unlock();
		}
	}

	public boolean isBlocked(){
		if(isOcupied()){
			return !player.isActive();
		}
		return false;
	}


}
