/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

/**
 *
 * @author Rima Ghoulam
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client extends Thread {

private Socket clientSocket;
private PrintWriter pw;
private BufferedReader br;

    public static void main(String args[]) {

        String host = "127.0.0.1";
        int port = 1491;

        Client client = new Client();
        client.start();
        
    }

    private Client() {}

    public void run() {

        Scanner clientSc = new Scanner(System.in);
        String in = clientSc.nextLine();
        String[] parametres = in.split(" ");
        
    try {
        
        clientSocket = new Socket("127.0.0.1", 1491);
        
        br = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        pw = new PrintWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()), true);

        
        
        while(this.clientSocket.isConnected()) {
        try {
            if (in.startsWith("_c ")) {
                pw.println(parametres[1]);
                System.out.println("[INFO] Connecter");

            } else if (in.startsWith("_qui")){ pw.println("_qui"); }

            
            String r = br.readLine();

            if (r.startsWith("_exit ") && r!=null){
                if(parametres[1]!=null)
                System.exit(0);
            }

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    } catch (IOException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
    }
}