/*
 * Team Memebers: Hardik Trivedi (hpt150030)
 * 				  Roshan Ravikumar (rxr151330)
 * 				  Sapan Gandhi (sdg150130) 
 */

public class Message
{
	private int processId;
	
	/*
	 * R - Ready for next round
	 * N - Start of next round
	 * I - Incoming
	 * O - Outgoing
	 * L - I'm leader Leader
	 */
	private char type;
	
	/*
	 * Integer.MIN_VALUE denotes hops field is insignificant
	 */
	private double hops;
	
	private char fromDir;
	
	public Message(int processId, char type, double hops, char fromDir)
	{
		this.processId = processId;
		this.type = type;
		this.hops = hops;
		this.fromDir = fromDir;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public double getHops() {
		return hops;
	}

	public void setHops(int hops) {
		this.hops = hops;
	}

	public char getFromDir() {
		return fromDir;
	}

	public void setFromDir(char fromDir) {
		this.fromDir = fromDir;
	}

	public void setHops(double hops) {
		this.hops = hops;
	}
	
	public String toString()
	{
		return "Process ID:"+processId+" Type:"+type+" Hops "+hops+" From Dir:"+fromDir;
		
	}
}