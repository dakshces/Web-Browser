package renderingengine;

/**
 * <p>
 * {@code LayoutTree} composed of {@code LayoutBox} nodes represents the laid out/positioned StyledTree. 
 * </p>
 */

public class LayoutTree
{
	// +--------+----------------------------------
	// | Fields |
	// +--------+
	LayoutBox root;


	// +--------------+---------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Constructs a {@code LayoutTree} from given {@code StyleTree}
	 * The dimensions of resulting {@code LayoutTree} root is constrained by the width of bounds
	 * The height of {@code LayoutTree} root is determined by the content
	 * @param sty a {@code StyleTree} which has to be laid out
	 * @param bounds a {@code Dimensions} is the parent container of root
	 */
	public LayoutTree(StyleTree sty, Dimensions bounds)
	{
		root = buildTree(sty.root);
		bounds.content.height = 0;
		root.layout(bounds,0,0);
		bounds.content.height = root.dim.content.height;
	}

	/**
	 * Builds a {@code LayoutBox} from given {@code StyleNode}
	 * The dimensions of resulting {@code LayoutTree} root is constrained by the width of bounds
	 * The height of {@code LayoutTree} root is determined by the content
	 * @param sn a {@code StyledNode} which has to be positioned
	 * @return {@code LayoutBox}
	 */
	public LayoutBox buildTree(StyledNode sn)
	{
		String option = sn.display();
		LayoutBox lb = new LayoutBox();
		if(option.compareTo("block") == 0)
			lb = new BlockNode(sn);
		else if(option.compareTo("inline") == 0)
			lb = new InlineNode(sn);
		else
		{
			System.out.println("Root node has display option: " + option);
			System.exit(1);
		}

		for(Node child0: sn.children)
		{
			StyledNode child = (StyledNode) child0;
			option = child.display();
			if(option.compareTo("block") == 0)
				lb.children.add(buildTree(child));
			else if(option.compareTo("inline") == 0)
				//add tree rooted at child to either an existing AnonymousBlock/InlineBlock or a new AnonymousBlock
				lb.getInlineContainer().children.add(buildTree(child)); 
		}
		return lb;
	}

}


