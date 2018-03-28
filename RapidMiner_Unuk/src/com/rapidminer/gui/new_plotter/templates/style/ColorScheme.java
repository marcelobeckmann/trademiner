/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.new_plotter.templates.style;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.gui.new_plotter.utility.ListUtility;

/**
 * Contains a color scheme.
 * 
 * @author Marco Boeck, Nils Woehler
 * 
 */
public class ColorScheme {

	/**
	 * Describes a color in RGB scheme and contains an alpha value for the color.
	 */
	public static class ColorRGB {

		/** r portion of the RGB color */
		private int r;

		/** g portion of the RGB color */
		private int g;

		/** b portion of the RGB color */
		private int b;

		/** alpha portion of the RGB color */
		private int alpha;

		/**
		 * Creates a new {@link ColorRGB} object with the specified r g b components and an alpha
		 * value of 255.
		 * 
		 * @param r
		 * @param g
		 * @param b
		 */
		public ColorRGB(int r, int g, int b) {
			this(r, g, b, 255);
		}

		/**
		 * Creates a new {@link ColorRGB} object with the specified r g b and alpha components.
		 * 
		 * @param r
		 * @param g
		 * @param b
		 * @param alpha
		 */
		public ColorRGB(int r, int g, int b, int alpha) {
			// check for illegal values
			if (r > 255 || r < 0) {
				throw new IllegalArgumentException("r must be between 0 and 255, but was '" + r + "'!");
			}
			if (g > 255 || g < 0) {
				throw new IllegalArgumentException("g must be between 0 and 255, but was '" + g + "'!");
			}
			if (b > 255 || b < 0) {
				throw new IllegalArgumentException("b must be between 0 and 255, but was '" + b + "'!");
			}
			if (alpha > 255 || alpha < 0) {
				throw new IllegalArgumentException("alpha must be between 0 and 255, but was '" + alpha + "'!");
			}

			this.r = r;
			this.g = g;
			this.b = b;
			this.alpha = alpha;
		}

		/**
		 * Returns the R component of this color.
		 * 
		 * @return
		 */
		public int getR() {
			return r;
		}

		/**
		 * Returns the G component of this color.
		 * 
		 * @return
		 */
		public int getG() {
			return g;
		}

		/**
		 * Returns the B component of this color.
		 * 
		 * @return
		 */
		public int getB() {
			return b;
		}

		/**
		 * Returns the Alpha component of this color.
		 * 
		 * @return
		 */
		public int getAlpha() {
			return alpha;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof ColorRGB)) {
				return false;
			}
			
			ColorRGB givenColorRGB = (ColorRGB)obj;
			
			if (givenColorRGB.getR() != this.getR()) {
				return false;
			}
			if (givenColorRGB.getG() != this.getG()) {
				return false;
			}
			if (givenColorRGB.getB() != this.getB()) {
				return false;
			}
			if (givenColorRGB.getAlpha() != this.getAlpha()) {
				return false;
			}
			
			return true;
		}
		
		/**
		 * Converts a {@link ColorRGB} input to a {@link Color} object.
		 * 
		 * @param colorRGB
		 * @return
		 */
		public static Color convertToColor(ColorRGB colorRGB) {
			return new Color(colorRGB.getR(), colorRGB.getG(), colorRGB.getB());
		}

		/**
		 * Converts a {@link ColorRGB} input to a {@link Color} object including the alpha value.
		 * 
		 * @param colorRGB
		 * @return
		 */
		public static Color convertToColorWithAlpha(ColorRGB colorRGB) {
			return new Color(colorRGB.getR(), colorRGB.getG(), colorRGB.getB(), colorRGB.getAlpha());
		}

		/**
		 * Converts a {@link Color} input to a {@link ColorRGB} object.
		 * 
		 * @param colorRGB
		 * @return
		 */
		public static ColorRGB convertColorToColorRGB(Color color) {
			return new ColorRGB(color.getRed(), color.getGreen(), color.getBlue());
		}

		/**
		 * Converts a {@link Color} input to a {@link ColorRGB} object including the alpha value.
		 * 
		 * @param colorRGB
		 * @return
		 */
		public static ColorRGB convertColorWithAlphaToColorRGB(Color color) {
			return new ColorRGB(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		}
		
		@Override
		public ColorRGB clone() {
			return new ColorRGB(r, g, b, alpha);
		}
	}

	/** the name of the {@link ColorScheme} */
	private String name;

	/** the colors which this scheme contains */
	private List<ColorRGB> listOfColors;

	private ColorRGB gradientStartColor;
	private ColorRGB gradientEndColor;

	/**
	 * Creates a new {@link ColorScheme}.
	 * 
	 * @param name
	 *            the name of the color scheme
	 * @param listOfColors
	 *            a list with the colors the scheme should contain
	 */
	public ColorScheme(String name, List<ColorRGB> listOfColors) {

		if (name == null) {
			throw new IllegalArgumentException("name must not be null!");
		}
		if (listOfColors == null) {
			throw new IllegalArgumentException("listOfColors must not be null!");
		}
		if (listOfColors.size() < 1) {
			throw new IllegalArgumentException("listOfColors must not be empty!");
		}

		this.name = name;
		this.listOfColors = listOfColors;
		this.gradientStartColor =  listOfColors.get(0);
		this.gradientEndColor = listOfColors.get(listOfColors.size() - 1);

	}

	public ColorScheme(String name, List<ColorRGB> listOfColors, ColorRGB gradientStart, ColorRGB gradientEnd) {
		this(name, listOfColors);
		this.gradientStartColor = gradientStart;
		this.gradientEndColor = gradientEnd;
	}

	/**
	 * Returns the name of this {@link ColorScheme}.
	 * 
	 * @return
	 */
	public String getName() {
		return toString();
	}

	/**
	 * @return the gradientStartColor
	 */
	public ColorRGB getGradientStartColor() {
		return gradientStartColor;
	}

	/**
	 * @return the gradientEndColor
	 */
	public ColorRGB getGradientEndColor() {
		return gradientEndColor;
	}

	/**
	 * @param gradientStartColor
	 *            the gradientStartColor to set
	 */
	public void setGradientStartColor(ColorRGB gradientStartColor) {
		if(!this.gradientStartColor.equals(gradientStartColor)) {
			this.gradientStartColor = gradientStartColor;
		}
	}

	/**
	 * @param gradientEndColor
	 *            the gradientEndColor to set
	 */
	public void setGradientEndColor(ColorRGB gradientEndColor) {
		if(!this.gradientEndColor.equals(gradientEndColor)) {
			this.gradientEndColor = gradientEndColor;
		}
	}

	/**
	 * Sets the name of this {@link ColorScheme}.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the list of {@link ColorRGB} for this {@link ColorScheme}.
	 * 
	 * @param listOfColors
	 */
	public void setColors(List<ColorRGB> listOfColors) {
		if (listOfColors == null) {
			throw new IllegalArgumentException("listOfColors must not be null!");
		}

		this.listOfColors = listOfColors;
	}
	
	/**
	 * Adds a color to the color scheme. If the color already exists, nothing will be done.
	 * If the color already exists will be checked via equals().
	 */
	public void addColor(ColorRGB color) {
		if(listOfColors.contains(color)) {
			return;
		}
		this.listOfColors.add(color);
	}
	
	/**
	 * Adds a color to the color scheme at specified index. Shifts the element currently at that position (if any) and 
	 * any subsequent elements to the right (adds one to their indices). If the color is already present in current color scheme
	 * it will be removed from its old position and add to the new index.
	 */
	public void addColor(int index, ColorRGB color) {
		int oldIdx = listOfColors.indexOf(color);
		if(oldIdx != -1) {
			ListUtility.changeIndex(listOfColors, color, index);
		} else {
			this.listOfColors.add(index, color);
		}
	}
	
	/**
	 * Removes the specified color from the {@link ColorScheme}.
	 */
	public void removeColor(ColorRGB color) {
		this.listOfColors.remove(color);
	}
	
	/**
	 *  Replaces old color with new color if old color is already in color scheme.
	 */
	public void setColor(ColorRGB oldColor, ColorRGB newColor) {
		int index = listOfColors.indexOf(oldColor);
		if(index != -1) {
			this.listOfColors.set(index, newColor);
		}
	}

	/**
	 * Returns a list with all {@link ColorRGB} objects this {@link ColorScheme} consists of.
	 */
	public List<ColorRGB> getColors() {
		return listOfColors;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public ColorScheme clone() {
		 List<ColorRGB> clonedList = new LinkedList<ColorScheme.ColorRGB>();
		 for(ColorRGB color : listOfColors) {
			 clonedList.add((ColorRGB) color.clone());
		 }
		return new ColorScheme(name, clonedList, (ColorRGB) gradientStartColor.clone(), (ColorRGB) gradientEndColor.clone());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ColorScheme)) {
			return false;
		}
		ColorScheme givenColorScheme = (ColorScheme)obj;
		
		if(!name.equals(givenColorScheme.getName())) {
			return false;
		}
		
		if(!gradientStartColor.equals(givenColorScheme.getGradientStartColor())) {
			return false;
		}
		
		if(!gradientEndColor.equals(givenColorScheme.getGradientEndColor())) {
			return false;
		}
		
		if (givenColorScheme.getColors().size() != this.getColors().size()) {
			return false;
		}
		
		for (int i=0; i<givenColorScheme.getColors().size(); i++) {
			ColorRGB givenColorRGB = givenColorScheme.getColors().get(i);
			ColorRGB thisColorRGB = this.getColors().get(i);
			if (!givenColorRGB.equals(thisColorRGB)) {
				return false;
			}
		}
		
		return true;
	}
}
