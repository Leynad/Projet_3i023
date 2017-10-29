package ing.tictactoe;

import java.io.IOException;
import java.net.Socket;

/**
 * Cette classe traite une nouvelle requête de jeu ou de connexion du serveur
 */
public class TicTacToeWorker extends HttpWorker {

    private TicTacToe game;
    private String httpResponse;
    
    /**
     * Constructeur de la classe TicTacToeWorker
     * @param socket socket client
     * @param pathHeader emplacement du dossier public_html
     * @param ttt référence au jeu TicTacToe
     */
    public TicTacToeWorker(Socket socket, String pathHeader, TicTacToe ttt) {
        super(socket, pathHeader);
        this.game = ttt;
        this.httpResponse = "";
    }


    @Override
    public void fileNotFoundHandler(String path) {
        try {
			if(path.equals("GET /connection? HTTP/1.1")){
				//First player
				//int idPlayer = game.addPlayer();
				//super.output.writeInt(idPlayer);
				//super.output.writeBytes(constructHttpResponse("UPDATE", idPlayer));
				//super.output.flush();
				int o;
				
				o = game.addPlayer();
				if (o == 0) {
					httpResponse = constructHttpResponse("The Noughts ", o);
					System.out.println(httpResponse);
				} else if (o == 1) {
					httpResponse = constructHttpResponse("The Crosses ", o);
				} else {
					httpResponse = constructHttpResponse(
							"Game in session ",
							o);
				}
				output.writeUTF(httpResponse);
			} else {
				String ajaxRequest = StoA(path);
				switch (ajaxRequest) {
				case "update":
					//System.out.println("update toto");
	    			
					int playerid = getPlayerID(path);
					String update_message="";
					int gg;
					
					if(game.getNbPlayer() < 2){
						update_message = constructHttpResponse("No adversary vs", playerid);
					}else if((gg = game.getWinner()) == playerid){
						update_message = constructHttpResponse("Win", playerid);
					}else if(gg == 2){
						
						update_message = constructHttpResponse("Tie", playerid);
						
					}else if (gg == (playerid+1)%2){
						update_message = constructHttpResponse("Lost", playerid);
					}else if(playerid == game.getNextToPlay()){
						update_message = constructHttpResponse("Your turn", playerid);
					}else{
						update_message = constructHttpResponse("Turn to the second Challenger", playerid);
					}
					super.output.writeUTF(update_message);
					
					break;
					
				case "move":
					int id = getPlayerID(path);
	    			int line = getLine(path);
	    			int column = getColumn(path);
	    			//System.out.println("Mouvement " + id + " " + line + " " + column);
	    			game.doMove(id, line, column);
	    			// Check the partie ended
	    			if(game.getWinner() != -1){
	    				//Check match is not a tie
	    				if(game.getWinner() != 2){
	    					game.getWinnerLine();
	    					System.out.println("Le joueur " +id+ " a gagne !\n" +game.toString());
	    				}else{
	    					System.out.println("Match nul !\n" +game.toString());
	    				}
	    			}
	    			
	    			//super.output.writeBytes(constructHttpResponse("UPDATE MOVE", idPlayer));
	    			super.output.flush();
	    			break;
				
				case "reset":
					System.out.println("RESET");
					game.reset();
        			game.addPlayer();
        			game.addPlayer();
        			super.output.writeBytes(constructHttpResponse("RESET", getPlayerID(path)));
            		super.output.flush();
            		System.out.println("RESET final");
					

				default:
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }





	/**
     * Construit la réponse HTTP + XML du serveur
     * L'état du jeu est entièrement décrit dans la réponse
     * Le client mettra à jour son interface graphique à la réception de cette réponse
     * @param message : message à envoyer
     * @param playerId : identifiant du joueur
     * @return La réponse HTTP
     */
    private String constructHttpResponse(String message, int playerId) {

        // construction du header de la réponse
        String httpResponse = HttpServer.constructHttpHeader(200, FileType.XML);

        httpResponse = httpResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n";
        httpResponse = httpResponse + "<RESPONSE>\r\n";
        httpResponse = httpResponse + "<message>" + message + "</message>\r\n";
        httpResponse = httpResponse + "<nbPlayer>" + Integer.toString(game.getNbPlayer()) + "</nbPlayer>\r\n";
        httpResponse = httpResponse + "<playerId>" + Integer.toString(playerId) + "</playerId>\r\n";
        httpResponse = httpResponse + "<winnerId>" + Integer.toString(game.getWinner()) + "</winnerId>\r\n";
        httpResponse = httpResponse + "<winnerLine>"+ game.getWinnerLine() +"</winnerLine>\r\n";
        httpResponse = httpResponse + "<nextToPlay>"+ Integer.toString(game.getNextToPlay())+"</nextToPlay>\r\n";

        for(int i = 0; i < game.GRID_SIZE; i++) {
            for(int j = 0; j < game.GRID_SIZE; j++) {
                httpResponse = httpResponse + "<cell>"+ Integer.toString(game.getCell(i,j))+"</cell>\r\n";
            }
            
        }

        httpResponse = httpResponse + "</RESPONSE>\r\n";

        return httpResponse;
    }

    
    /**
     * Determine la requete AJAX demandee.
     * @param request
     * @return la requete demandee sous forme de String
     */
    private static String StoA(String request){
    	request = request.substring(5);
    	String str = "";
    	for(int i = 0; i < request.length(); i++){
    		if(request.charAt(i) == '?'){
    			break;
    		}
    		str += request.charAt(i);
    	}
    	return str;
    }

    /**
     * Retourne l'ID d'un joueur dans une URL.
     * @param request
     * @return le premier ID rencontre.
     */
    public static int getPlayerID(String request){
    	boolean playerIdTime = false;
    	for(int i = 0; i < request.length(); i++){
    		if(playerIdTime == true){
    			if(request.charAt(i)=='-'){
    				return Integer.parseInt(""+request.charAt(i)+request.charAt(i+1));
    			}else{
    				return Integer.parseInt(""+request.charAt(i));
    			}
    		}
    		
    		if(request.charAt(i) == '='){
    			playerIdTime = true;
    		}
    	}
    	return -1;
    }
    
    /**
     * Recupere le numero de ligne dans une URL.
     * @param request
     * @return
     */
    public static int getLine (String request){
    	int cpt = 0;
    	for(int i = 0; i < request.length()-1; i++){
    		if(cpt == 2){
    			return Integer.parseInt(""+request.charAt(i));
    		}
    		if(request.charAt(i) == '='){
    			cpt++;
    		}
    	}
    	return -1;
    }
    
    /**
     * Recupere le numero de colonne dans une URL.
     * @param request
     * @return
     */
    public static int getColumn (String request){
    	int cpt = 0;
    	for(int i = 0; i < request.length(); i++){
    		if(cpt == 3){
    			return Integer.parseInt(""+request.charAt(i));
    		}
    		if(request.charAt(i) == '='){
    			cpt++;
    		}
    	}
    	return -1;
    }
}

