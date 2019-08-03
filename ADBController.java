package de.craftkekser.pokefarmer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ADBController {

	private File adbDir;
	
	public ADBController(File adbDir) {
		this.adbDir = adbDir;
	}
	
	public File getAdbDir() {
		return adbDir;
	}

	public void setAdbDir(File adbDir) {
		this.adbDir = adbDir;
	}

	public void initDevices() throws IOException {
		sendCommand("devices");
	}
	
	public void sendSwipe(int x1, int y1, int x2, int y2, int duration) throws IOException {
		sendCommand("shell", "input", "swipe", x1, y1, x2, y2, duration);
	}
	
	
	public BufferedImage screenshot() throws IOException {
		File tmpimg = new File("screenshot" + System.currentTimeMillis() + ".png");
		String remoteFile = "/sdcard/gameScreenshot.png";
		sendCommand("shell", "screencap", remoteFile);
		sendCommand("pull", remoteFile, tmpimg.getAbsolutePath());
		BufferedImage bi = ImageIO.read(tmpimg);
		tmpimg.delete();
		return bi;
	}
	
	public void sendTap(int x, int y) throws IOException {
		sendCommand("shell", "input", "tap", x, y);
	}
	
	public int sendCommand(Object... args) throws IOException {
		String command = this.getAdbDir().getAbsolutePath() + File.separator + "adb.exe";
		if(!System.getProperty("os.name").toLowerCase().contains("win")) {
			command = "adb";
		}
		String[] array = new String[args.length+1];
		array[0] = command;
		for(int i = 1; i < array.length; i++) {
			array[i] = String.valueOf(args[i-1]);
		}
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.command(array);
//		pb.inheritIO();
		Process p = pb.start();
		while(p.isAlive()) {}
		return p.exitValue();
	}
	
}
