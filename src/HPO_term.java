import java.util.ArrayList;
import java.util.HashSet;
import java.math.*;

public class HPO_term {
    String id;
    HashSet<String> parents = new HashSet<>();
    HashSet<String> ancestors = new HashSet<>();
    String id_MIMIA;
    HashSet<String> diseases = new HashSet<>();
    int D = 0;
    double IC = 0;
    

    HPO_term(String id){
        this.id = id;
    }
    public void add_parent(String id){
        parents.add(id);
    }

    public void add_ancestor(String id){ ancestors.add(id); }

    public void add_disease(String id_disease){ diseases.add(id_disease); }

    public void setParents(HashSet<String> parents){this.parents = parents;}

    public void setAncestors(HashSet<String> ancestors){this.ancestors = ancestors;}

    public void setId_MIMIA(String id_MIMIA){ this.id_MIMIA = id_MIMIA; }

    public void set_name_MIMIA(String id){
        this.id_MIMIA = id;
    }

    public HashSet<String> getParents()
    {
        return this.parents;
    }

    public HashSet<String> getAncestors()
    {
        return this.ancestors;
    }

    public String getId()
    {
        return this.id;
    }

    public String getId_MIMIA()
    {
        return this.id_MIMIA;
    }

    public int getD()
    {
        return this.D;
    }

    public double getIC() { return this.IC; }

    public void D_init() throws Exception { D = diseases.size(); }

    public void IC_init(int num_diseases)
    {
        double temp = (double) D / (double) num_diseases;
        IC = (-1) * Math.log(temp);
    }


}
