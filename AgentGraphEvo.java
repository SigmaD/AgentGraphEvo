import java.util.Random;
import java.io.*;
import java.util.*;

public class AgentGraphEvo {
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int AgentNum; //Agent Number
		int TotGen; //Total generations
		boolean UsePath; //Is a file being used as the network?
		String filepath = "smallworld.txt"; //Location of said file.
		int PotentialDoners; //Number of Potential Donors in each generation
		
		//Deal with arguments
		if (args.length == 4 || args.length == 3) {
			AgentNum = Integer.parseInt(args[0]);
			TotGen = Integer.parseInt(args[1]);
			PotentialDoners = Integer.parseInt(args[2]);
			if (args.length == 3) {
				UsePath = false;
			} else {
				UsePath = true;
				filepath = args[3];
			}
		} else {
			AgentNum = 100;
			TotGen = 30000;
			PotentialDoners = 3;
			UsePath = false;
			//Use setup as proposed in RCA paper.
		}

		double[] TagArr; //Agent Tag
		double[] TolArr; //Agent Tolerence
		double[] OldTolArr;
		double[] OldTagArr;

		//Hamming Distance modification
		int bitstrlen = 10;
		boolean[][] AgentbTag = new boolean[bitstrlen][AgentNum];
		boolean[][] OldAgentbTag;
		boolean HDist = true; //For Testing

		int[] AgentTag; //to consider agent tag over hundred generations
		int[] ScoreArr; //Agent Score
		int DonationCount = 0;
		int MaxDonations = 0;
		double AvgDon = 0;
		TagArr = new double[AgentNum];
		TolArr = new double[AgentNum];
		OldTagArr = new double[AgentNum];
		OldTolArr = new double[AgentNum];
		OldAgentbTag = new boolean[bitstrlen][AgentNum];
		ScoreArr = new int[AgentNum];
		Random randomGen = new Random();
		
		int[][] nodeLink = new int[AgentNum][0];
		
		System.out.println (nodeLink[0].length);
		//nodeLink[0] = new int[9];
		//System.out.println (nodeLink[0].length);
		
		//Generate Agents
		for (int idx=0; idx<AgentNum; ++idx) {
			//Generate boolean array
			if (HDist) {
				for (int subidx=0; subidx<bitstrlen; ++subidx) {
					if (Math.random() > 0.5) {
						AgentbTag[subidx][idx] = true;
					} else {
						AgentbTag[subidx][idx] = false;
					}
				}
			} else {
				TagArr[idx] = Math.random();
			}
			TolArr[idx] = Math.random();
		}
		//If a network is given, load the configuration into memory.
		if (UsePath == true) {
			//Read in graph
			try{
				// Open the file that is the first 
				// command line parameter
				FileInputStream fstream = new FileInputStream(filepath);
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				char strFirst;
				String[] nodelist;
				int[] tempNodeArr;
				//Read File Line By Line
				while ((strLine = br.readLine()) != null)   {
					//String Operations go here
					strFirst = strLine.charAt(0);
					if (strFirst != '#') {
						nodelist = strLine.split("\t");
						//System.out.println (nodelist[0]+" "+nodelist[1]);
						
						//Find edges of graph
						int nodeFrom = Integer.parseInt(nodelist[0]);
						int nodeTo = Integer.parseInt(nodelist[1]);
						
						//Assumes non-directed case
						
						//From-To
						tempNodeArr = new int[nodeLink[nodeFrom].length];
					    	System.arraycopy(nodeLink[nodeFrom], 0, tempNodeArr, 0, nodeLink[nodeFrom].length);
					    	nodeLink[nodeFrom] = new int[nodeLink[nodeFrom].length + 1];
					    	System.arraycopy(tempNodeArr, 0, nodeLink[nodeFrom], 0, tempNodeArr.length);
						nodeLink[nodeFrom][nodeLink[nodeFrom].length-1] = (int)nodeTo;
						
						//To-From
						tempNodeArr = new int[nodeLink[nodeTo].length];
					    	System.arraycopy(nodeLink[nodeTo], 0, tempNodeArr, 0, nodeLink[nodeTo].length);
					    	nodeLink[nodeTo] = new int[nodeLink[nodeTo].length + 1];
					    	System.arraycopy(tempNodeArr, 0, nodeLink[nodeTo], 0, tempNodeArr.length);
						nodeLink[nodeTo][nodeLink[nodeTo].length-1] = (int)nodeFrom;
					}
				
				}
				//Close the input stream
				in.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}
		//End of graph read block
		
		for (int GenNum=0; GenNum<TotGen; ++GenNum) {
		
			//Donation Step
			int CompAgent;
			MaxDonations = 0;
			DonationCount = 0;
			for (int idx=0; idx<AgentNum; ++idx) {
				if (nodeLink[idx].length > 0 || UsePath == false) { //Check that the node is connected to anything
					for (int PotIdx=0; PotIdx<PotentialDoners; ++PotIdx) {
						
						//Selection of agent to compare to.
						
						if (UsePath == false) {
							//Random across all agents
							//Accounts for inability for agents to select themselves.
							CompAgent = randomGen.nextInt(AgentNum-1);
							if (CompAgent>=idx) {
								CompAgent++;
							}
						} else {
							//Random across connected agents
							CompAgent = nodeLink[idx][randomGen.nextInt(nodeLink[idx].length)];
						}
						MaxDonations++; //Increment the number of possible donations
						if (HDist) {
							if (hammingDistance(AgentbTag,idx,CompAgent) <= TolArr[idx]*bitstrlen) {
								ScoreArr[idx] = ScoreArr[idx] - 1;
                                                        	ScoreArr[CompAgent] = ScoreArr[CompAgent] + 10;
                                                        	DonationCount++;
							}
						} else {
							//Donation computation for non-HDist
							if (Math.abs(TagArr[idx]-TagArr[CompAgent]) <= TolArr[idx]) {
								ScoreArr[idx] = ScoreArr[idx] - 1;
								ScoreArr[CompAgent] = ScoreArr[CompAgent] + 10;
								DonationCount++;
								//System.out.println("Agent "+idx+" donates to agent "+CompAgent); //Debug Monitor
							}
						}
					}
				}
			}
			AvgDon = AvgDon +((double)DonationCount/(double)(MaxDonations))*100;
				//System.out.println(GenNum + ": ");
			if (GenNum%100==0) {
				System.out.println("AvgDon: " + ((double)DonationCount/(double)(AgentNum*PotentialDoners))*100); //prints Donation Rate (percentage) this generation.
				AgentTag = new int[100];
				double AvgTol;
				int TrueAgentNum;
				AvgTol = 0;
				TrueAgentNum = 0;
				//HDIST vars
				int HAgenttag[];
				HAgenttag = new int[bitstrlen];
				// Examine Tag Clustering if not HDist
				if (HDist == false) {
					for (int agnt = 0; agnt < AgentNum; agnt++) {
						if (UsePath == false || nodeLink[agnt].length != 0) {	//Removed disconnected nodes(that cannot change) from count.
							AgentTag[(int)(100*TagArr[agnt])]++;
							AvgTol = AvgTol + TolArr[agnt];
							TrueAgentNum++;
						}
					}
					AvgTol = AvgTol/TrueAgentNum;
					System.out.println(Arrays.toString(AgentTag));
					System.out.println("AvgTol: " + AvgTol);
				} else {
					System.out.println("HDist in use");
					for (int agnt = 0; agnt < AgentNum; agnt++) {
						if (UsePath == false || nodeLink[agnt].length != 0) {
							for (int hidx = 0; hidx < bitstrlen; hidx++) {
								if (AgentbTag[hidx][agnt] == true) {
									HAgenttag[hidx]++;
								}
							}
						}
					}
					System.out.println(Arrays.toString(HAgenttag));
				}
			}
			

			//Evolution Step
			
			for (int idx=0; idx<AgentNum; ++idx) {
				OldTolArr[idx]=TolArr[idx];
				//If using hamming distance modification
				if (HDist) {
                               		for (int subidx=0; subidx<bitstrlen; ++subidx) {
						OldAgentbTag[subidx][idx]=AgentbTag[subidx][idx];
					}
                        	} else {
                                	OldTagArr[idx] = TagArr[idx];
                        	}
			}
			
			
			for (int idx=0; idx<AgentNum; ++idx) {
			if (nodeLink[idx].length > 0 || UsePath == false) { //Check that the node is connected to anything

				//Accounts for inability for agents to select themselves in false case.
				if (UsePath == false) {
					CompAgent = randomGen.nextInt(AgentNum-1);
					if (CompAgent>=idx) {
						CompAgent++;
					}
				} else {
					CompAgent = nodeLink[idx][randomGen.nextInt(nodeLink[idx].length)];
				}

				//Compare each agent to random other agent and adopt better scoring stats
				if (ScoreArr[idx]>=ScoreArr[CompAgent]) {
					//Mutation of Tolerence(Gaussian)
					if (Math.random()<0.1) {
						TolArr[idx] = OldTolArr[idx]+(randomGen.nextGaussian()*0.01);
						//TolArr[idx] = Math.random();
						if (TolArr[idx]<0) {TolArr[idx]=0;} //Tolerences cannot be negative
					} else {
						TolArr[idx] = OldTolArr[idx];
					}
					//Mutation of Tag (Random New Tag) - Checks for use of HDist
					if (Math.random()<0.1) {
						if (HDist) {
							for (int subidx=0; subidx<bitstrlen; ++subidx) {
                                        			if (Math.random() > 0.5) {
                                                			AgentbTag[subidx][idx] = true;
                                        			} else {
                                                			AgentbTag[subidx][idx] = false;
                                				}
							}
						} else {
							TagArr[idx] = Math.random();
						}
					} else {
						if (HDist) {
							for (int subidx=0; subidx<bitstrlen; ++subidx) {
								AgentbTag[subidx][idx] = OldAgentbTag[subidx][idx];
							}
						} else {
							TagArr[idx] = OldTagArr[idx];
						}
					}
				} else {
					//Mutation of Tolerence(Gaussian)
					if (Math.random()<0.1) {
						TolArr[idx] = OldTolArr[CompAgent]+(randomGen.nextGaussian()*0.01);
						//TolArr[idx] = Math.random();
						if (TolArr[idx]<0) {TolArr[idx]=0;} //Tolerences cannot be negative
					} else {
						TolArr[idx] = OldTolArr[CompAgent];
					}
					//Mutation of Tag (Random New Tag) - checks for use of HDist
					if (Math.random()<0.1) {
						if (HDist) {
                                                        for (int subidx=0; subidx<bitstrlen; ++subidx) {
                                                                if (Math.random() > 0.5) {
                                                                        AgentbTag[subidx][idx] = true;
                                                               	} else {
                                                                        AgentbTag[subidx][idx] = false;
                                                               	}
                                                       	}
                                               	} else {
                                                        TagArr[idx] = Math.random();
                                                }
					} else {
						if (HDist) {
                                                       	for (int subidx=0; subidx<bitstrlen; ++subidx) {
                                                               	AgentbTag[subidx][idx] = OldAgentbTag[subidx][CompAgent];
                                                       	}
                                               	} else {
                                                        TagArr[idx] = OldTagArr[CompAgent];
                                                }
					}
				}
			}
			}
			//Reset Scores
			for (int idx=0; idx<AgentNum; ++idx) {
				ScoreArr[idx]=0;
			}
			
		
		}
		
		//Debug Score Printout
		//for (int idx=0; idx<AgentNum; ++idx) {
		//	System.out.println(TolArr[idx]);
		//}
		System.out.println("---");
		System.out.println("Average Donation Rate / Generation: " + AvgDon/TotGen);
		
	}
	static int hammingDistance(boolean[][] array1, int agt1, int agt2) {
		int result = 0;
		for(int i = 0; i < array1.length; i++) {
			if(array1[i][agt1] != array1[i][agt2]) { result++; }
		}
		return result;
	}

}

