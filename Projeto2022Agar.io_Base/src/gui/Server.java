package gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.DoubleToIntFunction;

import environment.Direction;
import game.Game;
import game.PhoneyAutomaticPlayer;
import game.PhoneyHumanPlayer;


/**
 * Server:
 * ficará responsável por todas as comunicações entre os jogadores e o jogo.
 * O servidor é a aplicaçao principal 'main' do jogo.
 * É aqui que é iniciado todos os processos. Primeiro começa por iniciar o processo
 * de criar o jogo (GameInit extends Thread),
 * @author Tiago Pereira e Gonçalo Lopes
 */
public class Server {
	//private ExecutorService pool = Executors.newFixedThreadPool(10);
	private Game game;
	private Thread initThread;
	private ArrayList<ClientHandler> clientHandlers;
	private int nextPlayerId = 0;

	/**
	 * DealWithClient:
	 * extende a classe Thread e será uma classe interna ao servidor.
	 * O seu propósito é apenas fazer as conexões entre cliente e servidor
	 * através de 'sockets' .
	 */
	public class ClientHandler extends Thread {
		PhoneyHumanPlayer myPlayer;

		public ClientHandler(Socket socket) throws IOException, InterruptedException {
			doConnections(socket);
			myPlayer = new PhoneyHumanPlayer(nextPlayerId++, game,(byte)5 );
			game.addGamerToGame(myPlayer);
		}
		
		@Override
		public void run() {
			try {
				out.writeObject(game.getBoard());
				synchronized (clientHandlers){
					clientHandlers.add(this);
					clientHandlers.notifyAll();
				}
				//System.out.println("GAME EXPORTED");
				while (myPlayer.isActive()) movementListener();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private BufferedReader in;
		private ObjectOutputStream out;

		void doConnections(Socket socket) throws IOException {
			in = new BufferedReader ( new InputStreamReader (
					socket.getInputStream() ) );
			out = new ObjectOutputStream(socket.getOutputStream());
			//System.out.println("CONNECTIONS MADE");
		}
		
		private void movementListener() throws IOException, InterruptedException {
				switch(in.readLine()) {
					case "up":
						myPlayer.move(Direction.UP);
						break;
					case "down":
						myPlayer.move(Direction.DOWN);
						break;
					case "left":
						myPlayer.move(Direction.LEFT);
						break;
					case "right":
						myPlayer.move(Direction.RIGHT);
						break;
				}
			}

	}

	/**
	 * GameInit:
	 * 		Esta classe é interna ao servidor e fica responsável por criar o jogo.
	 * Extende a classe 'Thread' com o propósito de outros métodos não ficarem à
	 * espera que o processo acabe para dar continuidade ao jogo.
	 * 		É feito o ciclo que envia a cada "REFRESH_INTERVAL" o estado do jogo para
	 * todos os clientes através dos seus "ClientHandler"s.
	 * 		Após o jogo ter acabado é interrompido todos os processos
	 * adicionados à lista clientHandlers.
	 * O processo GameInit deve então terminar tal como o Server.main().
	 *
	 * Q1: Devo implementar uma ThreadPool?
	 * ThreadPool seria a seguinte:
	 * Iniciava p.e. 10 threads que iam ficar encarregues de mudar o estado dos NPC e movimenta-los.
	 *
	 *
	 */
	public class GameInit extends Thread{
		public GameInit() {
			game = new Game();
			clientHandlers = new ArrayList<>();
		}

		@Override
		public void run() {
			try {
				addBotsToGame(40);
			} finally {
				try {
					while(clientHandlers.isEmpty()) {
						synchronized (clientHandlers){
							clientHandlers.wait();
						}
					}
					while(!game.isGameOver){
						Thread.sleep(Game.REFRESH_INTERVAL);
						Game currentGame = game;
						for(ClientHandler c: clientHandlers)
							sendGameToClient(c, currentGame);
					}
					for(ClientHandler c: clientHandlers) c.interrupt();
					//System.out.println("Game is Over");
				} catch (InterruptedException | IOException e) {
					//nada é enviado
				}
				interrupt();
			}
		}

		void addBotsToGame(int n) {
			try{
				Thread.sleep(Game.INITIAL_WAITING_TIME);
				for(int i=0; i<n; i++){
					int rand = (int)(Math.random()*Game.MAX_INITIAL_STRENGTH) +1;
					new PhoneyAutomaticPlayer(nextPlayerId++, game, (byte)rand);
				}
			}catch(InterruptedException e){
				System.out.println("Initial waiting sleep time interrupted");
			}
		}

		void sendGameToClient(ClientHandler client, Game game) throws IOException {
			client.out.reset();
			client.out.writeObject(game.getBoard());
		}
	}//End GameInit


	public static final int PORTO = 8080;

	public void startServing() {
		initThread = new GameInit();
		initThread.start();
		try (ServerSocket ss = new ServerSocket(PORTO)){
			while(!game.isGameOver) {	// aceitacao das sockets
				Socket socket = ss.accept();
				new ClientHandler(socket).start();
			}
			initThread.join();
		} catch (IOException |InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {new Server().startServing();}



}
