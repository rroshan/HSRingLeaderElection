Steps to Execute

1) The submission will contain source code, README.txt, sample input.dat files.
2) Whiles executing the program the input.dat file should be in the same folder as the java files.

	Run the commands in the following order.
	3) javac Master.java
	4) java Master > output.txt
	5) Output should be visible on the console. The leader will announce that he is the leader and each process will display that it has found the leader.
	6) All threads will terminate.

UTD Machine Tested on:
cs1.utdallas.edu
cs2.utdallas.edu

Java Version tested on:
java version "1.8.0_45"
Java(TM) SE Runtime Environment (build 1.8.0_45-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.45-b02, mixed mode)




******************************************************************************************************************************************************************************************

This program simulates HS Leader Election algorithm in Synchronized Ring in Java. Threads are used to simulate each process in a Distributed Environment. The processes communicate with each other using Thread Safe Blocking Queues.
None of the processes know the number of processes in the distributed system and only know their respective IDs and their left and right neighbors in the Bidirectional Ring. All the processes execute the same code.

Sample Input
First Line denotes the number of processes in the in the distributed enivronment (Threads).
The second Line contains a space delimited Process ID.

30
529 702 346 697 271 272 607 182 383 193 775 789 363 734 602 599 735 783 429 880 183 920 353 162 914 437 545 603 778 479

Sample Output:
Process 920 says 'I'm leader'
Process 920 says 'I'm leader'
Process 353 knows 920 is the leader
Process 162 knows 920 is the leader
Process 914 knows 920 is the leader
Process 437 knows 920 is the leader
Process 545 knows 920 is the leader
Process 603 knows 920 is the leader
Process 778 knows 920 is the leader
Process 479 knows 920 is the leader
Process 529 knows 920 is the leader
Process 702 knows 920 is the leader
Process 346 knows 920 is the leader
Process 697 knows 920 is the leader
Process 271 knows 920 is the leader
Process 272 knows 920 is the leader
Process 607 knows 920 is the leader
Process 182 knows 920 is the leader
Process 383 knows 920 is the leader
Process 193 knows 920 is the leader
Process 775 knows 920 is the leader
Process 789 knows 920 is the leader
Process 363 knows 920 is the leader
Process 734 knows 920 is the leader
Process 602 knows 920 is the leader
Process 599 knows 920 is the leader
Process 735 knows 920 is the leader
Process 783 knows 920 is the leader
Process 429 knows 920 is the leader
Process 880 knows 920 is the leader
Process 183 knows 920 is the leader
Process 529 terminated!
Process 697 terminated!
Process 271 terminated!
Process 182 terminated!
Process 775 terminated!
Process 363 terminated!
Process 599 terminated!
Process 429 terminated!
Process 353 terminated!
Process 437 terminated!
Process 702 terminated!
Process 346 terminated!
Process 479 terminated!
Process 603 terminated!
Process 778 terminated!
Process 545 terminated!
Process 914 terminated!
Process 162 terminated!
Process 920 terminated!
Process 880 terminated!
Process 183 terminated!
Process 783 terminated!
Process 735 terminated!
Process 734 terminated!
Process 602 terminated!
Process 789 terminated!
Process 193 terminated!
Process 383 terminated!
Process 607 terminated!
Process 272 terminated!
Completed!!

