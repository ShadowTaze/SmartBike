import java.net.*;
import java.util.HashMap;
//import DatabaseManager.java;

public class Server {
	private final int PACKETSIZE = 500;
	private final boolean debug = false;
	private boolean running;
	private DatabaseManagerInterface DbM;
	private DatagramSocket socket;
	private String serverIP;
	private int serverPort;
	private double maxSpeed;
	private boolean speeding;
	public Server() {
		this(13375);
	}

	public Server(int port) {
		this("192.168.145.130", port);
	}

	public Server(String serverIP, int serverPort){
		this(serverIP, serverPort, new DatabaseManager());
	}

	public Server(String serverIP, int serverPort, DatabaseManagerInterface DbM){
		try {
			this.serverIP = serverIP;
			this.serverPort = serverPort;
			this.DbM = DbM;
			this.running = false;
			speeding = false;
			maxSpeed = 999;
			socket = new DatagramSocket(serverPort);
		} catch (Exception e){
			System.out.println("Error in the Server constructor...");
			e.printStackTrace();
			System.exit(0); //nothing can be done...
		}
	}


	public boolean getRunning() {
		return running;
	}
	public String getServerIP(){
		return serverIP;
	}
	public int getServerPort(){
		return serverPort;
	}
	//Whenever a packet is received this method is called with the packet.
	//It will get the plain text string and split it bassed on the ':' char
	//The first string will be the measurement type or the request
	//The second string will be the value / port to respond too.
	//The case statement will handle each requests
	private void packetReceived(DatagramPacket packet) {
		String s = new String(packet.getData()).trim();
		String[] pairs = s.split(":");
		if(pairs.length == 2 ){
			switch (pairs[0]){
				case "GPS"	:		DbM.addMeasurement(pairs[0], pairs[1]);
									DbM.setSystemState(pairs[0], pairs[1]);
								break;
				case "setRadius":	double rad = Double.parseDouble(pairs[1]);
									System.out.println("rad: " + rad);
									//assert(rad!=null);	
									//DbM.setRad()
				case "setMaxSpeed": maxSpeed = Double.parseDouble(pairs[1]);
								break;
				case "newRide": 	DbM.newRide();
								break;
				case "brake":		DbM.setSystemState(pairs[0],pairs[1]);
								break;
				case "turnL": 		DbM.setSystemState(pairs[0],pairs[1]);
								break;
				case "getState":	sendState(packet.getAddress(), pairs[1]);
								break;
				case "turnR":		DbM.setSystemState(pairs[0],pairs[1]);
								break;
				case "Speed":		DbM.addMeasurement(pairs[0], pairs[1]);
									DbM.setSystemState(pairs[0], pairs[1]);
									if(Double.parseDouble(pairs[1]) > maxSpeed & !speeding){
										speeding = true;
										alertRider();
									}
									if(speeding & Double.parseDouble(pairs[1]) < maxSpeed)
										speeding = false;
								break;
				default:			System.out.println("Unknown request: " + s);
								break;
			}
			//System.out.println("Type: " + pairs[0] + "\nValue: " + pairs[1]);
		}
	}
	//This will be used to alert the rider if they are going too fast
	//This code is writen and mounted on our bike, we will push it
	//after we take the bike apart. 
	public void alertRider(){
		if(debug) System.out.println("TOO FAST!");
		//The method does something like this:
		//else {
		//	int sport = 11111;
		//	InetAddress host = new InetAddress("10.0.0.12"); //I made this up
		//	try {
		//		int port = Integer.parseInt(sport);
        	  //      	DatagramSocket socket = new DatagramSocket();
	        	//        byte[] data;
			//	//send udp to handle pi
			//	if(speeding ) {
			//		data = "slowRider:1".getBytes();
			//	} else {
			//		data = "slowRider:0".getBytes();
			//	}
			//	socket.send(new DatagramPacket(data, data.length, host, port));
			//} catch (Exception e) {
			//	System.out.println("Error alerting rider");
			//}
//		}
	}
	//stop running, breaks out of the loop without closing connections
	public void stopReceiving(){
		running = false;
	}
	//While running receive udp packets and send them to the packet handler
	//If I had more time / was able to focus more on this code I would have 
	//Implemented runable and had the udp receiver in a seperate thread
	//Due to time restraints / android app programing I wasn't able to get to that
	//"Focus on functionality first Ben." they always said. :) 
	public void startReceiving(){
		running = true;
		//System.out.println("Starting to receive");
		while(running){
			try {
				DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE );
				socket.receive(packet);
				this.packetReceived(packet);
			} catch (Exception e) {
				e.printStackTrace();
				//this error isn't critical, continue 
			}
		}
	}
	
	//Sends a copy of the system state to the ip + port that requested it
	//It will be sent in the following format:
	//"Speed:10 turnL:1 turnR:0 brake:1 "
	private void sendState(InetAddress host, String sport){
		try {
			int port = Integer.parseInt(sport);
			DatagramSocket socket = new DatagramSocket();
			byte[] data;
			String s = "";
			//Iterate through the keys in the hash table building the message
			HashMap<String, String> state = DbM.getSystemState();
			for (String key : state.keySet()){
				s += key + ':' +state.get(key) + ' ';
			}
			data = s.getBytes();
	        socket.send(new DatagramPacket(data, data.length, host, port));
		} catch (Exception e){
			System.out.println("Failed to send System state...");
			e.printStackTrace();
		}
	}
	public void setPort(int newPort) throws Exception {
		this.serverPort = newPort;
	}
	//close sockets and turn off running
	public void exit() {
		try{
			this.socket.disconnect();
			this.socket.close();
			running = false;
		} catch (Exception e){
			System.out.println("Error closing sockets...");
			e.printStackTrace();
		}
	}

	public static void main(String [] args){
		Server s = new Server();
		s.startReceiving();
	}
}
