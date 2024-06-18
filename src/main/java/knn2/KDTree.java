package knn2;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


/*
KD-Tree for an efficient implementation of the K-NN.
Do not touch!
*/
class KDTree {

    private KDNode root;
    //Prende in input una lista di punti, costruisce l'albero a partire dai 
    //punti forniti
    public KDTree(List<Sample> points) {
        root = buildTree(points, 0);
    }
    //Questa classe è essenziale, rappresenta un nodo all'interno dell'albero. 
    //è come la struct TNode vista ad ASD
    private static class KDNode {
        Sample point;
        KDNode left, right;

        KDNode(Sample point) {
            this.point = point;
        }
    }

    private KDNode buildTree(List<Sample> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        int axis = depth % 2; // 0 for x, 1 for y
        //p: ciascuno ogetto nella lista quindi un'istanza di sample. 
        //Siccome ogni nodo è composto da 5 valori dobbiamo 
        //sceglierne uno per effettuare l'ordinamento e il build dell'albero.
        //se axis uguale a 0 allora si ordinerà per p.angoloLongTang altrimenti per p.pos..
        points.sort(Comparator.comparingDouble(p -> (axis == 0) ? p.differenzaSxDxSensor : p.differenzaSxMinDxMinSensor));
        int medianIndex = points.size() / 2;  
        KDNode node = new KDNode(points.get(medianIndex)); //root
        //siccome ho diviso i punti della lista allora da 0 a medianindex costruisco 
        //il sottoalbero sinistro. da medianindex+1 fino alla fine quello destro.
        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);

        return node;
    }
        public List<Sample> kNearestNeighbors(Sample target, int k) {
        //Inserisco i nodi trovati nella coda. Creo un comparatore personalizzato che utilizza il il sample target per il metodo distance. 
        //Ordina poi in ordine decrescente
        PriorityQueue<Sample> pq = new PriorityQueue<>(k, Comparator.comparingDouble(target::distance).reversed());
        kNearestNeighbors(root, target, k, 0, pq);
        return new ArrayList<>(pq);
    }

   private void kNearestNeighbors(KDNode node,Sample target, int k, int depth, PriorityQueue<Sample> pq) {
    if (node == null) {
        return;
    }
    
    // Calculate distance from the target to the current node using the features.
    double distance = target.distance(node.point);
    
    // If the priority queue is not full, add the current node.
    if (pq.size() < k) {
        pq.offer(node.point);
    } 
    // If the current node is closer than the farthest node in the priority queue, replace the farthest node.
    else if (distance < target.distance(pq.peek())) {
        pq.poll();
        pq.offer(node.point);
    }

    // Determine the current axis (feature) to split on.
    int axis = depth % 5; // There are 7 features to consider
    
    // Determine the nearer and farther subtrees based on the selected feature.
    KDNode nearNode;
    KDNode farNode;
    if ( 
        (axis == 0 && target.sensoreFrontale < node.point.sensoreFrontale) || 
        (axis == 1 && target.differenzaSxDxSensor < node.point.differenzaSxDxSensor) || 
        (axis == 2 && target.differenzaSxMinDxMinSensor < node.point.differenzaSxMinDxMinSensor) || 
        (axis == 3 && target.posizioneRispettoAlCentro < node.point.posizioneRispettoAlCentro) || 
        (axis == 4 && target.angoloLongTang < node.point.angoloLongTang)) {
        nearNode = node.left;
        farNode = node.right;
    } else {
        nearNode = node.right;
        farNode = node.left;
    }

    // Recur on the nearer subtree.
    kNearestNeighbors(nearNode, target, k, depth + 1, pq);

    // Check whether to search the farther subtree based on the distance to the splitting plane.
    boolean shouldCheckFarNode;
   if (axis == 0) {
        shouldCheckFarNode = pq.size() < k || Math.abs(target.sensoreFrontale - node.point.sensoreFrontale) < target.distance(pq.peek());
    } else if (axis == 1) {
        shouldCheckFarNode = pq.size() < k || Math.abs(target.differenzaSxDxSensor - node.point.differenzaSxDxSensor) < target.distance(pq.peek());
    } else if (axis == 2) {
        shouldCheckFarNode = pq.size() < k || Math.abs(target.differenzaSxMinDxMinSensor - node.point.differenzaSxMinDxMinSensor) < target.distance(pq.peek());
    } else if (axis == 3) {
        shouldCheckFarNode = pq.size() < k || Math.abs(target.posizioneRispettoAlCentro - node.point.posizioneRispettoAlCentro) < target.distance(pq.peek());
    } else{
        shouldCheckFarNode = pq.size() < k || Math.abs(target.angoloLongTang - node.point.angoloLongTang) < target.distance(pq.peek());
    }

    // Recur on the farther subtree if necessary.
    if (shouldCheckFarNode) {
        kNearestNeighbors(farNode, target, k, depth + 1, pq);
    }
}
}