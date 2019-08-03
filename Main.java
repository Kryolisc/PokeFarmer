package de.craftkekser.pokefarmer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Main {

	public static final String VERSION = "1.0";

	public static void main(String[] args) throws IOException {
		File adbFile = new File("C:\\adb");
		if(args.length==1) {
			adbFile = new File(args[0]);
		}
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

		log("The program will now take a screenshot to determine the touch-points automatically...");
		BufferedImage screenshot = controller.screenshot();
		log(" > Size: " + screenshot.getWidth() + "x" + screenshot.getHeight());

		int w = screenshot.getWidth();
		int h = screenshot.getHeight();
		int x1 = w - (w/5);
		int y1 = h/2 - 10;
		int x2 = x1 - (w-(w/5)*2);
		int y2 = h/2 + 10;
		
		int swipeTime = 279;
		int delay = 60*5*1000+40000;

		log(" > X1=" + x1);
		log(" > Y1=" + y1);
		log(" > X2=" + x2);
		log(" > Y2=" + y2);

		log("");
		for(int i = 5; i > 0; i--) {
			log("Starting in " + i + " seconds...");
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		}

		log("");
		System.out.println("To stop the bot, press ^C or close your terminal");
		log("______________________________________________________________________________________________________\n");

		while(true) {
			
			int ttw = delay;
			Random r = new Random();
			
			int ttwOffset = r.nextInt(60*1000);
			int xOffset = 40-r.nextInt(20);
			int yOffset = 80-r.nextInt(40);
			int swtOffset = r.nextInt(40);
			
			ttw = ttw+ttwOffset;
			
			System.out.println("Spinning...");
			controller.sendSwipe(x1+xOffset, y1+yOffset, x2-xOffset, y2-yOffset, swipeTime+swtOffset);
			System.out.println("Next spin in " + ttw + "ms (~" + (ttw/1000/60) + "min)");
			
			try { Thread.sleep(ttw); } catch (InterruptedException e) { }
			
		}

	}


	public static void log(String message) {
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + message);
	}

}
