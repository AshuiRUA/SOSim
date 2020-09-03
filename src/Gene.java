import java.util.HashSet;

public class Gene {
    String name;
    HashSet<String> hpo_terms = new HashSet<>();
    Gene(String name)
    {
        this.name = name;
    }
    String getName()
    {
        return this.name;
    }
    HashSet<String> getHpo_terms()
    {
        return  this.hpo_terms;
    }
    void add_hpoTerm(String id_hpoTerm)
    {
        this.hpo_terms.add(id_hpoTerm);
    }
    void setName(String name)
    {
        this.name = name;
    }
    void setHpo_terms(HashSet<String> hpo_terms)
    {
        this.hpo_terms = hpo_terms;
    }
}
