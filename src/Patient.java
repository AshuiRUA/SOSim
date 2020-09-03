import java.util.*;

public class Patient extends Disease {
    public String name_causativeGene = new String();

    Patient(String id) {
        super(id);
    }
    Patient(String id, Disease disease, String name_causativeGene, HashMap<String, Double>map_idHPOTerm_frequency)
    {
        super(id);
        this.name_causativeGene = name_causativeGene;
        Random generator = new Random();
        while (this.hpo_terms.size() == 0)
        {
            for (String id_hpoTerm_temp : map_idHPOTerm_frequency.keySet()) {
                if (disease.hpo_terms.contains(id_hpoTerm_temp)) {
                    double frequency = map_idHPOTerm_frequency.get(id_hpoTerm_temp);
                    if (generator.nextDouble() < frequency) {
                        this.hpo_terms.add(id_hpoTerm_temp);
                    }
                }
            }
        }
    }

    void add_noise(HPO hpo)
    {
        int num_toChange = (int) Math.floor(this.hpo_terms.size() * 0.5);
        Random random = new Random();
        String[] list_hpoTerm = this.hpo_terms.toArray(new String[this.hpo_terms.size()]);
        List<Integer> list_index_toChange = new ArrayList<>();
        // 随机决定哪些要被变换
        for (int i = 0; i<num_toChange; ++i)
        {
            list_index_toChange.add(random.nextInt(list_hpoTerm.length));
        }
        HashSet<String> temp = new HashSet<>();
        for (int i = 0; i < list_hpoTerm.length; ++i)
        {
            if (list_index_toChange.contains(i))
            {
                HPO_term hpoTerm_temp = hpo.HPO_terms.get(list_hpoTerm[i]);
                if (hpoTerm_temp.id != "HP:0000001")
                {
                    temp.add(hpoTerm_temp.parents.toArray(new String[hpoTerm_temp.parents.size()])[0]);
                }
            }
            else
            {
                temp.add(list_hpoTerm[i]);
            }
        }
        this.hpo_terms=temp;
    }

}
