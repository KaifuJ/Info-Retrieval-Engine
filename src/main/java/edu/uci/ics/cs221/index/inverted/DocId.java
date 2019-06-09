package edu.uci.ics.cs221.index.inverted;

public class DocId {
    public int segNum;
    public int docId;

    public DocId(int segNum, int docId){
        this.segNum = segNum;
        this.docId = docId;
    }

    @Override
    public int hashCode(){
        return segNum ^ docId;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;

        if(!(obj instanceof DocId)) return false; // "null of [Class]" is false

        DocId other = (DocId)obj;
        return this.segNum == other.segNum && this.docId == other.docId;
    }

}
