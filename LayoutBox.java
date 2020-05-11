package renderingengine;


import java.awt.FontMetrics;
import java.util.ArrayList;


public class LayoutBox
{
	Dimensions dim;
	ArrayList<LayoutBox> children;
	int currInlinePosX;
	int currInlinePosY;
	//boolean startedInline;

	public LayoutBox()
	{
		dim = new Dimensions();
		children = new ArrayList<LayoutBox>();
		currInlinePosX = (int) dim.content.width;
		currInlinePosY = 0;
		//startedInline = false;
	}

	public LayoutBox getInlineContainer()
	{
		//Return current object if it's Inline/Anonymous
		if(this instanceof InlineNode || this instanceof AnonymousBlock)
			return this;

		//Return existing AnonymousBlock if just created otherwise new one
		if(children.size() == 0)
			children.add(new AnonymousBlock());

		if(!(children.get(children.size()-1) instanceof AnonymousBlock))
			children.add(new AnonymousBlock());

		return children.get(children.size()-1);
	}

	public void layout(Dimensions container, int currInlinePosX, int currInlinePosY)
	{
		
		if(this instanceof BlockNode)
		{
			BlockNode curr = (BlockNode) this;
			curr.layoutBlock(container);
			//currInlineWidth = curr.dim.content.x;
		}

		else if(this instanceof InlineNode)
		{
			this.currInlinePosX = currInlinePosX;
			this.currInlinePosY = currInlinePosY;
			InlineNode curr = (InlineNode) this;
			curr.layoutBlock(container, currInlinePosX, currInlinePosY);
		}

		else
		{
			this.currInlinePosX = currInlinePosX;
			this.currInlinePosY = currInlinePosY;
			AnonymousBlock curr = (AnonymousBlock) this;
			curr.layoutBlock(container,currInlinePosX, currInlinePosY);
		}

	}
	public void layoutBlockChildren()
	{
		double width = dim.content.width;
	 
		ArrayList<LayoutBox> add = new ArrayList<LayoutBox>();
		for(LayoutBox child: children)
		{
			if(child instanceof InlineNode)
			{
				InlineNode c = (InlineNode) child;
				ArrayList<InlineNode> splits = c.layoutBlock(dim, currInlinePosX, currInlinePosY);
				if(splits != null)
				{
					add.addAll(splits);
				}
				
				dim.content.height += c.updateY - currInlinePosY;
				currInlinePosX = c.updateX;
				currInlinePosY = c.updateY;
			}
			
			else if(child instanceof AnonymousBlock)
			{
				AnonymousBlock c = (AnonymousBlock) child;
				c.layoutBlock(dim,currInlinePosX,currInlinePosY);
				dim.content.height += c.currInlinePosY - currInlinePosY;
				currInlinePosX = c.currInlinePosX;
				currInlinePosY = c.currInlinePosY;
				//dim.content.height += child.dim.marginBox().height;
			}

			else
			{
				child.layout(dim,currInlinePosX, currInlinePosY);
				dim.content.height += child.dim.marginBox().height;
				currInlinePosX  =  (int) dim.content.width;
				currInlinePosY  += child.dim.marginBox().height;
			}

			double height = dim.content.height;
			int x = 1;
		}
		if(add.size() != 0)
			children.addAll(add);
	}
}

class BlockNode extends LayoutBox
{
	StyledNode stynode;

	public BlockNode()
	{
		super();
		stynode = new StyledNode( );
	}

	public BlockNode(StyledNode stynode)
	{
		super();
		this.stynode = stynode;
	}

	public void layoutBlock(Dimensions container)
	{
		calculateWidth(container);
		calculateBlockPosition(container);
		this.currInlinePosX = (int) dim.content.width;
		this.currInlinePosY = 0;
		layoutBlockChildren();
		calculateBlockHeight();
	}

	public void calculateWidth(Dimensions container)
	{

		Value width = stynode.getValue("width");
		width = (width instanceof Length)? width : (new Keyword("auto"));

		Length zero = new Length(0,"px");

		Value [] surrounding  = {width, stynode.lookup("margin-left","margin",zero),stynode.lookup("margin-right","margin",zero),
				stynode.lookup("border-left-width","border-width",zero),stynode.lookup("border-right-width","border-width",zero),
				stynode.lookup("padding-left","padding",zero),stynode.lookup("padding-right","padding",zero)};
		double total = 0;
		for(Value val: surrounding)
		{
			total += val.getLen();
		}

		//		if(width != zero && total > container.content.width)
		//		{
		//			for(int i = 1; i < 3; i++) //if margins are "auto" treat them as zero
		//			{
		//				if(surrounding[i].isAuto())
		//					surrounding[i] = zero;
		//			}
		//		}

		double underflow = container.content.width - total;
		AutoBooleanTriple curr = new AutoBooleanTriple(new Value[]{surrounding[0],surrounding[1],surrounding[2]});

		if(curr.matches(new boolean[] {false,false,false}))
		{
			surrounding[2] = new Length(surrounding[2].getLen() + underflow, "px"); //Right margin gets adjusted if none is auto
		}

		else if(curr.matches(new boolean[] {false,false,true}))
		{
			surrounding[2] = new Length(underflow, "px");
		}

		else if(curr.matches(new boolean[] {false,true,false}))
		{
			surrounding[1] = new Length(underflow, "px");
		}

		else if(curr.matches(new boolean[] {false,true,true}))
		{
			surrounding[1] = new Length(underflow/2, "px");
			surrounding[2] = new Length(underflow/2, "px");
		}

		else //if width is auto
		{
			if(underflow >= 0)
				surrounding[0] = new Length(underflow,"px");
			else
				surrounding[2] = new Length(surrounding[2].getLen() + underflow, "px");
		}

		dim.content.width = surrounding[0].getLen();

		dim.margin.left = surrounding[1].getLen();
		dim.margin.right = surrounding[2].getLen();

		dim.border.left = surrounding[3].getLen();
		dim.border.right = surrounding[4].getLen();

		dim.padding.left = surrounding[5].getLen();
		dim.padding.right = surrounding[6].getLen();
	}

	public void calculateBlockPosition(Dimensions container)
	{
		Length zero = new Length(0,"px");
		dim.margin.top = stynode.lookup("margin-top", "margin", zero).getLen();
		dim.margin.bottom = stynode.lookup("margin-bottom", "margin", zero).getLen();

		dim.border.top = stynode.lookup("border-top-width", "border-width", zero).getLen();		
		dim.border.bottom = stynode.lookup("border-bottom-width", "border-width", zero).getLen();

		dim.padding.top = stynode.lookup("padding-top", "padding", zero).getLen();
		dim.padding.bottom = stynode.lookup("padding-bottom", "padding", zero).getLen();

		dim.content.x = container.content.x + dim.margin.left + dim.padding.left + dim.border.left;
		dim.content.y = container.content.y + container.content.height + dim.margin.top + dim.padding.bottom + dim.border.bottom;
	}

	public void calculateBlockHeight()
	{
		Value height = stynode.getValue("height");
		if(dim.content.height < height.getLen())
			dim.content.height = height.getLen();
	}

}

class AutoBooleanTriple
{
	boolean [] bool;
	AutoBooleanTriple(Value[] val)
	{
		bool = new boolean[3];
		for(int i = 0; i < 3; i++)
			bool[i] = val[i].isAuto();
	}

	boolean matches(boolean [] bool2)
	{
		for(int i = 0; i < 3; i++)
		{
			if(bool[i] != bool2[i])
				return false;
		}
		return true;
	}
}

class InlineNode extends LayoutBox
{
	StyledNode stynode;
	int updateX;
	int updateY;

	public InlineNode()
	{
		super();
		stynode = new StyledNode();
		updateX = 0;
		updateY = 0;
	}

	public InlineNode(StyledNode stynode)
	{
		super();
		this.stynode = stynode;
	}

	public ArrayList<InlineNode> layoutBlock(Dimensions container, int currInlinePosX, int currInlinePosY)
	{
		//		//
		//		//dim.content.y = container.content.y;
		//		dim.content.x = container.content.x;
		//		dim.content.y = container.content.y;
		//		dim.content.width = container.content.width;

		
		if(stynode.cont.txt != "")
		{
			String txt = stynode.cont.txt;
			java.awt.Canvas c = new java.awt.Canvas();
			FontMetrics metrics = c.getFontMetrics(stynode.cont.font);
			dim.content.height =  metrics.getHeight();
			if(currInlinePosX == container.content.width)
			{
				currInlinePosX = 0;
				currInlinePosY += dim.content.height;
			}
			dim.content.x = container.content.x + currInlinePosX;
			dim.content.y = container.content.y + currInlinePosY;
			
			int spaceWidth = metrics.charWidth(' ');
//			int tempX = currInlinePosX;
			if(txt.contains("BREAKLIEN"))
			{
				int index = txt.indexOf("BREAKLIEN");
				int w = metrics.stringWidth(txt.substring(0,index));
				int spaces = (int) (container.content.width- currInlinePosX-w)/spaceWidth;
				txt = txt.substring(0,index) + makeSpaces(spaces)+ "<DELETE>" + txt.substring(index+9);
				stynode.cont.txt = txt;
				//txt = txt.substring(0,index) + String.format("%1$" + (container.content.width- currInlinePosX+w)/space + "s",txt.substring(index+9));
			}
			int width = metrics.stringWidth(txt);
			double overflow = currInlinePosX + width - container.content.width;
			if(overflow > 0)
			{
				ArrayList<InlineNode> splits = new ArrayList<InlineNode>();
				InlineNode prev = this;
				while(overflow > 0)
				{
					int len = txt.length();
					int cutoff = (int) (len*(container.content.width - currInlinePosX)/width);
					prev.stynode.cont.txt = txt.substring(0,cutoff);
					InlineNode split = new InlineNode();
					split.stynode = prev.stynode.copy();
					split.stynode.cont.txt = txt.substring(cutoff).replaceAll("\\s*<DELETE>", "");
					split.dim.content.x = container.content.x + 0;
					split.dim.content.y = container.content.y + currInlinePosY + prev.dim.content.height;
					split.dim.content.height = prev.dim.content.height;
					prev.dim.content.width = metrics.stringWidth(prev.stynode.cont.txt);
					
					currInlinePosX = 0;
					
					txt = split.stynode.cont.txt;
					if(txt.contains("BREAKLIEN"))
					{
						int index = txt.indexOf("BREAKLIEN");
						int w = metrics.stringWidth(txt.substring(0,index));
						int spaces = (int) (container.content.width- currInlinePosX-w)/spaceWidth;
						txt = txt.substring(0,index) + makeSpaces(spaces) + "<DELETE>" + txt.substring(index+9);
						split.stynode.cont.txt = txt;
						//txt = txt.substring(0,index) + String.format("%1$" + (container.content.width- currInlinePosX+w)/space + "s",txt.substring(index+9));
					}
					split.dim.content.width = metrics.stringWidth(split.stynode.cont.txt);
					updateX = (int) split.dim.content.width;
					updateY = (int) (currInlinePosY + prev.dim.content.height);
					currInlinePosY = updateY;
					width = (int) split.dim.content.width;
					overflow = currInlinePosX + width - container.content.width;
					splits.add(split);
					prev = split;
					
				}
				return splits;
			}
			else
			{
				dim.content.width = width;
				updateX = currInlinePosX + (int) dim.content.width;
				updateY = currInlinePosY;
			}
		}

		else
		{
			
			this.currInlinePosX = currInlinePosX;
			this.currInlinePosY = currInlinePosY;
			dim.content.x =  container.content.x;
			dim.content.y =  container.content.y;
			dim.content.width = container.content.width;//?
			layoutBlockChildren();
			updateX = this.currInlinePosX;
			updateY = this.currInlinePosY;
		}
		return null;
	}
	
	public String makeSpaces(int spaces)
	{
		String str = "";
		for(int i = 0; i<spaces; i++)
		{
			str += ' ';
		}
		return str;
	}

}

class AnonymousBlock extends LayoutBox
{
	int updateX;
	int updateY;
	
	public AnonymousBlock()
	{
		super();
		updateX = 0;
		updateY = 0;
	}
	public void layoutBlock(Dimensions container, int currInlinePosX, int currInlinePosY)
	{
		//dim.content = container.content;
//		dim.content.x = container.content.x;
//		dim.content.y = container.content.y;
//		dim.content.width = container.content.width;
//		layoutBlockChildren();
		
		this.currInlinePosX = currInlinePosX;
		this.currInlinePosY = currInlinePosY;
		dim.content.x =  container.content.x;
		dim.content.y =  container.content.y;
		dim.content.width = container.content.width;//?
		layoutBlockChildren();
		//updateX = this.currInlinePosX;
		//updateY = this.currInlinePosY;
	}
}


class Dimensions
{
	Rect content;
	EdgeSizes padding;
	EdgeSizes border;
	EdgeSizes margin;

	public Dimensions()
	{
		content = new Rect(0,0,0,0);
		padding = new EdgeSizes();
		border = new EdgeSizes();
		margin = new EdgeSizes();
	}

	public Dimensions(Rect rect)
	{
		content = rect;
		padding = new EdgeSizes();
		border = new EdgeSizes();
		margin = new EdgeSizes();
	}

	public Rect paddingBox()
	{
		return content.expandedBy(padding);
	}

	public Rect borderBox()
	{
		return paddingBox().expandedBy(border);
	}

	public Rect marginBox()
	{
		return borderBox().expandedBy(margin);
	}

}

class Rect
{
	double x;
	double y;
	double height;
	double width;

	public Rect(double x, double y, double height, double width)
	{
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	public Rect expandedBy(EdgeSizes e)
	{	//not sure about y-e.top
		return new Rect(x-e.left, y-e.top, height+e.top+e.bottom, width+e.left+e.right);

	}
}

class EdgeSizes
{
	double left;
	double right;
	double bottom;
	double top;
}