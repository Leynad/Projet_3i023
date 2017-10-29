package ing.tictactoe;

public class Main {

    public static void main(String[] args) {
        TicTacToeServer ticTacToe = new TicTacToeServer(9090, "public_html/", new TicTacToe());
        ticTacToe.run();
    }

}
