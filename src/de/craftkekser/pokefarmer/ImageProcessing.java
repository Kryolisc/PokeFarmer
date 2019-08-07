package de.craftkekser.pokefarmer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.craftkekser.pokefarmer.ImageProcessing.PokestopState.PokestopStateValue;

public class ImageProcessing {

	
	/*
	 * Determines the state a Pokestop is in by a given Image (typically a screenshot)
	 * 
	 * States:
	 *   - AWAY(2):       The Pokestop is too far away
	 *   - USED(1):       The Pokestop has already been used (~5min timeout has not yet expired)
	 *   - AVAILABLE(0):  The Pokestop is ready to be used
	 * 
	 * @param img BufferedImage containing a screenshot of the Pokestop
	 * @param maxRel The value by which the state will be determined (~0.47 can be a good value for rain, ~0.63 for sunny days)
	 * @returns The state with the current relation value
	 * 
	 */
	public static PokestopState getPokestopState(BufferedImage img, double maxRel) {

		// Scale down image
		int w = img.getWidth()/3;
		int h = img.getHeight()/3;

		BufferedImage scaled = scale(img, w, h);

		// Check for out of reach
		// Extracting the important area
		BufferedImage oorChunk = scaled.getSubimage(w/5, (int) (h*0.788), (w/5)*3, (int) (h*0.035));
		int oorchunkSize = oorChunk.getWidth()*oorChunk.getHeight();
		int ored = 0;
		int ogreen = 0;
		int oblue = 0;
		for(int px = 0; px < oorChunk.getWidth(); px++) {
			for(int py = 0; py < oorChunk.getHeight(); py++) {
				Color pixel = new Color(oorChunk.getRGB(px, py));
				ored += pixel.getRed();
				ogreen += pixel.getGreen();
				oblue += pixel.getBlue();
			}
		}
		Color oorColor = new Color(ored/oorchunkSize, ogreen/oorchunkSize, oblue/oorchunkSize);
		// If that color has too much red in it, return the state and don't even bother with the rest
		double rg = (double)oorColor.getRed()/(double)oorColor.getGreen();
		double rb = (double)oorColor.getRed()/(double)oorColor.getBlue();
		if(rg > 0.98 && rb > 0.72) {
			return new PokestopState(rg, PokestopStateValue.AWAY);
		}


		// Divide into 12 sections and calculate average color
		// As the color of the center chunks is not actually that important to the state of the Pokestop, they will be ignored.
		// (They are mostly background and Pokestop picture)
		Color[] colors = new Color[6];
		int wstep = w/3;
		int hstep = h/4;
		int counter = 0;
		// Iterating through before mentioned sections
		for(int intx = 0; intx < 3; intx++) {
			for(int inty = 0; inty < 4; inty++) {
				if(inty==0||inty==3) { // <- ignoring center parts
					// Create subimage, add up all color values of each individual pixel and get average
					BufferedImage chunk = scaled.getSubimage(wstep*intx, hstep*inty, wstep, hstep);
					int chunkSize = chunk.getWidth()*chunk.getHeight();
					int red = 0;
					int green = 0;
					int blue = 0;
					for(int px = 0; px < chunk.getWidth(); px++) {
						for(int py = 0; py < chunk.getHeight(); py++) {
							Color pixel = new Color(chunk.getRGB(px, py));
							red += pixel.getRed();
							green += pixel.getGreen();
							blue += pixel.getBlue();
						}
					}
					colors[counter] = new Color(red/chunkSize, green/chunkSize, blue/chunkSize);
					counter++;
				}
			}	
		}

		// Get overall color
		int r = 0;
		int g = 0;
		int b = 0;
		for(Color c : colors) {
			r+=c.getRed();
			g+=c.getGreen();
			b+=c.getBlue();
		}

		Color theColor = new Color(r/6, g/6, b/6);

		// Red-Blue-Relation
		double rel = (double)theColor.getRed()/(double)theColor.getBlue();

		
		// Check rel value according to given parameter
		if(rel < maxRel) {
			return new PokestopState(rel, PokestopStateValue.AVAILABLE);
		}else {
			return new PokestopState(rel, PokestopStateValue.USED);
		}

	}
	
	
	/*
	 * Scales an image by redrawing it
	 * 
	 * @param img The image to be scaled
	 * @param w The new width
	 * @param h The new height
	 * @returns The scaled image
	 */
	public static BufferedImage scale(BufferedImage img, int w, int h) {
		BufferedImage scaled = new BufferedImage(w, h, img.getType());
		Graphics2D scaled2D = scaled.createGraphics();
		scaled2D.drawImage(img, 0, 0, w, h, null);
		return scaled;
	}
	
	/*
	 * Class for storing the state of a Pokestop	 * 
	 */
	public static class PokestopState{
		private double relValue;
		private PokestopStateValue state;
		
		public PokestopState(double relValue, PokestopStateValue state) {
			this.setRelValue(relValue);
			this.setState(state);
		}
		
		public double getRelValue() {
			return relValue;
		}

		public void setRelValue(double relValue) {
			this.relValue = relValue;
		}

		public PokestopStateValue getState() {
			return state;
		}

		public void setState(PokestopStateValue state) {
			this.state = state;
		}

		public static enum PokestopStateValue {
			AVAILABLE(0),
			USED(1),
			AWAY(2);
			
			private int value;
			
			PokestopStateValue(int value) {
				this.setValue(value);
			}

			public int getValue() {
				return value;
			}

			public void setValue(int value) {
				this.value = value;
			}
		}
	}

}
