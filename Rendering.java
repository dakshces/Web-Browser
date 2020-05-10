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
	int height;
	int width;

	public Rendering(LayoutTree layout, Rect bounds)
	{
		displayList = new ArrayList<DisplayCommand>();
		height = (int) bounds.height;
		width = (int) bounds.width;
		renderLayoutBox(layout.root);
		paint();
	}


	public void paint()
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics output = image.createGraphics();
		output.setColor(new java.awt.Color(255, 255, 255)); //set initial color to white
		output.fillRect(0, 0, width, height);
		
		for(DisplayCommand item0: displayList)
		{
			
			if(item0 instanceof SolidColor)
			{
				SolidColor item = (SolidColor) item0;
				Color c = item.color;
				output.setColor(new java.awt.Color(c.r, c.g, c.b));
				output.fillRect((int)item.rect.x, (int)item.rect.y, (int)item.rect.width, (int)item.rect.height);
			}

			else
			{
				DisplayText item = (DisplayText) item0;
				output.setFont(item.font);
				//output.setColor(java.awt.Color.RED);
				Color c = item.color;
				output.setColor(new java.awt.Color(c.r, c.g, c.b));
				output.drawString(item.text,(int)item.rect.x,(int)item.rect.y);
				
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
