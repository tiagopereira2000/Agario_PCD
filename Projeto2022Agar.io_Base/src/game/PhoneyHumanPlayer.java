package game;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import environment.Cell;
import environment.Coordinate;
import environment.Direction;
//import gui.BoardJComponent;

/**
 * Class to demonstrate a player being added to the game.
 * @author luismota
 */
public class PhoneyHumanPlayer extends Player implements Serializable {
	

	public PhoneyHumanPlayer(int id, Game game, byte strength) throws InterruptedException {
		super(id, game, strength);
		addPlayerToGame();
	}

	public boolean isHumanPlayer() {
		return true;
	}

	@Override
	public void addPlayerToGame() {
		super.addPlayerToGame();
		game.addGamerToGame(this);
	}

	public void move(Direction dir) throws InterruptedException {
		Cell cell = getCurrentCell();
		Coordinate pos = cell.getPosition();
		if(dir == Direction.RIGHT && pos.x < Game.DIMX - 1) {
			//avança para a direita
			Coordinate newpos = pos.translate(Direction.RIGHT.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
		if(dir == Direction.LEFT && pos.x>0) {
			//avança para a esquerda
			Coordinate newpos = pos.translate(Direction.LEFT.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
		if(dir == Direction.UP && pos.y>0) {
			//avança para cima
			Coordinate newpos = pos.translate(Direction.UP.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();

		}
		if(dir == Direction.DOWN && pos.y<Game.DIMY - 1) {
			//avança para baixo
			Coordinate newpos = pos.translate(Direction.DOWN.getVector());
			Cell newcell = game.getCell(newpos);
			newcell.movePlayerTo(this);
			game.notifyChange();
		}
	}
	
}
