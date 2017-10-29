package ing.tictactoe;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Cette classe traite une nouvelle requête du serveur
 */
public class HttpWorker extends Thread {

    private Socket socket = null;
    protected BufferedReader input = null;
    protected DataOutputStream output = null;
    private final String pathHeader;
    private String type;


    public HttpWorker(Socket socket, String path_header) {
        super("HttpWorker");
        this.socket = socket;
        this.pathHeader = path_header;
    }


    public void run() {
        try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new DataOutputStream(socket.getOutputStream());
			httpHandler();
			input.close();
			output.close();
			socket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }


    /**
     *  Traite une requête HTTP
     */
    private void httpHandler() {
        String headerHTTP = null;
		try {
			headerHTTP = input.readLine();
			//Display all Header Requested
			System.out.println("Header Requested : " + headerHTTP);
			String requestedRessources = requestHTTPparser(headerHTTP);
			File f = new File(pathHeader + requestedRessources);
			
			if(f.exists() && !f.isDirectory() && f.canRead()) {
				FileInputStream fils = new FileInputStream(f);
				if (type == null)
					type = "HTML";
				System.out.println("Type dans httpHandler : " + type);
				output.writeBytes(HttpServer.constructHttpHeader(200, FileType.getFileTypeByName(type)));
				
				HttpServer.outputFile(fils, output);
				output.flush();
			} else {
				fileNotFoundHandler(headerHTTP);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    }


    private String requestHTTPparser(String request) {
		String [] req;
    	if(request.equals("GET / HTTP/1.1")) {
    		System.out.println("INDEX");
    		return "index.html";
    	}
    	
    	else {
    			req =request.substring(5).split(" ");
    			//System.out.println("toto " + req[0]);
    			//On verifie que il y a une extenxion si oui on l'ajoute
    			if(req[0].contains(".")){
    				type = req[0].split("\\.")[1].toUpperCase();
    			}
    			//System.out.println("Type fdfdsf = " + type);
    			return req[0];
    			
    		}
	}


	/** Cette méthode gère une ressource non-trouvée
     * @param path : chemin de la ressource non trouvée
     */
    public void fileNotFoundHandler(String path) {
        try {
            // Envoi de l'erreur 404 : Fichier non-trouvé
            String retMessage = "<html><head></head><body>Fichier "+path+" non trouvé...</body></html>\n";

            output.writeUTF(HttpServer.constructHttpHeader(404, FileType.HTML));
            output.writeUTF(retMessage);
        } catch (Exception e) {
            System.err.println("Erreur avec le chemin "+path+" : " + e.getMessage());
        }
    }

} // class
