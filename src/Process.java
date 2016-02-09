import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class Process implements Runnable 
{
	private int processId;
	private BlockingQueue<Message> qIn, qMaster, qRound;
	private Process leftNeighbor, rightNeighbor;
	private boolean leaderFound;
	private int roundNo;

	/*
	 * U - unknown
	 * L - Leader
	 * N - not leader
	 */
	//initialized to unknown
	private char myStatus;
	private int leaderId;
	private int phase;
	private boolean leftWriteDone = false;
	private boolean rightWriteDone = false;
	private boolean isRunning = true;

	private ArrayList<Message> outList = new ArrayList<Message>();

	public Process(int processId)
	{
		this.processId = processId;
		leaderFound = false;
		myStatus = 'U';
		leaderId = Integer.MIN_VALUE;
		phase = 0;
		roundNo = 0;
	}

	public ArrayList<Message> getOutList()
	{
		return outList;
	}
	
	public void setQMaster(BlockingQueue<Message> qMaster)
	{
		this.qMaster = qMaster;
	}

	public void setQIn(BlockingQueue<Message> qIn)
	{
		this.qIn = qIn;
	}
	
	public BlockingQueue<Message> getQIn()
	{
		return qIn;
	}

	public void setLeftWriteDone(boolean leftWriteDone)
	{
		this.leftWriteDone = leftWriteDone;
	}

	public void setRightWriteDone(boolean rightWriteDone)
	{
		this.rightWriteDone = rightWriteDone;
	}
	
	public void setLeftNeighbor(Process leftNeighbor)
	{
		this.leftNeighbor = leftNeighbor;
	}

	public void setRightNeighbor(Process rightNeighbor)
	{
		this.rightNeighbor = rightNeighbor;
	}
	
	public void setQRound(BlockingQueue<Message> qRound)
	{
		this.qRound = qRound;
	}

	@Override
	public void run() 
	{
		Message tokenl, tokenr = null; 
		while(true)
		{
			//waiting new round start message
			Message msg = null;
			try {
				msg = qRound.take();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				System.out.println(processId+" completed");
				break;
			}
			if(msg.getType() == 'N')
			{
				//send token both sides if status = U

				/*
				 * Read from outList and send
				 */
				Iterator<Message> it = outList.iterator();
				Message sendMsg;
				while(it.hasNext())
				{
					sendMsg = it.next();
					if(sendMsg.getFromDir() == 'L')
					{
						try {
							rightNeighbor.getQIn().put(sendMsg);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(sendMsg.getFromDir() == 'R')
					{
						try {
							leftNeighbor.getQIn().put(sendMsg);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					if(sendMsg.getType() == 'L')
					{
						isRunning = false;
						break;
					}
				}
				
				leftNeighbor.setRightWriteDone(true);
				rightNeighbor.setLeftWriteDone(true);
				
				/*if(!isRunning)
					break;*/

				outList.clear();

				//waiting till neighboring processes have finished writing
				while(true)
				{
					if(leftWriteDone && rightWriteDone)
					{
						break;
					}
				}

				leftWriteDone = false;
				rightWriteDone = false;

				Message m1 = null;
				int sameInCount = 0;

				if(!qIn.isEmpty())
				{
					while(qIn.size() > 0)
					{
						try {
							m1 = qIn.take();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if(m1.getType() == 'O')
						{
							if(m1.getProcessId() > processId)
							{
								
								if(m1.getHops() > 1)
								{
									if(m1.getFromDir() == 'R')
									{
										tokenr = new Message(m1.getProcessId(), 'O', m1.getHops() - 1, 'R');
										outList.add(tokenr);
									}
									else if(m1.getFromDir() == 'L')
									{
										tokenl = new Message(m1.getProcessId(), 'O', m1.getHops() - 1, 'L');
										outList.add(tokenl);
									}
								}
								else if(m1.getHops() == 1)
								{
									if(m1.getFromDir() == 'R')
									{
										tokenr = new Message(m1.getProcessId(), 'I', Integer.MIN_VALUE, 'L');
										//TODO
										//put to queue
										outList.add(tokenr);
									}
									else if(m1.getFromDir() == 'L')
									{
										tokenl = new Message(m1.getProcessId(), 'I', Integer.MIN_VALUE, 'R');
										//put to queue
										outList.add(tokenl);
									}
								}
							}
							else if(m1.getProcessId() < processId)
							{
								
							}
							else
							{
								myStatus = 'L';
								leaderFound = true;
								leaderId = processId;
								
								System.out.println(processId+" I'm leader");

								//announce..code change is required
								//assembling a token containing id, phase, and direction=out and fromDir
								Message leaderTokenr = new Message(processId, 'L', 1, 'L');

								//sending token to neighbors
								outList.add(leaderTokenr);

							}
						}
						else if(m1.getType() == 'I')
						{
							if(m1.getProcessId() != processId)
							{
								Message token = new Message(m1.getProcessId(), 'I', Integer.MIN_VALUE, m1.getFromDir());
								//sending token to neighbors
								outList.add(token);
							}
							else if(m1.getProcessId() == processId)
							{
								
								sameInCount++;
								if(sameInCount == 2)
								{
									phase++;
									Message msg1 = new Message(processId, 'O', Math.pow(2, phase), 'L');
									Message msg2 = new Message(processId, 'O', Math.pow(2, phase), 'R');
									outList.add(msg1);
									outList.add(msg2);
								}
							}
						}
						else if(m1.getType() == 'L')
						{
							leaderFound = true;
							leaderId = m1.getProcessId();
							outList.add(m1);
							System.out.println(processId+" He is leader:"+leaderId);
						}
					}
				}
			}
			
			Message ready = null;
			if(leaderFound)
			{
				ready = new Message(processId, 'L', Integer.MIN_VALUE, 'X');
			}
			else
			{
				ready = new Message(processId, 'R', Integer.MIN_VALUE, 'X');
			}
			
			//sending token to neighbors
			try {
				qMaster.put(ready);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
