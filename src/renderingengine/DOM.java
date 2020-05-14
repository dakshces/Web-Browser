package renderingengine;

/**
 * An {@code ArrayList} implementation of a Document Object Model tree.
 * 
 */
public class DOM {
	
	// +-------+------------------------------------------------------------
	// | Field |
	// +-------+
	Node root;

	// +--------------+-------------------------------------------
	// | Constructors |
	// +--------------+
	
	public DOM() {
		root = null;
	} // DOM()

	public DOM(Node root) {
		this.root = root;
	} // DOM(Node)

	/**
	 * Prints the DOM tree to console following depth-first preorder traversal
	 */
	public void print() {
		print(root, "", "\t");
	} // print()

	/**
	 * Prints to console the specified node and its children
	 * following depth-first preorder traversal.
	 * 
	 * @param n a {@code Node}
	 * @param prefix a {@code String}
	 * @param indent a {@code String}
	 */
	private void print(Node n, String prefix, String indent) {

		System.out.println(prefix + n.toString());
		for (Node t : n.children) {
			print(t, prefix + indent, indent);
		}
	} // print(Node, String, String)
	
	@Override
	public String toString() {
		String str = "";

		return str;
	} // toString()

} // class DOM
