package edu.uci.ics.cs221.search;

import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class IcsSearchEngine {

    public InvertedIndexManager iim;
    private String   path;
    public double d;
    public double[] RankScore;
    public List<Pair<Document,Double>> SearchResult;
    private int docNum;
    public  List<Pair<Integer,Double>> PageRankScores;

    /**
     * Initializes an IcsSearchEngine from the directory containing the documents and the
     *
     */

    public static IcsSearchEngine createSearchEngine(Path documentDirectory, InvertedIndexManager indexManager) {
        return new IcsSearchEngine(documentDirectory, indexManager);
    }

    private IcsSearchEngine(Path documentDirectory, InvertedIndexManager indexManager) {
        this.iim = indexManager;
        this.path = documentDirectory.toString();
        this.d = 0.85;
        this.SearchResult = new ArrayList<>();
        this.PageRankScores = new ArrayList<>();
        File file = new File(path + "/cleaned");
        File[] tempList = file.listFiles();
        this.docNum = tempList.length;
    }

    /**
     * Writes all ICS web page documents in the document directory to the inverted index.
     */
    public void writeIndex() {
        try {
            File file = new File(path + "/cleaned");
            File[] tempList = file.listFiles();
            for (int i = 0; i < tempList.length; i++) {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(tempList[i]));
                BufferedReader br = new BufferedReader(reader);
                String text = "";
                text += br.readLine()+"\n"; // add DocID
                text += br.readLine()+"\n"; // add url
                text += br.readLine(); // add text
                iim.addDocument(new Document(text));
                docNum += 1;
                br.close();
                reader.close();
            }
            System.out.println(1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Computes the page rank score from the "id-graph.tsv" file in the document directory.
     * The results of the computation can be saved in a class variable and will be later retrieved by `getPageRankScores`.
     */
    public void computePageRank(int numIterations) {
        try {
            File file = new File(path + "/id-graph.tsv");
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String line;
            Map<Integer,List<Integer>> m = new HashMap<>();
            int [] degree = new int[docNum];
            Arrays.fill(degree,0);
            while((line = br.readLine())!=null){
                String [] arr = line.split("\\s+");
                int from = Integer.parseInt(arr[0]);
                int to = Integer.parseInt(arr[1]);
                degree[from] += 1;
                // update out degree
                if(m.containsKey(to)) m.get(to).add(from);
                else {
                    m.put(to,new ArrayList<>(Arrays.asList(from)));
                }
                // update relation
            }
            br.close();
            reader.close();
            RankScore = new double[docNum];
            Arrays.fill(RankScore,1);
            for(int i = 0; i < numIterations; i++){
                double[] tmp = new double[docNum];
                for(int j = 0; j < RankScore.length; j++){
                    if(m.containsKey(j) == false){
                        tmp[j] = 1-d;
                        continue;
                    }
                    List<Integer> inUrl = m.get(j);
                    Double count = 0.0;
                    for(int t : inUrl){
                        count += RankScore[t]/degree[t];
                    }
                    tmp[j] = 1-d + count * d ;
                }
                RankScore = tmp;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Gets the page rank score of all documents previously computed. Must be called after `cmoputePageRank`.
     * Returns an list of <DocumentID - Score> Pairs that is sorted by score in descending order (high scores first).
     */
    public List<Pair<Integer, Double>> getPageRankScores() {

        PageRankScores.clear();
        for(int i = 0; i < RankScore.length; i++){
            PageRankScores.add(new Pair<>(i,RankScore[i]));
        }
        Collections.sort(PageRankScores,((o1, o2) -> {
            if(o1.getRight() > o2.getRight()) return -1;
            else if(o1.getRight() < o2.getRight()) return 1;
            else return 0;
        }));
        return PageRankScores;
    }

    /**
     * Searches the ICS document corpus and returns the top K documents ranked by combining TF-IDF and PageRank.
     *
     * The search process should first retrieve ALL the top documents from the InvertedIndex by TF-IDF rank,
     * by calling `searchTfIdf(query, null)`.
     *
     * Then the corresponding PageRank score of each document should be retrieved. (`computePageRank` will be called beforehand)
     * For each document, the combined score is  tfIdfScore + pageRankWeight * pageRankScore.
     *
     * Finally, the top K documents of the combined score are returned. Each element is a pair of <Document, combinedScore>
     *
     *
     * Note: We could get the Document ID by reading the first line of the document.
     * This is a workaround because our project doesn't support multiple fields. We cannot keep the documentID in a separate column.
     */
    public Iterator<Pair<Document, Double>> searchQuery(List<String> query, int topK, double pageRankWeight) {
        Iterator<Pair<Document, Double>> it = iim.searchTfIdf(query,null);
        PriorityQueue<Pair<Document,Double>> pq = new PriorityQueue<Pair<Document,Double>>(topK, new Comparator<Pair<Document, Double>>() {
            @Override
            public int compare(Pair<Document, Double> o1, Pair<Document, Double> o2) {
                if(o2.getRight() > o1.getRight()){
                    return -1;
                }
                else if(o2.getRight() < o1.getRight()){
                    return 1;
                }
                else {
                    return 0;
                }
            }
        });
        SearchResult.clear();
        while(it.hasNext()){
            Pair<Document, Double> tmp = it.next();
            String[] s = tmp.getLeft().getText().split("\n");
            int id = Integer.parseInt(s[0]);
            if(pq.size() == topK && tmp.getRight()+RankScore[id] > pq.peek().getRight()) {
                pq.poll();
                pq.offer(new Pair<Document, Double>(tmp.getLeft(), tmp.getRight() + RankScore[id]));
            }
            if(pq.size() < topK)
                pq.offer(new Pair<Document, Double>(tmp.getLeft(), tmp.getRight() + RankScore[id]));
        }
        for(int i = 0; i < topK; i++){
            SearchResult.add(pq.poll());
        }
        Collections.reverse(SearchResult);
        return SearchResult.iterator();
    }

}