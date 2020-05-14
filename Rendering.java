package renderingengine;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * <p>
 * {@code Rendering} paints to the screen by traversing {@code LayoutTree}
 * </p>
 */
public class Rendering {
	// +--------+--------------------------------------------------------------------------
	// | Fields |
	// +--------+
	ArrayList<DisplayCommand> displayList;
	String URL;
	int height;
	int width;

	// +--------------+----------------------------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code Rendering} using the specified {@code LayoutTree}and {@code Rect}
	 * {@code this} can generate a graphic of the specified {@code LayoutTree} with the
	 * width of the graphic being {@code bounds.width}.
	 * @param URL is a String representing web-page URL
	 * @param layout is a {@code LayoutTree}.
	 * @param bounds is a {@code Rect}.
	 */
	public Rendering(String URL, LayoutTree layout, Rect bounds) {
		this.URL = URL;
		displayList = new ArrayList<DisplayCommand>();
		height = (int) bounds.height;
		width = (int) bounds.width;
		renderLayoutBox(layout.root);
		paint();
	} // Rendering(LayoutTree, Rect)

	// +---------+--------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * Paint the {@code DisplayCommand}s collected in displayList to screen
	 */
	public void paint() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics output = image.createGraphics();
		output.setColor(new java.awt.Color(255, 255, 255)); // set initial color to white
		output.fillRect(0, 0, width, height);

		for (DisplayCommand item0 : displayList) {

			if (item0 instanceof SolidColor) {
				SolidColor item = (SolidColor) item0;
				Color c = item.color;
				output.setColor(new java.awt.Color(c.r, c.g, c.b));
				output.fillRect((int) item.rect.x, (int) item.rect.y, (int) item.rect.width, (int) item.rect.height);
			}

			else if (item0 instanceof DisplayImage) {
				DisplayImage item = (DisplayImage) item0;
				output.drawImage(item.image, (int) item.rect.x, (int) item.rect.y, null);
			} else {
				DisplayText item = (DisplayText) item0;
				output.setFont(item.font);
				Color c = item.color;
				output.setColor(new java.awt.Color(c.r, c.g, c.b));
				output.drawString(item.text, (int) item.rect.x, (int) item.rect.y);
			}

		}
		try {
			ImageIO.write(image, "jpg", new File("WebPage.jpg")); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Render {@code LayoutBox} and its children
	 * @param box is a {@code LayoutBox}
	 */
	public void renderLayoutBox(LayoutBox box) {
		renderContent(box);
		renderBorders(box);

		for (LayoutBox child : box.children) {
			renderLayoutBox(child);
		}
	}

	/**
	 * Render content of {@code LayoutBox} including background-color, image, and text
	 * @param box is a {@code LayoutBox}
	 */
	public void renderContent(LayoutBox box) {
		Color color = getColor(box, "background-color");
		if (color.exists) {
			displayList.add(new SolidColor(color, box.dim.borderBox()));
		}

		else if (box instanceof InlineNode) {
			InlineNode box1 = (InlineNode) box;

			if (box1.stynode.tagName.compareTo("img") == 0) {
				String src = box1.stynode.attributes.get("src");
				src =  URL + src.replaceAll("\"", ""); 
				double widthD = box1.dim.content.width;
				double heightD = box1.dim.content.height;
				Image image = null;
				URL url;
				try {
					url = new URL(src);
					image = ImageIO.read(url);
					displayList.add(
							new DisplayImage(image.getScaledInstance((int) widthD, (int) heightD, Image.SCALE_DEFAULT),
									box1.dim.content));
				} catch (MalformedURLException e) {
					System.out.println("Image URL " + src + " is malformed and has therefore been ignored.");

				} catch (IOException e) {
					System.out.println("Image at URL " + src + " is unreadable and has therefore been ignored.");
				}
			}

			else if (!box1.stynode.cont.txt.equals(""))
				displayList.add(new DisplayText(box1.stynode.cont.txt, box1.stynode.cont.font, box1.dim.content,
						getColor(box, "color")));
		}

	}

	/**
	 * Render borders of {@code LayoutBox}
	 * @param box is a {@code LayoutBox}
	 */
	public void renderBorders(LayoutBox box) {
		Color color = getColor(box, "border-color");
		if (!color.exists)
			return;

		Rect bBox = box.dim.borderBox();
		// Left border box (x,y,height,width)
		displayList.add(new SolidColor(color, new Rect(bBox.x, bBox.y, bBox.height, box.dim.border.left)));
		// Right border box
		displayList.add(new SolidColor(color,
				new Rect(bBox.x + bBox.width - box.dim.border.right, bBox.y, bBox.height, box.dim.border.right)));
		// Top border box
		displayList.add(new SolidColor(color, new Rect(bBox.x, bBox.y, box.dim.border.top, bBox.width)));
		// Bottom border box
		displayList.add(new SolidColor(color,
				new Rect(bBox.x, bBox.y + bBox.height - box.dim.border.bottom, box.dim.border.bottom, bBox.width)));

	}

	/**
	 * Get given color property from stynode field of {@code LayoutBox} box
	 * @param box is a {@code LayoutBox} 
	 * @param property is a String representing the color attribute which has to be looked up
	 * @return 
	 */
	public Color getColor(LayoutBox box, String property) {
		Value val = null;
		if (box instanceof BlockNode) 
			val = ((BlockNode)box).stynode.getValue(property);

		else if (box instanceof InlineNode) 
			val = ((InlineNode)box).stynode.getValue(property);

		Color color = val instanceof Color ? (Color) val : new Color(false);
		return color;
	}

} // class Rendering

/**
 * {@code DisplayCommand} represents objects that will be used by a
 * {@code Rendering} object to generate a graphic.
 */
class DisplayCommand {
	// +--------+--------------------------------------------------------
	// | Fields |
	// +--------+
	Rect rect;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+

	/**
	 * Constructs a {@code DisplayCommand} out of the specified rect object.
	 * 
	 * @param rect a {@code Rect}.
	 */
	public DisplayCommand(Rect rect) {
		this.rect = rect;
	} // DisplayCommand(Rect)
} // class DisplayCommand

/**
 * {@code SolidColor} represents a block of solid color that can be generated in
 * the paint() function of a {@code Rendering} object.
 */
class SolidColor extends DisplayCommand {
	// +--------+--------------------------------------------------------
	// | Fields |
	// +--------+
	Color color;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+

	/**
	 * Constructs a {@code SolidColor} out of the specified color and rect objects.
	 * 
	 * @param color a {@code Color}.
	 * @param rect  a {@code Rect}.
	 */
	public SolidColor(Color color, Rect rect) {
		super(rect);
		this.color = color;
	} // SolidColor(Color, Rect)
} // class SolidColor

/**
 * {@code DisplayImage} represents an image that can be generated in the paint()
 * function of a {@code Rendering} object.
 */
class DisplayImage extends DisplayCommand {

	// +--------+--------------------------------------------------------
	// | Fields |
	// +--------+
	Image image;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+

	/**
	 * Constructs a {@code DisplayImage} out of the specified image and rect
	 * objects.
	 * 
	 * @param image
	 * @param rect
	 */
	public DisplayImage(Image image, Rect rect) {
		super(rect);
		this.image = image;
	} // Display(Image, Rect)
} // class DisplayImage

/**
 * {@code DisplayText} represents text that can be generated in the paint()
 * function of a {@code Rendering} object.
 */
class DisplayText extends DisplayCommand {

	// +--------+--------------------------------------------------------
	// | Fields |
	// +--------+
	String text;
	Font font;
	Color color;

	// +-------------+----------------------------------------------------
	// | Constructor |
	// +-------------+

	/**
	 * Constructs a {@code DisplayText} out of the specified string, font, rect, and
	 * color objects.
	 *
	 * @param text  a string that represents the text to be displayed.
	 * @param font  a {@code Font} that represents the font in which text will be
	 *              displayed.
	 * @param rect  a {@code Rect}.
	 * @param color a {@code Color} that represents the color in which text will be
	 *              displayed.
	 */
	public DisplayText(String text, Font font, Rect rect, Color color) {
		super(rect);
		this.text = text;
		this.font = font;
		this.color = (color.exists) ? color : new Color(0, 0, 0, 255); // black for text

	} // DisplayText(String, Font, Rect, Color)
} // class DisplayText
