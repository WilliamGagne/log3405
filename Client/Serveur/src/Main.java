import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		
		
		connexion(lireAdresseValide(), lirePortValide());
		while(lireCommande());
		
	}
	
	public static boolean connexion(String adresse, int port) {
		
		// connexion au serveur
		String serveur = "int", reponse;
		Scanner sc  = new Scanner(System.in);
		System.out.println("Connexion a " + adresse + " sur " + port);
		
		/*try {
			Socket socket = new Socket(adresse, port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			
			
			while((serveur = br.readLine())!=null) {
				System.out.println(serveur);
				System.out.println("message:");
				reponse = sc.nextLine();
				pw.write(reponse);
			}
			
		}*/
		try (
	            Socket kkSocket = new Socket(adresse, port);
	            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(
	                new InputStreamReader(kkSocket.getInputStream()));
	        ) {
	            BufferedReader stdIn =
	                new BufferedReader(new InputStreamReader(System.in));
	            String fromServer;
	            String fromUser;
	 
	            while ((fromServer = in.readLine()) != null) {
	                System.out.println("Server: " + fromServer);
	                if (fromServer.equals("Bye."))
	                    break;
	                 
	                fromUser = stdIn.readLine();
	                if (fromUser != null) {
	                    System.out.println("Client: " + fromUser);
	                    out.println(fromUser);
	                }
	            }
	        }
		
		catch(Exception e) {
			System.out.println(e);
		}
		
		return false;
	}
	
	public static String lireAdresseValide() {
		
		Scanner sc = new Scanner(System.in);
		String temp = "";
		int[] adresse = new int[4];
		
		System.out.println("Adresse IP du serveur : xxx.xxx.xxx.xxx");
				
		do {
			try {
				temp = sc.nextLine();
				for(int i = 0; i < 4; i++) {
					adresse[i] = Integer.valueOf(temp.split("\\.")[i]);
					if(adresse[i] > 254) throw new Exception("depassement d'octet");
				}
				temp = "";
			}
			catch(Exception e) {
				System.out.println("Adresse IP invalide");
			}
		} while(temp != "");
		
		return temp;

	}
	
	public static int lirePortValide() {
		
		Scanner sc = new Scanner(System.in);
		String temp = "";
		int port = 0;
		
		System.out.println("Port du serveur : xxxx (5000 - 5050)");
				
		do {
			try {
				temp = sc.nextLine();
				port = Integer.valueOf(temp);
				if(port < 5000 || port > 5050) throw new Exception("mauvais port");
				temp = "";
			}
			catch(Exception e) {
				System.out.println("Port invalide");
			}
		} while(temp != "");
		
		return port;
	}

	public static boolean lireCommande() {
		
		Scanner sc = new Scanner(System.in);
		String temp[], commande, argument;
		
		temp = sc.nextLine().split(" ");
		
		switch(temp[0]) {
		
			case "cd":
				if(verifierParam(temp, 1)) {
					cd(temp[1]);
				}
				break;
			case "ls":
				if(verifierParam(temp, 0)) {
					ls();
				}
				break;
			case "mkdir":
				if(verifierParam(temp, 1)) {
					mkdir(temp[1]);
				}
				break;
			case "upload":
				if(verifierParam(temp, 1)) {
					upload(temp[1]);
				}
				break;
			case "download":
				if(verifierParam(temp, 1)) {
					download(temp[1]);
				}
				break;
			case "exit": 
				if(verifierParam(temp, 0)) {
					System.out.println("fin");
					return false;
				}
				break;
			default: 
				System.out.println("Commande inexistante");
		
		}

		return true;
		
	}
	
	public static boolean verifierParam(String[] param, int nbParam) {
		
		if(nbParam != param.length-1) {
			System.out.println(param[0] + " prend " + nbParam + " parametre, vous en avez fourni " + (param.length-1));
			return false;
		}
		
		return true;
	}

	public static void cd(String param) {
		System.out.println("-- cd : " + param);
	}
	
	public static void ls() {
		System.out.println("-- ls");
	}
	
	public static void mkdir(String param) {
		System.out.println("-- mkdir : " + param);
	}
	
	public static void upload(String param) {
		System.out.println("-- upload : " + param);
	}
	
	public static void download(String param) {
		System.out.println("-- download : " + param);
	}
	
}
