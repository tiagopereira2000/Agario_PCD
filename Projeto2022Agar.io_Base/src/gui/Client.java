package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observer;
import java.util.Observable;
import javax.swing.*;

import environment.Cell;
import environment.Coordinate;
import environment.Direction;
import game.Game;
import game.PhoneyAutomaticPlayer;
import game.PhoneyHumanPlayer;
import game.Player;

/**
 * Client:
 * 		O cliente é que vai elaborar o jogo tal como a Gui.
 * As instâncias dos clientes, neste caso, são criadas pelo StartClients
 * que vai evocar 2 Clientes ou pelo próprio "main" do "Client".
 * 		O cliente vai ter acesso a alguns dados importados pelo Servidor.
 * 	No entanto, este não desempenha nenhum papel de importância no servidor, pois não tem
 * acesso a este diretamente.
 * 		O cliente vai ter à sua disposição uma janela do jogo (Observer, JFrame),
 * de onde vai conseguir digitar as suas teclas de movimentação e exportá-los para o servidor.
 * 		O Servidor recebe estas informações e vai, retribuindo o desenrolar do jogo,
 * independentemente se este se movimenta ou não.
 * Para a invocação dos KeyListeners implementámos a classe BoardJComponent
 * nesta classe como aninhada da mesma.
 *
 *  @author Tiago Pereira e Gonçalo Lopes
 * 
 */
public class Client implements Observer{
	private JFrame frame;
	private BoardJComponent board;
	private Game game;
	private Socket socket;
	private InetAddress address;
	private int port;
	private int up;
	private int left;
	private int down;
	private int right;
	private PhoneyHumanPlayer myPlayer;
	private int lastPressedKey;

	public Client(InetAddress addr, int port,
				  int up, int left, int down, int right) throws IOException {
		super();
		address = addr;
		this.port = port;
		this.up= up;
		this.left = left;
		this.down = down;
		this.right = right;
		frame=new JFrame("pcd.io");
		game=new Game();
		game.addObserver(this);

	}

	/**
	 * Classe BoardJComponent:
	 * Implementa o KeyListener que vai ler os inputs do teclado e atualizar o frame.
	 *
	 */
	public class BoardJComponent extends JComponent implements KeyListener {
		private Image obstacleImage = new ImageIcon("obstacle.png").getImage();
		private Image humanPlayerImage= new ImageIcon("abstract-user-flat.png").getImage();

		public BoardJComponent() {
			setFocusable(true);
			addKeyListener(this);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			double cellHeight=getHeight()/(double)Game.DIMY;
			double cellWidth=getWidth()/(double)Game.DIMX;

			for (int y = 1; y < Game.DIMY; y++) {
				g.drawLine(0, (int)(y * cellHeight), getWidth(), (int)(y* cellHeight));
			}
			for (int x = 1; x < Game.DIMX; x++) {
				g.drawLine( (int)(x * cellWidth),0, (int)(x* cellWidth), getHeight());
			}
			for (int x = 0; x < Game.DIMX; x++)
				for (int y = 0; y < Game.DIMY; y++) {
					Coordinate p = new Coordinate(x, y);

					Player player = game.getCell(p).getPlayer();
					if(player!=null) {
						// Fill yellow if there is a dead player
						if(player.getCurrentStrength()==0) {
							g.setColor(Color.YELLOW);
							g.fillRect((int)(p.x* cellWidth),
									(int)(p.y * cellHeight),
									(int)(cellWidth),(int)(cellHeight));
							g.drawImage(obstacleImage, (int)(p.x * cellWidth), (int)(p.y*cellHeight),
									(int)(cellWidth),(int)(cellHeight), null);
							// if player is dead, don'd draw anything else?
							continue;
						}
						// Fill green if it is a human player
						if(player.isHumanPlayer()) {
							g.setColor(Color.GREEN);
							g.fillRect((int)(p.x* cellWidth),
									(int)(p.y * cellHeight),
									(int)(cellWidth),(int)(cellHeight));
							// Custom icon?
							g.drawImage(humanPlayerImage, (int)(p.x * cellWidth), (int)(p.y*cellHeight),
									(int)(cellWidth),(int)(cellHeight), null);
						}
						g.setColor(new Color(player.getIdentification() * 1000));
						((Graphics2D) g).setStroke(new BasicStroke(5));
						Font font = g.getFont().deriveFont( (float)cellHeight);
						g.setFont( font );
						String strengthMarking=(player.getCurrentStrength()==10?"X":""+player.getCurrentStrength());
						g.drawString(strengthMarking,
								(int) ((p.x + .2) * cellWidth),
								(int) ((p.y + .9) * cellHeight));
					}

				}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			lastPressedKey=e.getKeyCode();
				if (lastPressedKey == left) {
					out.println("left");
				}
				else if(lastPressedKey == right) {
					out.println("right");
				} else if (lastPressedKey == up) {
					out.println("up");
				} else if (lastPressedKey == down) {
					out.println("down");
				}
			}

		@Override
		public void keyReleased(KeyEvent e) {
			//ignore
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// Ignored...
		}

	}

	private ObjectInputStream in;
	private PrintWriter out;

	/**
	 * Conexão com o servidor vai criar a "socket" com o endereço e porto do
	 * servidor.
	 * Define os Streamers.
	 */
	void connectToServer() throws IOException {
		System.out.println("Endereco:" + address);
		socket = new Socket(address, port);
		System.out.println("Socket:" + socket);
		in = new ObjectInputStream(socket.getInputStream());
		out = new PrintWriter (new BufferedWriter (
				new OutputStreamWriter(socket.getOutputStream())),
				true);
	}

	/**
	 * Cria a GUI a partir da BoardJComponent que recebe da socket do server.
	 */
	private void buildGui() throws ClassNotFoundException, IOException {
		System.out.println("BUILDING GUI");
		game.setBoard((Cell[][]) in.readObject());
		System.out.println("GAME RECEIVED");
		board = new BoardJComponent();
		frame.add(board);
		frame.setSize(800, 800);
		frame.setLocation(0, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	/**
	 * Inicializa o frame do Jogo.
	 */
	public void clientRun() throws IOException, ClassNotFoundException, InterruptedException {

		new Thread(){
			@Override
			public void run() {
				super.run();
				try{
					connectToServer();
					buildGui();
					frame.setVisible(true);
					while (!game.isGameOver){
						System.out.println("CLIENT RUNNING");
						game.setBoard((Cell[][]) in.readObject());
						System.out.println("BOARD READ");
						game.notifyChange();
					}
					socket.close();
				} catch (IOException | ClassNotFoundException exception){
					exception.printStackTrace();
				}
				System.out.println("SOCKET CLOSED");
			}
		}.start();
	}

	@Override
	public void update(Observable o, Object arg) {
		board.repaint();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		new Client(
				InetAddress.getByName(null),
				Server.PORTO,
				KeyEvent.VK_UP,
				KeyEvent.VK_LEFT,
				KeyEvent.VK_DOWN,
				KeyEvent.VK_RIGHT).clientRun();
	}
}




