package game;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import environment.Cell;
import environment.Coordinate;

/**
 *Nesta classse vão ser inicializados a maior parte da informação sobre o jogo em si.
 *No inicio da classe estão formatadas as variáveis que vão fazer parte da instância game,
 *onde estão comentadas nas suas respetivas linhas o que significam. 
 * @author ASUS-Tiago
 *
 */

public class Game extends Observable implements Serializable {

	public static final int DIMY = 20;	//dimensão do exio X do tabuleiro
	public static final int DIMX = 20;	//dimensao do eixo Y do tabuleiro
	private static final int NUM_PLAYERS = 90;	//numero maximo de jogadores no jogo
	public static final int NUM_FINISHED_PLAYERS_TO_END_GAME=3;	//Vencedores necessarios para acabar o jogo

	public static final long REFRESH_INTERVAL = 400;	//tempo de refresh
	public static final double MAX_INITIAL_STRENGTH = 3;	//força inicial maxima de cada jogador
	public static final long MAX_WAITING_TIME_FOR_MOVE = 2000;	//maximo de tempo espera por jogada
	public static final long INITIAL_WAITING_TIME = 10000;	//tempo inicial de espera para o jogo dar comeco
	
	public ArrayList<Player> winners;	//Lista com jogadores que já ganharam o jogo
	public ArrayList<PhoneyAutomaticPlayer> botPlayers;	//Lista com jogadores ativos no jogo
	public ArrayList<PhoneyHumanPlayer> serverPlayers;
	
	protected Cell[][] board;	//matriz celular do tabuleiro
	public WinnerCounter wc;	//barreira

	public boolean isGameOver;

	public Game() {
		board = new Cell[Game.DIMX][Game.DIMY];
		for (int x = 0; x < Game.DIMX; x++) 
			for (int y = 0; y < Game.DIMY; y++) 
				board[x][y] = new Cell(new Coordinate(x, y),this);
		wc = new WinnerCounter(this);
		new Thread(() -> {
			try {
				wc.await();
				stop();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}).start();
		winners = new ArrayList<Player>();
		botPlayers = new ArrayList<PhoneyAutomaticPlayer>();
		serverPlayers = new ArrayList<>();
		isGameOver=false;
	}
	
	public Cell getCell(Coordinate at) {
		return board[at.x][at.y];
	}

	public void setBoard(Cell[][] board) {
		this.board = board;
	}

	public Cell[][] getBoard() {
		return board;
	}

	/**
	 * Updates GUI. Should be called anytime the game state changes
	 */
	public void notifyChange() {
		setChanged();
		notifyObservers();
	}
	
	public void addWinner(Player p) {
		synchronized (winners) {
			wc.decrementar();
			winners.add(p);
		}
	}
	
	public void addBotToGame(PhoneyAutomaticPlayer p) {
			botPlayers.add(p);
	}

	public void addGamerToGame(PhoneyHumanPlayer p){
		serverPlayers.add(p);
	}
	
	public void removePlayer(PhoneyAutomaticPlayer p) {
		botPlayers.remove(p);
	}
	
	//metodo que será utilizado para quando um player avançar contra outro, vence o player que tiver mais "força"
	//Se tiverem ambos igual força é escolhido aleatoriamente um player
	public Player confrontation(Player p1, Player p2) {
		byte p1str = p1.getCurrentStrength();
		byte p2str = p2.getCurrentStrength();
		if(p1str > p2str) {
			p1.addStrength(p2str);
			p2.died();
//			System.out.println("O vencedor foi " + p1.toString());
			return p1;
		}
		else if(p1str < p2str) {
			p2.addStrength(p1str);
			p1.died();
//			System.out.println("O vencedor foi " + p2.toString());
			return p2;
		}
		else{
			int rng = (int) (Math.random()*2);
			if(rng == 0) {
				p1.addStrength(p2str);
				p2.died();
//				System.out.println("O vencedor foi " + p1.toString());
				return p1;
			}
			if(rng == 1) {
				p2.addStrength(p1str);
				p1.died();
//				System.out.println("O vencedor foi " + p2.toString());
				return p2;
			}
		}
		return null;
	}

	public Cell getRandomCell() {
		Cell newCell=getCell(new Coordinate((int)(Math.random()*Game.DIMX),(int)(Math.random()*Game.DIMY)));
		return newCell; 
	}

	public void stop() throws InterruptedException{
		isGameOver=true;
		for(Player p: botPlayers)
			if(p.isActive()) p.deactivate();
		botPlayers.clear();
		System.out.println("GAME OVER");
	}
	

}
























