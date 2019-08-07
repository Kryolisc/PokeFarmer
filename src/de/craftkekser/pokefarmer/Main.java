package de.craftkekser.pokefarmer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

import de.craftkekser.pokefarmer.ImageProcessing.PokestopState;
import de.craftkekser.pokefarmer.ImageProcessing.PokestopState.PokestopStateValue;

public class Main {

	public static final String VERSION = "1.2";
	volatile public static double RELVALUE = 0.61; // This value is slightly weather-dependent

	volatile public static long ttw = 0;
	volatile public static long ttwStart = 0;
	volatile public static long ttwEnd = 0;
	volatile public static boolean running = true;

	public static void main(String[] args) throws IOException {
		// Get the adb directory from parameters
		File adbFile = new File("C:\\adb");
		if(args.length==1) {
			adbFile = new File(args[0]);
		}
		if(args.length==2) {
			RELVALUE = Double.parseDouble(args[1]);
		}

		// New ADBController instance for communication
		ADBController controller = new ADBController(adbFile);

		log("Loading...");
		controller.initDevices();
		log("");
		log("");
		log("=============================");
		log("PokeFarmer v" + VERSION + "");
		log("=============================");

		log("Please note that you have to be in the range of the Pokestop you want to use.");
		log("Also, you have to manually open the Pokestop. You may not use your device as usual while the bot is active.");

		log("______________________________________________________________________________________________________");

		log("The program will now take a screenshot to determine the touch-points and color values automatically.");
		log("Please make sure that you have a valid Pokestop on screen (it must be able to spin).");

		log("");

		// Delay (to make sure everything is visible)
		for(int i = 5; i > 0; i--) {
			log("You have " + i + " seconds left");
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		}

		log("");

		// Take a screenshot to calculate the required points
		BufferedImage screenshot = controller.screenshot();
		log(" > Size: " + screenshot.getWidth() + "x" + screenshot.getHeight());
		PokestopState probeState = ImageProcessing.getPokestopState(screenshot, 1.0D);

		int w = screenshot.getWidth();
		int h = screenshot.getHeight();
		// Swipe start
		int x1 = w - (w/5);
		int y1 = h/2 - 10;
		// Swipe end
		int x2 = x1 - (w-(w/5)*2);
		int y2 = h/2 + 10;

		// Timing, not randomized
		int swipeTime = 279;
		int delay = 60*5*1000-20000;

		log(" > X1=" + x1);
		log(" > Y1=" + y1);
		log(" > X2=" + x2);
		log(" > Y2=" + y2);
		if(probeState.getState()!=PokestopStateValue.AWAY) {
			RELVALUE = probeState.getRelValue()+0.02D;
		}else {
			log("[unable to determine REL, using default]");
		}
		log(" > REL=" + RELVALUE);

		log("");

		// Delay (for safety)
		for(int i = 5; i > 0; i--) {
			log("Starting in " + i + " seconds...");
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		}

		log("");
		System.out.println("To stop the bot, press ^C or close your terminal");
		log("______________________________________________________________________________________________________\n");

		Random r = new Random();

		// Command handler
		Thread commands = new Thread(new Runnable() {

			@Override
			public void run() {
				// Open new Scanner with System.in
				Scanner scan = new Scanner(System.in);

				while(running) {
					// Get next line (command)
					String line = scan.nextLine().trim();

					// Force spin
					if(line.equalsIgnoreCase("forcespin")) {
						log("Spinning... (Forcespin)");
						// Randomize values
						int xOffset = 40-r.nextInt(20);
						int yOffset = 80-r.nextInt(40);
						int swtOffset = r.nextInt(40);
						// Check state
						PokestopState state = ImageProcessing.getPokestopState(screenshot, RELVALUE);
						try {
							state = ImageProcessing.getPokestopState(controller.screenshot(), RELVALUE);
						} catch (IOException e1) {}
						log("  > State: " + state.getState().name() + " (" + state.getRelValue() + ")");
						if(state.getState() != PokestopStateValue.AVAILABLE) {
							log("  (Warning) The Pokestop is in an invalid state. (" + state.getRelValue() + ")");
						}
						try {
							// Send swipe command
							controller.sendSwipe(x1+xOffset, y1+yOffset, x2-xOffset, y2-yOffset, swipeTime+swtOffset);
							// If the state was AVAILABLE before, the internal delay until the next spin has to be reset, as the Pokestop has been used.
							// Otherwise, the state has not changed, so the timer can stay as it is
							if(state.getState() == PokestopStateValue.AVAILABLE) {
								int ttwOffset = r.nextInt(60*1000);
								ttw = delay+ttwOffset;
								ttwStart = System.currentTimeMillis();
								ttwEnd = ttw + ttwStart;
								log("Swipe successful. Timing reset to " + ttw + "ms (~" + (ttw/1000/60) + "min)");
							}else {
								log("Swipe successful. No timing reset");
							}
						} catch (IOException e) {
							log("Error: " + e.getMessage());
						}
					}
					// Set Time (in ms)
					else if(line.toLowerCase().startsWith("settime")) {
						try {
							int newTime = Integer.parseInt(line.toLowerCase().replace("settime", "").trim());
							ttw = newTime;
							ttwStart = System.currentTimeMillis();
							ttwEnd = ttw + ttwStart;
							log("Time set to " + newTime + "ms (~" + (newTime/1000/60) + "min)");
						}catch(NumberFormatException ex) {
							log("Invalid time: must be a number (milliseconds)");
						}
					}
					// Set relation value
					else if(line.toLowerCase().startsWith("setrel")) {
						try {
							double newRel = Double.parseDouble(line.toLowerCase().replace("setrel", "").trim());
							RELVALUE = newRel;
							log("Relation value set to " + newRel + "");
						}catch(NumberFormatException ex) {
							log("Invalid time: must be a number (milliseconds)");
						}
					}
					// Get remaining time
					else if(line.equalsIgnoreCase("remaining")) {
						long remaining = ttwEnd-System.currentTimeMillis();
						log("Time remaining: " + remaining + " ms (~" + (remaining/1000/60) + "min)  [" + ttw + ", " + ttwStart + ", " + ttwEnd + ", " + System.currentTimeMillis() + ", " + (ttwEnd-System.currentTimeMillis()) + "]");
					}
					// Stop
					else if(line.equalsIgnoreCase("stop")) {
						log("Stopping...");
						running=false;
						ttw = 0;
						ttwEnd = 0;
					}
					// State
					else if(line.equalsIgnoreCase("state")) {
						PokestopState state;
						log("...");
						try {
							state = ImageProcessing.getPokestopState(controller.screenshot(), RELVALUE);
							log("State: " + state.getState().name() + " (" + state.getRelValue() + ")");
						} catch (IOException e) {
							log("State: Unknown");
						}
					}

				}
				scan.close();
			}
		});
		commands.start();

		while(running) {

			// Randomizing the values a bit
			int ttwOffset = r.nextInt(60*1000);
			int xOffset = 40-r.nextInt(20);
			int yOffset = 80-r.nextInt(40);
			int swtOffset = r.nextInt(40);

			ttw = delay+ttwOffset;

			log("Spinning...");

			// Get the Pokestop-state by taking a screenshot and evaluating some color values
			PokestopState state = ImageProcessing.getPokestopState(controller.screenshot(), RELVALUE);
			int rettime = 0;
			// While something is wrong, try again until it works
			while(state.getState()!=PokestopStateValue.AVAILABLE && running) {
				log("  > State: " + state.getState().name() + " (" + state.getRelValue() + ")");
				switch(state.getState()) {
				case USED:
					// The Pokestop is used. Usually, the delay was a bit too short.
					// Therefore, the program will wait a few seconds and check the status again, until the Pokestop is available
					rettime = r.nextInt(20000)+10000;
					log("  (Warning) The Pokestop is in an invalid state. Retrying in " + (rettime/1000) + " seconds");
					try { Thread.sleep(rettime); } catch (InterruptedException e) { }
					state = ImageProcessing.getPokestopState(controller.screenshot(), RELVALUE);
					break;
				case AWAY:
					// The Pokestop is too far away. There is nothing the program can do about that, so the user will be warned
					// The program will wait a few seconds longer than it would when the Pokestop is used and check the status again, until the Pokestop is available
					rettime = r.nextInt(30000)+10000;
					log("  (Warning) The Pokestop is too far away. Retrying in " + (rettime/1000) + " seconds");
					log("  Please make sure that the Pokestop is reachable from your current location. However, don't bring yourself or anyone else in danger by your actions.");
					try { Thread.sleep(rettime); } catch (InterruptedException e) { }
					state = ImageProcessing.getPokestopState(controller.screenshot(), RELVALUE);
					break;
				default:
					// If everything is OK, continue with spinning
					break;
				}
			}

			// Send a swipe using the coordinates calculated and randomized before to 
			controller.sendSwipe(x1+xOffset, y1+yOffset, x2-xOffset, y2-yOffset, swipeTime+swtOffset);
			log("Next spin in " + ttw + "ms (~" + (ttw/1000/60) + "min)");

			// Awaiting every millisecond independently, so the time can always be changed with immediate effect.
			ttwStart = System.currentTimeMillis();
			ttwEnd = ttw + ttwStart;
			boolean printed = false;
			while(ttwEnd > System.currentTimeMillis()) {
				// Waiting...
				long difference = ttwEnd-System.currentTimeMillis();
				if(difference == 30000) {
					if(!printed)
						log("Next spin in 30 seconds");
					printed=true;
				}else {
					if(difference > 5100)
						printed=false;
				}
				if(difference < 5100) {
					if((difference%1000)==0) {
						if(!printed)
							log("Next spin in " + (difference/1000) + " seconds");
						printed=true;
					}else {
						printed=false;
					}
				}
			}

		}

	}


	public static void log(String message) {
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + message);
	}

}
