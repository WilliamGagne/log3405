package serveur_side;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Thread{
	
	static int nbClient = 0;
	static int portRef;
	
	String chemin = "./serveur";
	
	public static void main(String[] args) {
		
		// initialise le la reference de port
		portRef = args.length == 1 ? Integer.valueOf(args[0])-1 : 5006;
		
		// ouverture du premier client
		Main nouveauClient = new Main();
		nouveauClient.start();
		
	}
	
	public void nouveauClient(int port) {
		
		try {
			// initialisation des sockets
			ServerSocket serveur = new ServerSocket(port);
			Socket client = serveur.accept();
			String adresseClient, commande[];
				
			// initialisation des flux
			ObjectOutputStream fluxSortie = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream fluxEntree = new ObjectInputStream(client.getInputStream());
			
			//----------------------------
			adresseClient = client.getRemoteSocketAddress() +  ":" + client.getPort();
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd@HH:mm:ss");  
			LocalDateTime date = LocalDateTime.now();
			//----------------------------
			
			// creation d'une nouvelle ligne d'attente
			Main nouveauClient = new Main();
			nouveauClient.start();
			
			// communication avec le client
			while ((commande = (String[])fluxEntree.readObject()) != null) {
				
				System.out.println("[" + adresseClient + "-" + format.format(date) + "]: " + commande[0] + " " + (commande.length>1 ? commande[1]: ""));
				
				switch(commande[0]) {
					case "cd": 
						fluxSortie.writeObject(cd(commande[1]));
						break;
					case "ls": 
						fluxSortie.writeObject(ls());
						break;
					case "mkdir":
						fluxSortie.writeObject(mkdir(commande[1]));
						break;
					case "upload": 
						collerFichier((File)fluxEntree.readObject());
						break;
					case "download": 
						fluxSortie.writeObject(copierFichier(commande[1]));
						break;
					case "exit":
						System.exit(0);				
				}
				
			}
			
		}
		catch (Exception e) {
			
			// afficher l'erreur et mettre fin a la communication avec ce client
			System.err.println(e);
			return;
		}
	}
	
	public void run() {
		portRef++;
		System.out.println("Nouveau port d'Acceuil: " + portRef);
		nouveauClient(portRef);
	}
	
	public String cd(String dossier) {
		
		// implement /..
		if(dossier.equals("..")) {
			chemin = chemin.equals("./serveur") ? chemin: chemin.substring(0, chemin.lastIndexOf("/"));
			return chemin;
		}
		
		for(String fichier: ls()) {
			if(fichier.equals(dossier)) {
				chemin += "/" + dossier;
				return chemin;
			}
		}
		return "aucun repertoire a ce nom";
	}
	
	public String[] ls() {
		
		File repertoire = new File(chemin);
		return repertoire.list();
	
	}
	
	public String mkdir(String nom) {
		try {
			Files.createDirectories(Paths.get(chemin+"/"+nom));
			return nom + " a été créer";
		} catch (Exception e) {
			return "impossible de créer " + nom;
		}
	}
	
	public File copierFichier(String nom) {
		
		return new File(chemin + "/" + nom);

	}
	
	public void collerFichier(File fichier) {
		
		fichier.renameTo(new File(new File(chemin), fichier.getName()));

	}
	
}
