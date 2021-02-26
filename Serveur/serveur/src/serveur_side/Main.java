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
	
	String chemin = "./";
	
	// done
	public static void main(String[] args) {
		
		try { 		// initialise le la reference de port	
			
			portRef = Integer.valueOf(args[0])-1 ;
			if(portRef < 5000 || portRef > 5050) throw new Exception("mauvais port");
			if(portRef != Math.floor( portRef)) throw new Exception("mauvais port");
			
		} catch (Exception e) {
			System.exit(0);
		}
		
		// ouverture du premier client
		Main nouveauClient = new Main();
		nouveauClient.start();
		
	}
	
	// done
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
			
			nbClient++;
			
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
						fluxSortie.writeObject(collerFichier((byte[])fluxEntree.readObject(), commande[1]));
						break;
					case "download": 
						copierFichier(commande[1], fluxSortie);
						break;
					case "exit":
						nbClient--;
						if(nbClient < 1) System.exit(0);	
						else return;
				}
				
			}
			
		}
		catch (Exception e) {
			
			// afficher l'erreur et mettre fin a la communication avec ce client
			System.err.println(e);
			return;
			
		}
	}
	
	// done
	public void run() {
		
		nextPort();
		boolean portAccepter = false;
		
		// trouver le prochain port valide
		while(!portAccepter) {
			try {
				ServerSocket test = new ServerSocket(portRef);
				test.close();
				portAccepter = true;
			} catch (Exception e) {
				System.out.println("Le port " + portRef + " est innacessible");
				nextPort();
			}
		}
		
		System.out.println("Nouveau port d'Acceuil: " + portRef);
		nouveauClient(portRef);
	}
	
	// done
	public static void nextPort() {
		portRef = (portRef+1)%50+5000;
	}
	
	// done
 	public String cd(String dossier) {
		
		if(dossier.equals("..")) {
			chemin = chemin.equals("./") ? chemin: chemin.substring(0, chemin.lastIndexOf("/"));
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
	public void copierFichier(String nom, ObjectOutputStream fluxSortie) throws IOException {
		
		// verification de l'Existence du fichier
		
		boolean lecturePossible = false;
		
		for(String fichier: ls()) {
			if(fichier.equals(nom)) {
				lecturePossible = true;
			}
		}
		
		if(!lecturePossible) {
			fluxSortie.writeObject("Le fichier demander n'existe pas");
			fluxSortie.writeObject(false);
			return;
		}
		
		// verifier que ce n'est pas un repertoire.
		
		if(!nom.contains(".")) {
			fluxSortie.writeObject("Le fichier demander est un repertoire. Impossible de le telecharger");
			fluxSortie.writeObject(false);
			return;
		}
		
		fluxSortie.writeObject("Copie en cours...");
		fluxSortie.writeObject(true);
		
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
	
	// done
	public String collerFichier(byte[] data, String nom) {
				
		try {
			
			FileOutputStream fluxFichier = new FileOutputStream(chemin + "/" + nom);
		    BufferedOutputStream fluxSortie = new BufferedOutputStream(fluxFichier);
		    fluxSortie.write(data, 0 , data.length);
		    fluxSortie.flush();
		    return "le ficher " + nom + " a été sauvgarder sur le serveur";
		    
		} catch (IOException e) {
			
			return "Le fichier " + nom + " n'a pas pu etre sauvergarder sur le serveur";
			
		}
	    
	}
	
}
