
public class Simulated_patients
{
    public static void main(String[] args) throws Exception
    {
        // 参数
        boolean restricted_geneAnnotation = false;
        String Disease_name = "Masino_frequency";
        int number_patientPerDisease = 25;
        boolean add_noise = false;
        boolean unsymmetrical = false;
        ////////////////////////////////////////////////////////////////////////

        // 构建hpo
        String path_oboFile = "data/source_data/hp.obo";
        String path_DiseaseAnnotationFile = "data/source_data/DiseaseAnnotation.txt";
        String path_GeneAnnotationFile = "data/source_data/gene_phenotype_disease.txt";
        HPO hpo = new HPO(path_oboFile, path_DiseaseAnnotationFile, path_GeneAnnotationFile, restricted_geneAnnotation);

        // 生成模拟病人，和基因们计算相似度
        String path_frequencyFile = String.format("data/frequency/%s.txt", Disease_name);
        hpo.generatePatient_useFrequencyFile(path_frequencyFile, number_patientPerDisease, add_noise);
        // 为每个病人计算和所有基因的相似度
        hpo.calculate_sim_Patients_Genes(hpo.Patients.keySet(), hpo.Genes.keySet(), Calculation_type.All, unsymmetrical);

        String path_outputFile;
        if (unsymmetrical)
        {
            if (add_noise)
            {
                path_outputFile = String.format("data/result/%s_generate100Patient_geneAnnotationUnrestricted_CSIsrootNot1_allGenes_addNoise.txt", Disease_name);
            }
            else
            {
                path_outputFile = String.format("data/result/%s_generate100Patient_geneAnnotationUnrestricted_CSIsrootNot1_allGenes.txt", Disease_name);
            }
        }
        else
        {
            if (add_noise)
            {
                path_outputFile = String.format("data/result/%s_generate100Patient_geneAnnotationUnrestricted_CSIsrootNot1_allGenes_addNoise_symmetrical.txt", Disease_name);
            }
            else
            {
                path_outputFile = String.format("data/result/%s_generate100Patient_geneAnnotationUnrestricted_CSIsrootNot1_allGenes_symmetrical.txt", Disease_name);
            }
        }

        hpo.output_patientGene_ranking(path_outputFile);
    }
}
