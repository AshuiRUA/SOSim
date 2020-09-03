import java.util.HashMap;
import java.util.HashSet;

public class Disease {
    String id;
    HashSet<String> hpo_terms = new HashSet<>();
    HashMap<String, HashMap<String, Double>> sim_gene_simType_simResult = new HashMap<>();

    Disease(String id)
    {
        this.id = id;
    }


    String getId()
    {
        return this.id;
    }

    HashSet<String> getHpo_terms()
    {
        return this.hpo_terms;
    }

    void add_hpoTerm(String id_hpoTerm)
    {
        this.hpo_terms.add(id_hpoTerm);
    }

    void setHpo_terms(HashSet<String> hpo_terms)
    {
        this.hpo_terms = hpo_terms;
    }

    int gene_sim_ranking(String name_gene, String sim_type)
    {
        // 先检查当前Disease，有没有和name_gene，用sim_type的方式计算相似度
        if ( ! sim_gene_simType_simResult.containsKey(name_gene))
        {
            System.out.println(id+"没有和"+name_gene+"计算出的相似度");
            return -1;
        }
        if (! sim_gene_simType_simResult.get(name_gene).containsKey(sim_type))
        {
            System.out.println(id+"没有和"+name_gene+"用"+sim_type+"计算出的相似度");
            return -1;
        }
        // 遍历当前Disease，所有用sim_type计算过相似度的Gene，找出name_gene的排名
        double sim = sim_gene_simType_simResult.get(name_gene).get(sim_type);
        int count = 0;
        int ranking = 1;
        for (String name_gene_temp : sim_gene_simType_simResult.keySet())
        {
            if (sim_gene_simType_simResult.get(name_gene_temp).containsKey(sim_type))
            {
                ++count;
            }
            if (sim_gene_simType_simResult.get(name_gene_temp).get(sim_type) > sim)
            {
                ++ranking;
            }
        }
        return ranking;
    }

    HashMap<String, Integer> gene_sim_ranking_allSimType(String name_gene)
    {
        HashMap<String, Integer> result = new HashMap<>();
        if (sim_gene_simType_simResult.containsKey(name_gene))
        {
            HashMap<String, Double> map_simType_simResult = sim_gene_simType_simResult.get(name_gene);
            for (String sim_type : map_simType_simResult.keySet())
            {
                double sim = sim_gene_simType_simResult.get(name_gene).get(sim_type);
                int ranking = 1;
                for (String name_gene_temp : sim_gene_simType_simResult.keySet())
                {
                    if (sim_gene_simType_simResult.get(name_gene_temp).get(sim_type) > sim)
                    {
                        ++ranking;
                    }
                }
                result.put(sim_type, ranking);
            }
        }
        // HashMap<sim_type, ranking>
        return result;
    }
}
