package com.jercode.ca1_pill_and_capsule_analyser;

//terminology: disjoint sets (connected components/pixels/pills/capsules)
public class UniFindOp {
    private int[] parent; //array holds the parent of each element
    private int[] rank; //approximate depth of the tree rooted at i
    int[] size; //stores the size of each component

    public UniFindOp(int size) {
        parent=new int[size]; //initializes the arrays parent, rank, and size with the size specified by the argument
        rank=new int[size];
        this.size=new int[size];

        for (int i=0; i<size; i++) {
            parent[i]=i; //Each element is initially its own parent.
            rank[i]=0;//at first, all elements are at the base level (flat tree)
            this.size[i] =1; //each component is its own set with size 1
        }
    }

    public int find(int p) { //implements path compression (flattens the structure of the tree whenever it is traversed)
        if (parent[p]!=p) {//checks if parent[p] is not p (not its own parent),
                            //if so, it recursively finds the root and updates parent[p] to point directly to the root.
            //System.out.println("Path compression for " + p + ",root " + parent[p]);
            parent[p]=find(parent[p]);
        }
        return parent[p];
    }

    public void union(int p, int q) {//uses find and union by rank to merge two subsets into a single subset
        int rootP = find(p);
        int rootQ = find(q);
        //System.out.println("Union operation for " + p + " and " + q + "; roots " + rootP + ", " + rootQ);

        if (rootP != rootQ) {
            if (rank[rootP] > rank[rootQ]) {//tree with lower rank is attached under the tree with higher rank
                parent[rootQ] = rootP;
                size[rootP] += size[rootQ];
                //System.out.println("Root " + rootQ + " is now pointing to " + rootP);
            } else if (rank[rootP] < rank[rootQ]) { //tree with lower rank is attached under the tree with higher rank
                parent[rootP] = rootQ;
                size[rootQ]+=size[rootP];
                //System.out.println("Root " + rootP + " is now pointing to " + rootQ);
            } else { //else one is arbitrarily made the root and its rank is incremented by one
                parent[rootQ] = rootP;
                rank[rootP]++;
                size[rootP]+=size[rootQ]; //size of the new root is updated by adding the sizes of both sets
                //System.out.println("Root " + rootQ + " is now pointing to " + rootP + " and rank of " + rootP + "is incremented");
            }
            //System.out.println("Uniting " + p + " and " + q + "; New Size: " + size[find(p)]);
        }
    }

    public boolean isSignificant(int p, double minSize, double maxSize) {
        int root=find(p);   //determines if the component containing the element p is within a specific size range.
        int componentSize=size[root];//checks if size of set, rooted at p, is within the given bounds
        boolean isSignificant=componentSize >= minSize && componentSize <= maxSize;//min/max values come from displayColoredComponents()
        //log for debug
        //System.out.println("Pixel: " + p + " Root: " + root + " Size: " + componentSize + " isSignificant: " + isSignificant);
        return isSignificant;
    }
}

