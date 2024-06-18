package provaKDTree;


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

    public KDTree(List<Sample> points) {
        root = buildTree(points, 0);
    }

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
        points.sort(Comparator.comparingDouble(p -> (axis == 0) ? p.x : p.y));
        int medianIndex = points.size() / 2;
        KDNode node = new KDNode(points.get(medianIndex));

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

    private void kNearestNeighbors(KDNode node, Sample target, int k, int depth, PriorityQueue<Sample> pq) {
        if (node == null) {
            return;
        }
        //quanto disto dal nodo corrente. 
        double distance = target.distance(node.point);
        //se non ho niente in pq allora inizio ad inserire qualcosa.
        if (pq.size() < k) {
            pq.offer(node.point);
        }//se la distanza con il nodo corrente è minore dell'ultima distanza inserita allora 
        //togli il nodo inserito e aggiungi quello nuovo. 
        else if (distance < target.distance(pq.peek())) {
            pq.poll();
            pq.offer(node.point);
        }

        int axis = depth % 2;
        KDNode nearNode = (axis == 0 && target.x < node.point.x) || (axis == 1 && target.y < node.point.y) ? node.left : node.right;
        KDNode farNode = (nearNode == node.left) ? node.right : node.left;

        kNearestNeighbors(nearNode, target, k, depth + 1, pq);

        if (pq.size() < k || Math.abs((axis == 0 ? target.x - node.point.x : target.y - node.point.y)) < target.distance(pq.peek())) {
            kNearestNeighbors(farNode, target, k, depth + 1, pq);
        }
    }
}