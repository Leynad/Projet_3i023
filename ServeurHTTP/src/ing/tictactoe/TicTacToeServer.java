package ing.tictactoe;

import java.io.IOException;
import java.net.Socket;

/**
 * Cette classe crée un serveur de jeu de Tic Tac Toe
 */
public class TicTacToeServer extends HttpServer {
    private TicTacToe game;

    public TicTacToeServer(int listeningPort, String pathHeader, TicTacToe ttt) {
        super(listeningPort, pathHeader);
        this.game = ttt;
    }


    /**
     *  Cette méthode attend une requête et créée un nouveau thread pour prendre en charge la requête
     * @throws IOException 
     */
    @Override
    void waitingForRequest() throws IOException {
        /* A compléter */
    	Socket socket = welcomeSocket.accept();
        TicTacToeWorker worker = new TicTacToeWorker(socket,pathHeader,game);
        worker.start();
    }

}
