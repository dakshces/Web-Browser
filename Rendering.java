package renderingengine;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Rendering
{
	ArrayList<DisplayCommand> displayList;
	Canvas page;

	public Rendering(LayoutTree layout, Rect bounds)
	{
		displayList = new ArrayList<DisplayCommand>();
		page = new Canvas((int)bounds.width, (int)bounds.height);
		renderLayoutBox(layout.root);
		paint();
	}

	//	public void paint2()
	//	{
	//		for(DisplayCommand item: displayList)
	//			page.paintItem(item);
	//
	//		BufferedImage image = new BufferedImage(page.width, page.height, BufferedImage.TYPE_INT_RGB);
	//
	//		for(int y = 0; y < page.height; y++)
	//		{
	//			for(int x = 0; x < page.width; x++)
	//			{
	//				Color c = page.pixels.get(y*page.width+x);
	//				image.setRGB(x, y, new java.awt.Color(c.r, c.g, c.b).getRGB());
	//			}
	//		}
	//		try 
	//		{
	//			ImageIO.write(image, "jpg", new File("WebPage.jpg"));
	//		}
	//		catch (IOException e) 
	//		{ 
	//			e.printStackTrace();
	//		}
	//	}


	public void paint()
	{
		BufferedImage image = new BufferedImage(page.width, page.height, BufferedImage.TYPE_INT_RGB);
		Graphics output = image.createGraphics();
		output.setColor(new java.awt.Color(255, 255, 255)); //set initial color to white
		output.fillRect(0, 0, page.width, page.height);
		boolean prevInline = false;
		int currContainerX = 0;
		int currContainerY = 0;
		int currContainerWidth = page.width;
		int currX = 0;
		int currY = 0;
		for(DisplayCommand item0: displayList)
		{
			
			if(item0 instanceof SolidColor)
			{
				SolidColor item = (SolidColor) item0;
				Color c = item.color;
				output.setColor(new java.awt.Color(c.r, c.g, c.b));
				currX = (int)item.rect.x;
				currContainerX = currX;
				currY = (int)item.rect.y;
				currContainerY = currY;
				currContainerWidth = (int)item.rect.width;
				output.fillRect(currX, currY, currContainerWidth, (int)item.rect.height);
				prevInline = false;
			}

			else
			{
				DisplayText item = (DisplayText) item0;
				output.setFont(item.font);
				//output.setColor(java.awt.Color.RED);
				Color c = item.color;
				output.setColor(new java.awt.Color(c.r, c.g, c.b));
				if(prevInline)
				{
					output.drawString(item.text, currX, currY);
				}
				else
				{
					currX = (int) item.rect.x;
					currY = (int) item.rect.y;
					currContainerX = currX;
					currContainerWidth = (int) item.rect.width;
					output.drawString(item.text, currX, currY);
				}
				
				currX += output.getFontMetrics().stringWidth(item.text);
				
				if(currX >= currContainerWidth || item.text.contains("\n"))
				{
					currX = currContainerX;
					currY += output.getFontMetrics().getHeight();
				}
				
				prevInline = true;
			}
			
//			if(page.height - currY < 200)
//			{
//				BufferedImage dup = new BufferedImage(image.getWidth(), 2*image.getHeight(), image.getType());
//				output = dup.getGraphics();
//				output.drawImage(image, 0, 0, null);
//				image = dup;
//			}
		}
		try 
		{
			ImageIO.write(image, "jpg", new File("WebPage.jpg"));
		}
		catch (IOException e) 
		{ 
			e.printStackTrace();
		}
	}
	
	public void renderLayoutBox(LayoutBox box)
	{
		renderBackground(box);
		renderBorders(box);

		for(LayoutBox child: box.children)
		{
			renderLayoutBox(child);
		}
	}

	public void renderBackground(LayoutBox box)
	{
		Color color = getColor(box, "background-color");
		if(color.exists)
		{
			displayList.add(new SolidColor(color,box.dim.borderBox()));
		}

		else if(box instanceof InlineNode)
		{
			InlineNode box1 = (InlineNode) box;
			if(box1.stynode.cont.txt != "")
				displayList.add(new DisplayText(box1.stynode.cont.txt, box1.stynode.cont.font, box1.dim.content,getColor(box,"color")));
		}

	}

	public void renderBorders(LayoutBox box)
	{
		Color color = getColor(box, "border-color");
		if(!color.exists)
			return;

		Rect bBox = box.dim.borderBox();
		//Left border box (x,y,height,width)
		displayList.add(new SolidColor(color, new Rect(bBox.x,bBox.y,bBox.height,box.dim.border.left)));
		//Right border box
		displayList.add(new SolidColor(color, new Rect(bBox.x+bBox.width-box.dim.border.right,bBox.y,bBox.height,box.dim.border.right)));
		//Top border box
		displayList.add(new SolidColor(color, new Rect(bBox.x,bBox.y,box.dim.border.top,bBox.width)));
		//Bottom border box
		displayList.add(new SolidColor(color, new Rect(bBox.x,bBox.y+bBox.height-box.dim.border.bottom,box.dim.border.bottom,bBox.width)));


	}

	//Returns white if no color specified
	public Color getColor(LayoutBox box, String property)
	{
		Value val = null;
		if(box instanceof BlockNode)
		{
			BlockNode box1 = (BlockNode) box;
			val =  box1.stynode.getValue(property);
		}

		else if(box instanceof InlineNode)
		{
			InlineNode box1 = (InlineNode) box;
			val = box1.stynode.getValue(property);
		}

		Color color = val instanceof Color? (Color) val: new Color(false);
		return color;
	}


}

class DisplayCommand
{}

class SolidColor extends DisplayCommand
{
	Color color;
	Rect rect;

	public SolidColor(Color color, Rect rect) 
	{
		this.color = color;
		this.rect = rect;
	}
}

class DisplayText extends DisplayCommand
{
	String text;
	Font font;
	Rect rect; 
	Color color;

	public DisplayText(String text, Font font, Rect rect, Color color) 
	{
		this.text = text;
		this.font = font;
		this.rect = rect;
		if(color.exists)
			this.color = color;
		else
			this.color = new Color(0,0,0,255); //black for text
	}
}


class Canvas
{
	ArrayList<Color> pixels;
	int width;
	int height;

	public Canvas(int width, int height)
	{
		pixels = new ArrayList<Color>();
		this.width = width;
		this.height = height;
		Color white = new Color(255,255,255,255);
		for(int i = 0; i < width*height; i++)
			pixels.add(white);
	}

	public void paintItem(DisplayCommand item0)
	{
		if(item0 instanceof SolidColor)
		{
			SolidColor item = (SolidColor) item0;
			int x0 = clamp(item.rect.x,0,width-1);
			int x1 = clamp(item.rect.x+item.rect.width,0,width-1);
			int y0 = clamp(item.rect.y,0,height-1);
			int y1 = clamp(item.rect.y+item.rect.height,0,height-1);
			for(int y = y0; y <= y1; y++)
			{
				for(int x = x0; x <= x1; x++)
				{
					pixels.add(y*width+x, item.color);
				}
			}

		}
	}

	public int clamp(double curr, double lower, double upper)
	{
		if(curr > upper)
			return (int) upper;
		if(curr < lower)
			return (int) lower;
		return (int) curr;
	}
}