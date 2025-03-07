package uk.ac.ebi.pride.cluster.tools.reanalysis.processing;

import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.ProcessingException;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException;

import java.util.HashMap;

public interface ProcessingExecutable {

    /**
     * Executes an external executable from another tool.
     *
     * @throws uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException
     * @throws uk.ac.ebi.pride.cluster.tools.reanalysis.exception.ProcessingException
    */
    boolean process() throws UnspecifiedException, ProcessingException, ClusterDataImporterException;

    /**
     *
     * @return the parameters of the executable
     */
    HashMap<String, String> getParameters();
}
