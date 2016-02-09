import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Master 
{
	//Master process ID
	private int masterProcessId;
	
	//processes write to this to indicate that they are ready for the next round.
	private BlockingQueue<Message> masterQueue;
	
	//number of processes in the ring
	private int numberOfProcesses;
	
	boolean completed = false; 

	/*
	 * this collection contains an array of BlockingQueues
	 * master writes to processes' respective queue with a start of new round message (message type = N)
	 */
	private ArrayList<BlockingQueue<Message>> arrQueue = new ArrayList<BlockingQueue<Message>>();
	private ArrayList<BlockingQueue<Message>> arrRoundQueue = new ArrayList<BlockingQueue<Message>>();

	public Master(int masterProcessId, int[] id) //id array contains the unique id of the processes
	{
		this.masterProcessId = masterProcessId;
		
		//number of processes
		numberOfProcesses = id.length;

		//ready for next round queue
		masterQueue = new ArrayBlockingQueue<>(numberOfProcesses);
		
		Message readyMsg;
		BlockingQueue<Message> processQueue, roundQueue;

		for(int i = 0; i < numberOfProcesses; i++)
		{
			readyMsg = new Message(id[i], 'R', Integer.MIN_VALUE, 'X');
			masterQueue.add(readyMsg);

			/*
			 * this queue has capacity 2 as at any point of time if will have a maximum of 2 messages in it.
			 * One from left neighbor and one from right neighbor
			 * While processing that message it will remove it from the queue
			 * Also messages from the master requesting to start next round will land here
			 */
			processQueue = new ArrayBlockingQueue<>(6);
			roundQueue = new ArrayBlockingQueue<>(6);
			arrQueue.add(processQueue);
			arrRoundQueue.add(roundQueue);
		}
	}

	/*
	 * if all the values in the masterQueue have the message type value as R, return true
	 * else return false
	 */
	public boolean validateNewRoundStart()
	{
		/*
		 * if number of message in the queue is less than nuumberOfProcesses then return false
		 */
		
		int count = 0;
		
		if(masterQueue.size() < numberOfProcesses)
		{
			return false;
		}

		Message msg;
		
		for(int i = 0; i < numberOfProcesses; i++)
		{
			try 
			{
				//remove the message from the queue and check its type
				msg = masterQueue.take();
				if(msg.getType() != 'R' && msg.getType() != 'L')
				{
					return false;
				}
				
				if(msg.getType() == 'L')
				{
					count++;
					
					if(count == numberOfProcesses)
					{
						completed = true;
						return false;
					}
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	/*
	 * Send to all the processes in their respective message queue that they should be starting the next round
	 */
	public void startNextRound()
	{
		Iterator<BlockingQueue<Message>> it = arrRoundQueue.iterator();
		BlockingQueue<Message> blkq;
		Message msg;
		
		while(it.hasNext())
		{
			blkq = it.next();
			msg = new Message(masterProcessId, 'N', Integer.MIN_VALUE, 'X');
			blkq.add(msg);
		}
	}
	
	public BlockingQueue<Message> getMasterQueue()
	{
		return masterQueue;
	}
	
	public ArrayList<BlockingQueue<Message>> getProcessMasterQueue()
	{
		return arrQueue;
	}
	
	public ArrayList<BlockingQueue<Message>> getRoundQueue()
	{
		return arrRoundQueue;
	}
	
	public boolean isCompleted()
	{
		return completed;
	}

	public static void main(String[] args)
	{
		//accept input from input.dat
		int masterProcessId = 0;
		String currentDirectory = System.getProperty("user.dir");
		BufferedReader inputReader = null;
		int n = 0;
		int[] id = null;
		try {
			inputReader = new BufferedReader(new FileReader(new File(currentDirectory + "/input.dat")));
			String noOfProcesses = inputReader.readLine();
			n = Integer.parseInt(noOfProcesses);
			id = new int[n];
			String processIds = inputReader.readLine();
			String processes[] = processIds.split(" ");
			if(processes.length!=n) {
				System.err.println("Number of process ids not equal to n. Please check input and try again.");
				System.exit(-1);
			}
			for (int i = 0; i < processes.length; i++) {
				id[i] = Integer.parseInt(processes[i]);
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("Input file not found. Please check if the input file is in the same folder.");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Input file incorrect. Please check the input file format and try again.");
			System.exit(-1);
		} catch(NumberFormatException e) {
			System.err.println("Input file contains some non-integer data. Please check the input file and try again.");
			System.exit(-1);
		}
		
		//creating the master process. Master thread is the main thread
		Master masterProcess = new Master(masterProcessId, id);
		
		//creating other threads for HS algorithm simulation
		Process[] processes = new Process[n];
		
		for(int i = 0; i < n; i++)
		{
			processes[i] = new Process(id[i]);
		}
		
		for(int i = 0; i < n; i++)
		{
			//setting the process's incoming queue where message from left and right processes will be written.
			processes[i].setQIn(masterProcess.getProcessMasterQueue().get(i));
			
			//setting the process i's left and right neighboring processes
			if(i == 0)
			{
				processes[i].setLeftNeighbor(processes[(n-1) % n]);
			}
			else
			{
				processes[i].setLeftNeighbor(processes[(i - 1) % n]);
			}
			
			processes[i].setRightNeighbor(processes[(i + 1) % n]);
			processes[i].getOutList().clear();
			
			Message msg1 = new Message(id[i], 'O', 1, 'L');
			Message msg2 = new Message(id[i], 'O', 1, 'R');
			
			processes[i].getOutList().add(msg1);
			processes[i].getOutList().add(msg2);
			
			processes[i].setQRound(masterProcess.getRoundQueue().get(i));
		}
		
		//starting all the threads
		Thread[] t = new Thread[n];
		for(int i = 0; i < n; i++)
		{
			//reference for the queue to which processes will write ready for next round
			processes[i].setQMaster(masterProcess.getMasterQueue());
			t[i] = new Thread(processes[i]);
			t[i].start();
		}
		
		/*
		 * keep looping till HS algorithm is completed
		 */
		while(!masterProcess.isCompleted())
		{
			if(masterProcess.validateNewRoundStart())
			{
				masterProcess.startNextRound();
			}
		}
		
		for(int i = 0; i < n; i++)
		{
			t[i].interrupt();
		}
		
		//waiting till all child thread complete
		for(int i = 0; i < t.length; i++)
		{
			try {
				t[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Completed!!");
	}
}
