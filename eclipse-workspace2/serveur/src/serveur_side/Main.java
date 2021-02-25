package serveur_side;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Thread{
	
	static int nbClient = 0;
	static int portRef;
	
	String chemin = "./../../serveur";
	
	public static void main(String[] args) {
		
		// initialise le la reference de port
		portRef = args.length == 1 ? Integer.valueOf(args[0])-1 : 5012;
		
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
						collerFichier(fluxEntree, commande[1]);
						break;
					case "download": 
						copierFichier2(commande[1], fluxSortie);
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
	
	// done
	public String cd(String dossier) {
		
		// implement /..
		if(dossier.equals("..")) {
			chemin = chemin.equals("./../../serveur") ? chemin: chemin.substring(0, chemin.lastIndexOf("/"));
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
	
	// done
	public String[] ls() {
		
		File repertoire = new File(chemin);
		return repertoire.list();
	
	}
	
	// done
	public String mkdir(String nom) {
		try {
			Files.createDirectories(Paths.get(chemin+"/"+nom));
			return nom + " a été créer";
		} catch (Exception e) {
			return "impossible de créer " + nom;
		}
	}
	
	// done
	public void copierFichier2(String nom, ObjectOutputStream fluxSortie) throws IOException {
		
		FileInputStream fluxFichier = null;
	    BufferedInputStream fluxEntree = null;
	    byte[] data;
	    
        try {
          // send file
          File fichier = new File (chemin +"/"+nom);
          data  = new byte [(int)fichier.length()];
          fluxFichier = new FileInputStream(fichier);
          fluxEntree = new BufferedInputStream(fluxFichier);
          fluxEntree.read(data,0,data.length);
          
          fluxSortie.writeObject(data);
                    
        } catch (Exception e) {
        	
        	data = new byte[1];
            fluxSortie.writeObject(data);
			
		}
		      
	}
	
	public void collerFichier(ObjectInputStream fluxEntree, String nom) throws Exception{
		
		byte[] data = (byte[])fluxEntree.readObject();
		
		FileOutputStream fluxFichier = null;
	    BufferedOutputStream fluxSortie = null;
	    
		fluxFichier = new FileOutputStream(chemin + "/" + nom);
	    fluxSortie = new BufferedOutputStream(fluxFichier);
	    
	    fluxSortie.write(data, 0 , data.length);
	    fluxSortie.flush();
	    
	}
	
}
