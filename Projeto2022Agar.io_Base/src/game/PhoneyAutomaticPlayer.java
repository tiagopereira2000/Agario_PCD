package game;

import environment.Cell;
import environment.Coordinate;
import environment.Direction;

import java.io.Serializable;

public class PhoneyAutomaticPlayer extends Player implements Serializable {
	private Thread myThread;
	public PhoneyAutomaticPlayer(int id, Game game, byte strength) {
		super(id, game, strength);
		init();
	}

	public class AutonomousThread extends Thread implements Serializable{
		@Override
		public void run() {
			try {
				addPlayerToGame();
				while(isActive()) {
					Thread.sleep((int)originalStrength*Game.REFRESH_INTERVAL);
					randomMovement();
				}
			} catch (InterruptedException e) {
				System.err.println("autonomous bot thread interrupted");
			}
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();
		myThread.interrupt();
	}

	private void init() {
		myThread = new AutonomousThread();
		myThread.start();
	}

	@Override
	public void addPlayerToGame() {
		super.addPlayerToGame();
		game.addBotToGame(this);
	}

	@Override
	public boolean isHumanPlayer() {
		return false;
	}

	public Player getPlayer(){
		return this;
	}


	public void randomMovement() throws InterruptedException {
		Cell cell = getCurrentCell();
		Coordinate pos = cell.getPosition();
		int dir = (int)(Math.random()*4);
		//Utilização da função math.random() com o intervalo de valores de 1 a 4 para aleatoriamente escolher a direção para qual irá avançar
		if(dir==0 && pos.x < Game.DIMX - 1) {
			//avança para a direita
			Coordinate newpos = pos.translate(Direction.RIGHT.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
		else if(dir==1 && pos.x>0) {
			//avança para a esquerda
			Coordinate newpos = pos.translate(Direction.LEFT.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
		else if(dir==2 && pos.y>0) {
			//avança para cima
			Coordinate newpos = pos.translate(Direction.UP.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
		else if(dir==3 && pos.y<Game.DIMY - 1 ) {
			//avança para baixo
			Coordinate newpos = pos.translate(Direction.DOWN.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
		else Thread.sleep(2000);
	}
}

