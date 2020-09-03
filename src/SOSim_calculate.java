import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class SOSim_calculate implements CalculateBehavior_hpoTerm_similarity {
    @Override
    public HashMap<String,Double> calculate(HPO_term hpo_term1, HPO_term hpo_term2, TreeMap<String, HPO_term> HPO_terms, int num_diseases)
    {
        String sim_type = "SOSim";
        HashMap<String,Double> results = new HashMap<>();
        if (hpo_term1.getId().equals(hpo_term2.getId())
                || (hpo_term1.ancestors.contains(hpo_term2.getId()) && hpo_term1.getD() == hpo_term2.getD())
                || (hpo_term2.ancestors.contains(hpo_term1.getId()) && hpo_term1.getD() == hpo_term2.getD())
            )
        {
            // 同一个HPO_term
            results.put(sim_type, hpo_term1.getIC() + 3);
            return results;
        }
        else if (hpo_term1.ancestors.contains(hpo_term2.getId()))
        {
            // hpo_term1是hpo_term2的子孙，hpo_term2是hpo_term1的祖先
            results.put(sim_type, hpo_term1.getIC() - (hpo_term2.getD() / (double)num_diseases));
            return results;
        }
        else if(hpo_term2.ancestors.contains(hpo_term1.getId()))
        {
            // hpo_term1是hpo_term2的祖先，hpo_term2是hpo_term的子孙
            HPO_term hpo_term1_MIMIA = HPO_terms.get(hpo_term1.getId_MIMIA());
            HashMap<String,Double> sim_hpoTerm1_hpoTerm1MIMIA = calculate(hpo_term1, hpo_term1_MIMIA, HPO_terms, num_diseases);
            results.put(sim_type, sim_hpoTerm1_hpoTerm1MIMIA.get("SOSim") + (hpo_term2.getD() / (double)num_diseases));
            return results;
        }
        else
        {
            // 有共同祖先
            // 找到MICA
            HPO_term hpoTerm_MICA = null;
            double max_IC = -1000000;
            for (String id_parent_1 : hpo_term1.ancestors)
            {
                if (hpo_term2.ancestors.contains(id_parent_1))
                {
                    HPO_term hpoTerm_commonAncestor = HPO_terms.get(id_parent_1);
                    if (hpoTerm_commonAncestor.getIC() > max_IC && Double.isFinite(hpoTerm_commonAncestor.getIC()))
                    {
                        hpoTerm_MICA = hpoTerm_commonAncestor;
                        max_IC = hpoTerm_commonAncestor.getIC();
                    }
                }
            }
            assert hpoTerm_MICA != null;
            if (hpoTerm_MICA.getId().equals("HP:0000001"))
            {
                results.put("SOSim", -1.0);
            }
            else
            {
                HPO_term hpoTerm_MICA_MIMIA = HPO_terms.get(hpoTerm_MICA.getId_MIMIA());
                HashMap<String,Double> sim_hpoTerm1_MICAMIMIA = calculate(hpo_term1, hpoTerm_MICA_MIMIA, HPO_terms, num_diseases);
                double c =  ((double) hpoTerm_MICA_MIMIA.getD() - (double) hpoTerm_MICA.getD()) / (double) hpoTerm_MICA.getD();
                double temp = (double) hpo_term2.getD() / (double)num_diseases;
                results.put("SOSim", sim_hpoTerm1_MICAMIMIA.get("SOSim") + c * temp);
            }
            return results;
        }
    }
}
