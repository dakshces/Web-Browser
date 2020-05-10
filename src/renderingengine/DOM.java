package renderingengine;

public class DOM {
	Node root;

	public DOM() {
		root = null;
	}

	public DOM(Node root) {
		this.root = root;
	}

	public void print() {
		print(root, "");
	}

	public void print(Node n) {
		System.out.print(n);
	}

	public void print(Node n, String prefix) {

		System.out.println(prefix + n.toString());
		for (Node t : n.children) {
			print(t, prefix + "\t");

		}
	}
	
	@Override
	public String toString() {
		String str = "";

		return str;
	}

}
