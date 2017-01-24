package com.jwetherell.datastructure;

import java.util.Arrays;


/**
 * A Patricia trie is a space-optimized trie data structure where each
 * non-terminating (black) node with only one child is merged with its child.
 * The result is that every internal non-terminating (black) node has at least
 * two children. Each terminating node (white) represents the end of a string.
 * 
 * http://en.wikipedia.org/wiki/Radix_tree
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class PatriciaTrie<C extends CharSequence> {

    private int size = 0;

    protected Node root = null;
    protected static final boolean BLACK = false;
    protected static final boolean WHITE = true;


    public PatriciaTrie() { }

    public boolean add(C string) {
        Node node = this.addNode(string);
        return (node != null);
    }

    protected Node addNode(C string) {
        if (root==null) root = createNewNode(null, null, BLACK);

        int indexIntoParent = -1;
        int indexIntoString = -1;
        Node node = root;
        for (int i = 0; i <= string.length();) {
            indexIntoString = i;
            indexIntoParent++;
            if (i == string.length()) break;

            char c = string.charAt(i);
            if (node.partOfThis(c, indexIntoParent)) {
                // Node has a char which is equal to char c at that index
                i++;
                continue;
            } else if (node.string != null && indexIntoParent < node.string.length) {
                // string is equal to part of this Node's string
                break;
            }

            Node child = node.getChildBeginningWithChar(c);
            if (child != null) {
                // Found a child node starting with char c
                indexIntoParent = 0;
                node = child;
                i++;
            } else {
                // Node doesn't have a child starting with char c
                break;
            }
        }

        Node addedNode = null;
        if (node.string != null && indexIntoParent < node.string.length) {
            char[] parentString = Arrays.copyOfRange(node.string, 0, indexIntoParent);
            char[] refactorString = Arrays.copyOfRange(node.string, indexIntoParent, node.string.length);

            Node parent = node.parent;
            if (indexIntoString < string.length()) {
                // Creating a new parent by splitting a previous node and adding
                // a new node

                // Create new parent
                if (parent!=null) parent.removeChild(node);
                Node newParent = createNewNode(parent, parentString, BLACK);
                if (parent!=null) parent.addChild(newParent);

                // Convert the previous node into a child of the new parent
                Node newNode1 = node;
                newNode1.parent = newParent;
                newNode1.string = refactorString;
                newParent.addChild(newNode1);

                // Create a new node from the rest of the string
                CharSequence newString = string.subSequence(indexIntoString, string.length());
                Node newNode2 = createNewNode(newParent, newString.toString().toCharArray(), WHITE);
                newParent.addChild(newNode2);

                // New node which was added
                addedNode = newNode2;
            } else {
                // Creating a new parent by splitting a previous node and
                // converting the previous node
                if (parent!=null) parent.removeChild(node);
                Node newParent = createNewNode(parent, parentString, WHITE);
                if (parent!=null) parent.addChild(newParent);

                // Parent node was created
                addedNode = newParent;

                // Convert the previous node into a child of the new parent
                Node newNode1 = node;
                newNode1.parent = newParent;
                newNode1.string = refactorString;
                newParent.addChild(newNode1);
            }
        } else if (node.string != null && string.length() == indexIntoString) {
            // Found a node who exactly matches a previous node

            // Already exists as a white node (leaf node)
            if (node.type == WHITE) return null;

            // Was black (branching), now white (leaf)
            node.type = WHITE;
            addedNode = node;
        } else if (node.string != null) {
            // Adding a child
            CharSequence newString = string.subSequence(indexIntoString, string.length());
            Node newNode = createNewNode(node, newString.toString().toCharArray(), WHITE);
            node.addChild(newNode);
            addedNode = newNode;
        } else {
            // Add to root node
            Node newNode = createNewNode(node, string.toString().toCharArray(), WHITE);
            node.addChild(newNode);
            addedNode = newNode;
        }

        size++;
        
        return addedNode;
    }

    protected Node createNewNode(Node parent, char[] string, boolean type) {
        return (new Node(parent, string, type));
    }

    public boolean remove(C string) {
        Node node = getNode(string);
        if (node == null) return false;

        // No longer a white node (leaf)
        node.type = BLACK;

        Node parent = node.parent;
        if (node.getChildrenSize() == 0) {
            // Remove the node if it has no children
            if (parent != null) parent.removeChild(node);
        } else if (node.getChildrenSize() == 1) {
            // Merge the node with it's child and add to node's parent

            Node child = node.getChild(0);
            StringBuilder builder = new StringBuilder();
            builder.append(node.string);
            builder.append(child.string);
            child.string = builder.toString().toCharArray();
            child.parent = parent;

            if (parent != null) {
                parent.removeChild(node);
                parent.addChild(child);
            }
        }

        // Walk up the tree and see if we can compact it
        while (parent != null && parent.type == BLACK && parent.getChildrenSize() == 1) {
            Node child = parent.getChild(0);
            // Merge with parent
            StringBuilder builder = new StringBuilder();
            if (parent.string != null) builder.append(parent.string);
            builder.append(child.string);
            child.string = builder.toString().toCharArray();
            if (parent.parent != null) {
                child.parent = parent.parent;
                parent.parent.removeChild(parent);
                parent.parent.addChild(child);
            }
            parent = parent.parent;
        }

        size--;
        
        return true;
    }

    public boolean contains(C string) {
        Node node = getNode(string);
        return (node != null && node.type == WHITE);
    }

    protected Node getNode(C string) {
        Node node = root;
        int indexIntoParent = -1;
        for (int i = 0; i < string.length();) {
            indexIntoParent++;

            char c = string.charAt(i);
            if (node.partOfThis(c, indexIntoParent)) {
                // Node has a char which is equal to char c at that index
                i++;
                continue;
            } else if (node.string != null && indexIntoParent < node.string.length) {
                // string is equal to part of this Node's string
                return null;
            }

            Node child = node.getChildBeginningWithChar(c);
            if (child != null) {
                // Found a child node starting with char c
                indexIntoParent = 0;
                node = child;
                i++;
            } else {
                // Node doesn't have a child starting with char c
                return null;
            }
        }

        if (node.string != null && indexIntoParent == (node.string.length - 1)) {
            // Get the partial string to compare against the node's string
            int length = node.string.length;
            CharSequence sub = string.subSequence(string.length()-length, string.length());
            for (int i=0; i<length; i++) {
                if (node.string[i] != sub.charAt(i)) return null;
            }
            return node;
        }
        return null;
    }

    public int size() {
        return size;
    }
    
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }


    protected static class Node implements Comparable<Node> {

        private static final int GROW_IN_CHUNKS = 10;
        private Node[] children = new Node[2];
        private int childrenSize = 0;

        protected Node parent = null;
        protected boolean type = BLACK;
        protected char[] string = null;


        protected Node(Node parent) {
            this.parent = parent;
        }

        protected Node(Node parent, char[] string) {
            this(parent);
            this.string = string;
        }

        protected Node(Node parent, char[] string, boolean type) {
            this(parent, string);
            this.type = type;
        }
        
        protected void addChild(Node node) {
            if (childrenSize==children.length) {
                children = Arrays.copyOf(children, children.length+GROW_IN_CHUNKS);
            }
            children[childrenSize++] = node;
        }
        private boolean removeChild(Node child) {
            boolean found = false;
            if (childrenSize==0) return found;
            for (int i=0; i<childrenSize; i++) {
                if (children[i].equals(child)) {
                    found = true;
                } else if (found) {
                    //shift the rest of the keys down
                    children[i-1] = children[i];
                }
            }
            if (found) {
                childrenSize--;
                children[childrenSize] = null;
            }
            return found;
        }
        protected boolean removeChild(int index) {
            if (index>=childrenSize) return false;
            children[index] = null;
            for (int i=index+1; i<childrenSize; i++) {
                //shift the rest of the keys down
                children[i-1] = children[i];
            }
            childrenSize--;
            children[childrenSize] = null;
            return true;
        }
        protected Node getChild(int index) {
            if (index>=childrenSize) return null;
            return children[index];
        }
        protected int getChildrenSize() {
            return childrenSize;
        }
        protected boolean partOfThis(char c, int idx) {
            // Search myself
            if (string != null && idx < string.length && string[idx] == c) return true;
            return false;
        }
        protected Node getChildBeginningWithChar(char c) {
            // Search children
            Node node = null;
            for (int i=0; i<this.childrenSize; i++) {
                Node child = this.children[i];
                if (child.string[0] == c) return child;
            }
            return node;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("string = ").append(string).append("\n");
            builder.append("type = ").append(type).append("\n");
            return builder.toString();
        }

        @Override
        public int compareTo(Node node) {
            if (node == null) return -1;

            int length = string.length;
            if (node.string.length < length) length = node.string.length;
            for (int i = 0; i < length; i++) {
                Character a = string[i];
                Character b = node.string[i];
                int c = a.compareTo(b);
                if (c != 0) return c;
            }

            if (this.type==BLACK && node.type==WHITE) return -1;
            else if (node.type==BLACK && this.type==WHITE) return 1;

            if (this.getChildrenSize() < node.getChildrenSize()) return -1;
            else if (this.getChildrenSize() > node.getChildrenSize()) return 1;

            return 0;
        }
    }

    protected static class TreePrinter<C extends CharSequence> {

        protected static <C extends CharSequence> String getString(PatriciaTrie<C> tree) {
            return getString(tree.root, "", null, true);
        }

        protected static <C extends CharSequence> String getString(Node node, String prefix, String previousString, boolean isTail) {
            StringBuilder builder = new StringBuilder();
            String string = null;
            if (node.string!=null) {
                String temp = String.valueOf(node.string);
                if (previousString!=null) string = previousString + temp;
                else string = temp;
            }
            builder.append(prefix + (isTail ? "└── " : "├── ") + 
                ((node.string != null) ? 
                    "(" + String.valueOf(node.string) + ") " + "[" + ((node.type==WHITE)?"white":"black") + "] " + string
                : 
                    "[" + node.type + "]") + 
            "\n");
            if (node.children != null) {
                for (int i = 0; i < node.getChildrenSize() - 1; i++) {
                    builder.append(getString(node.getChild(i), prefix + (isTail ? "    " : "│   "), string, false));
                }
                if (node.getChildrenSize() >= 1) {
                    builder.append(getString(node.getChild(node.getChildrenSize() - 1), prefix + (isTail ? "    " : "│   "), string, true));
                }
            }
            return builder.toString();
        }
    }
}
