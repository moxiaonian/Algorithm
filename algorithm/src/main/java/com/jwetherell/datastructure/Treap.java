package com.jwetherell.datastructure;

import java.util.ArrayList;
import java.util.List;


/**
 * The treap and the randomized binary search tree are two closely related forms
 * of binary search tree data structures that maintain a dynamic set of ordered
 * values and allow binary searches among the values. After any sequence of
 * insertions and deletions of values, the shape of the tree is a random
 * variable with the same probability distribution as a random binary tree; in
 * particular, with high probability its height is proportional to the logarithm
 * of the number of values, so that each search, insertion, or deletion
 * operation takes logarithmic time to perform.
 * 
 * http://en.wikipedia.org/wiki/Treap
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Treap<T extends Comparable<T>> extends BinarySearchTree<T> {

    private static int randomSeed = 100; // This should be at least twice the number of Nodes


    public Treap() { }
    
    public Treap(int randomSeed) {
        Treap.randomSeed = randomSeed;
    }
    
    @Override
    protected Node<T> addValue(T value) {
        TreapNode<T> nodeToAdd = new TreapNode<T>(null, value);
        super.add(nodeToAdd);
        heapify(nodeToAdd);
        return nodeToAdd;
    }

    private void heapify(TreapNode<T> current) {
        // Bubble up the heap, if needed
        TreapNode<T> parent = (TreapNode<T>) current.parent;
        while (parent != null && current.priority > parent.priority) {
            Node<T> grandParent = parent.parent;
            if (grandParent != null) {
                if (grandParent.greater != null && grandParent.greater.equals(parent)) {
                    // My parent is my grandparents greater branch
                    grandParent.greater = current;
                    current.parent = grandParent;
                } else if (grandParent.lesser != null && grandParent.lesser.equals(parent)) {
                    // My parent is my grandparents lesser branch
                    grandParent.lesser = current;
                    current.parent = grandParent;
                } else {
                    System.err.println("YIKES! Grandparent should have at least one non-NULL child which should be my parent.");
                }
                current.parent = grandParent;
            } else {
                root = current;
                root.parent = null;
            }

            if (parent.lesser != null && parent.lesser.equals(current)) {
                // LEFT
                parent.lesser = null;

                if (current.greater == null) {
                    current.greater = parent;
                    parent.parent = current;
                } else {
                    Node<T> lost = current.greater;
                    current.greater = parent;
                    parent.parent = current;
                    
                    parent.lesser = lost;
                    lost.parent = parent;
                }
            } else if (parent.greater != null && parent.greater.equals(current)) {
                // RIGHT
                parent.greater = null;

                if (current.lesser == null) {
                    current.lesser = parent;
                    parent.parent = current;
                } else {
                    Node<T> lost = current.lesser;
                    current.lesser = parent;
                    parent.parent = current;
                    
                    parent.greater = lost;
                    lost.parent = parent;
                }
            } else {
                // We really shouldn't get here
                System.err.println("YIKES! Parent should have at least one non-NULL child which should be me.");
            }

            parent = (TreapNode<T>) current.parent;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TreapPrinter.getString(this);
    }


    private static class TreapNode<T extends Comparable<T>> extends Node<T> {

        private int priority = Integer.MIN_VALUE;

        private TreapNode(TreapNode<T> parent, int priority, T value) {
            super(parent, value);
            this.priority = priority;
        }

        private TreapNode(TreapNode<T> parent, T value) {
            this(parent, RANDOM.nextInt(randomSeed), value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("priorty=").append(priority).append(" value=").append(value);
            if (parent != null) builder.append(" parent=").append(parent.value);
            builder.append("\n");
            if (lesser != null) builder.append("left=").append(lesser.toString()).append("\n");
            if (greater != null) builder.append("right=").append(greater.toString()).append("\n");
            return builder.toString();
        }
    }
    
    protected static class TreapPrinter {

        public static <T extends Comparable<T>> String getString(Treap<T> tree) {
            if (tree.root == null) return "Tree has no nodes.";
            return getString((TreapNode<T>)tree.root, "", true);
        }

        private static <T extends Comparable<T>> String getString(TreapNode<T> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix + (isTail ? "└── " : "├── ") + "(" + node.priority + ") " + node.value + "\n");
            List<Node<T>> children = null;
            if (node.lesser != null || node.greater != null) {
                children = new ArrayList<Node<T>>(2);
                if (node.lesser != null) children.add(node.lesser);
                if (node.greater != null) children.add(node.greater);
            }
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    TreapNode<T> treapNode = (TreapNode<T>) children.get(i);
                    builder.append(getString(treapNode, prefix + (isTail ? "    " : "│   "), false));
                }
                if (children.size() >= 1) {
                    TreapNode<T> treapNode = (TreapNode<T>) children.get(children.size() - 1);
                    builder.append(getString(treapNode, prefix + (isTail ? "    " : "│   "), true));
                }
            }

            return builder.toString();
        }
    }
}
