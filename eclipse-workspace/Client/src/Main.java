import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		
		String adresse = lireAdresseValide();
		int port = lirePortValide();
		
		communication(adresse, port);
				
	}
	
	public static void communication(String adresse, int port) {
				
		try 
		{
            Socket serveur = new Socket(adresse, port);
			
            ObjectOutputStream fluxSortie = new ObjectOutputStream(serveur.getOutputStream());
            ObjectInputStream fluxEntree = new ObjectInputStream(serveur.getInputStream());
            	 
            while (!serveur.isClosed())  		                
                while (!commande(fluxSortie, fluxEntree));
            
            serveur.close();
        } catch(Exception e) {
		System.err.println(e);
		}
				
	}
	
	public static boolean commande(ObjectOutputStream fluxSortie, ObjectInputStream fluxEntree) throws Exception {
		
		Scanner sc  = new Scanner(System.in);
		String[] commande = sc.nextLine().split(" ");
		boolean retour = false;
		
		switch(commande[0]) {
		
			case "cd": 
				
				if(!verifierParam(commande, 1)) break;						// verif du param
				
				fluxSortie.writeObject(commande);							// envoie de la commande
				System.out.println(fluxEntree.readObject());					// affichage de la reponse
				
				retour = true;
				break;
				
			case "ls": 
				
				if(!verifierParam(commande, 0)) break;
				
				fluxSortie.writeObject(commande);							// envoie de la commande
				for(String fichier: (String[])fluxEntree.readObject()) 		// affichage de la reponse
					System.out.println(" > " + fichier);
				
				retour = true;
				break;
				
			case "mkdir": 
				
				if(!verifierParam(commande, 1)) break;
				
				fluxSortie.writeObject(commande);							// envoie de la commande
				System.out.println(fluxEntree.readObject());				// affichage de la reponse
				
				retour = true;
				break;
				
			case "upload": 
				
				if(!verifierParam(commande, 1)) break;
				
				fluxSortie.writeObject(commande);							// envoie de la commande
				File fichierOut = copierFichier(commande[1]);				// copier le fichier
				if(fichierOut != null) fluxSortie.writeObject(fichierOut);	// envoyer le fichier
				
				retour = true;
				break;
				
			case "download": 
				
				if(!verifierParam(commande, 1)) break;				
				
				fluxSortie.writeObject(commande);						// envoie de la commande
				File fichierIn = (File)fluxEntree.readObject();				// recevoir fichier
				if(fichierIn != null)  collerFichier(fichierIn);			// coller fichier
				
				retour = true;
				break;
				
			case "exit": 
				
				if(!verifierParam(commande, 0)) break;
				fluxSortie.writeObject(commande);						// envoie de la commande
				System.exit(0);
				
				break;
				
			default:
		}
		
		if(!retour) System.err.println("Commande inexistante");

		return retour;
	}
	
	public static File copierFichier(String nom) {
		return null;
	}
	
	public static void collerFichier(File fichier) {
		
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
		String temp = null;
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
		} while(temp == null);
		
		return port;
	}

	public static boolean verifierParam(String[] param, int nbParam) {
		
		
		
		if(nbParam != param.length-1) {
			System.err.println(param[0] + " prend " + nbParam + " parametre, vous en avez fourni " + (param.length-1));
			return false;
		}
		
		return true;
	}

}
