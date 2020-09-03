import java.util.HashSet;
import java.util.Random;

public class Sim_aTerm_itsAncestor {
    public static void main(String[] args) throws Exception {
        String path_oboFile = "data/source_data/hp.obo";
        String path_DiseaseAnnotationFile = "data/source_data/DiseaseAnnotation.txt";
        String path_GeneAnnotationFile = "data/source_data/gene_phenotype_disease.txt";
        boolean restricted_geneAnnotation = false;
        HPO hpo = new HPO(path_oboFile, path_DiseaseAnnotationFile, path_GeneAnnotationFile, restricted_geneAnnotation);

        HPO_term hpoTerm_source = hpo.HPO_terms.get("HP:0009824");

        System.out.format("挑出的HPO term为%s\n", hpoTerm_source.getId());

        System.out.println(hpoTerm_source.getId());
        System.out.println(hpo.calculate_hpoTerm_similarity(hpoTerm_source, hpoTerm_source, Calculation_type.SOSim));

        System.out.println("和祖先计算相似度");
        HashSet<String> ancestors = hpoTerm_source.getAncestors();
        for (String id_hpoTerm_ancestor : ancestors)
        {
            System.out.println(id_hpoTerm_ancestor);
            System.out.println(hpo.calculate_hpoTerm_similarity(hpoTerm_source, hpo.HPO_terms.get(id_hpoTerm_ancestor), Calculation_type.SOSim));
        }

        System.out.println("和子孙计算相似度");
        for (String id_hpoTerm :hpo.HPO_terms.keySet())
        {
            HPO_term hpoTerm_temp = hpo.HPO_terms.get(id_hpoTerm);
            if (hpoTerm_temp.ancestors.contains(hpoTerm_source.getId()))
            {
                System.out.println(hpoTerm_temp.getId());
                System.out.println(hpo.calculate_hpoTerm_similarity(hpoTerm_source, hpoTerm_temp, Calculation_type.SOSim));
            }
        }
    }
}
