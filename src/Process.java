import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

/*
 * Team Memebers: Hardik Trivedi (hpt150030)
 * 				  Roshan Ravikumar (rxr151330)
 * 				  Sapan Gandhi (sdg150130) 
 */

public class Process implements Runnable 
{
	private int processId;
	private BlockingQueue<Message> qIn, qMaster, qRound;
	private Process leftNeighbor, rightNeighbor;
	private boolean leaderFound;
	private int leaderId;
	private int phase;
	private boolean leftWriteDone = false;
	private boolean rightWriteDone = false;

	private ArrayList<Message> outList = new ArrayList<Message>();

	public Process(int processId)
	{
		this.processId = processId;
		leaderFound = false;
		leaderId = Integer.MIN_VALUE;
		phase = 0;
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
				System.out.println("Process "+processId+" terminated!");
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
						break;
					}
				}

				synchronized (leftNeighbor) {
					leftNeighbor.setRightWriteDone(true);
					leftNeighbor.notify();
				}

				synchronized (rightNeighbor) {
					rightNeighbor.setLeftWriteDone(true);
					rightNeighbor.notify();
				}

				outList.clear();

				//waiting till neighboring processes have finished writing
				while(true)
				{
					synchronized (this)
					{
						if(leftWriteDone && rightWriteDone)
						{
							break;
						}
						try {
							this.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
								if(!leaderFound)
								{
									leaderFound = true;
									leaderId = processId;

									System.out.println("Process "+ processId+" says \'I'm leader\'");

									Message leaderTokenr = new Message(processId, 'L', 1, 'L');

									//sending token to neighbors
									outList.add(leaderTokenr);
								}

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
							System.out.println("Process "+  processId+" knows "+leaderId+" is the leader");
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
