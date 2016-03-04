package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VLScmSnapshot;
import org.auscope.portal.server.vegl.VLScmSnapshotDao;
import org.auscope.portal.server.web.service.scm.Entries;
import org.auscope.portal.server.web.service.scm.Problem;
import org.auscope.portal.server.web.service.scm.Solution;
import org.auscope.portal.server.web.service.scm.Toolbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * A service for handling Scientific Code Marketplace templates.
 *
 * @author Geoff Squire
 *
 */
@Service
public class ScmEntryService {
    private final Log logger = LogFactory.getLog(getClass());

    public static final String SCM_URL = "http://ec2-54-206-9-187.ap-southeast-2.compute.amazonaws.com/scm";
    
    /** Puppet module template resource */
    protected static final String PUPPET_TEMPLATE =
        "org/auscope/portal/server/web/service/template.pp";

    private VLScmSnapshotDao vlScmSnapshotDao;
    private VelocityEngine velocityEngine;
    private VEGLJobManager jobManager;
    private CloudComputeService[] cloudComputeServices;

    /**
     * Create a new instance.
     */
    @Autowired
    public ScmEntryService(VLScmSnapshotDao vlScmSnapshotDao,
                           VEGLJobManager jobManager,
                           VelocityEngine velocityEngine,
                           CloudComputeService[] cloudComputeServices) {
        super();
        this.vlScmSnapshotDao = vlScmSnapshotDao;
        this.jobManager = jobManager;
        this.setVelocityEngine(velocityEngine);
        this.cloudComputeServices = cloudComputeServices;
    }

    /**
     * Return id of the VM for entry at computeServiceId, or null if not found.
     *
     * @param entryId SCM template entry ID
     * @param computeServiceId ID of the CloudComputeService provider
     * @return Snapshot id if one exists, otherwise null
     */
    public String getScmEntrySnapshotId(String entryId,
                                        String computeServiceId) {
        String vmId = null;
        VLScmSnapshot snapshot = vlScmSnapshotDao
            .getSnapshotForEntryAndProvider(entryId, computeServiceId);
        if (snapshot != null) {
            vmId = snapshot.getComputeVmId();
        }
        return vmId;
    }

    /**
     * Update job (jobId) with vmId and computeServiceId for solution
     * if we have one.
     *
     * @param jobId String job ID
     * @param solutionId String solution URL
     * @throws PortalServiceException
     */
    public void updateJobForSolution(String jobId, String solutionId)
        throws PortalServiceException {
        //Lookup our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.warn("Unable to lookup job with id " + jobId + ": " + ex.getMessage());
            logger.debug("exception:", ex);
            throw new PortalServiceException("Unable to lookup job with id " + jobId, ex);
        }

        // Store the solutionId in the job
        job.setSolutionId(solutionId);
        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error updating job " + job, ex);
            throw new PortalServiceException("Error updating job for solution: ", ex);
        }
    }

    /**
     * Return the puppet module for SCM solution.
     *
     * Generates a puppet module that will provision a VM suitable for
     * running a job using the SCM entry.
     *
     * Placeholder parameters:
     *
     * <table>
     * <tr><td>sc-name</td><td>Name of the scientific code</td></tr>
     * <tr><td>source</td><td>Map of source parameters</td></tr>
     * <tr><td>source.type</td><td>Source repository type ("git", "svn")</td></tr>
     * <tr><td>source.url</td><td>Source repository URL</td></tr>
     * <tr><td>source.checkout</td><td>Checkout target for source repository</td></tr>
     * <tr><td>source.exec</td><td>Shell command to execute after source checkout.</td></tr>
     * <tr><td>system_packages</td><td>List of system packages</td></tr>
     * <tr><td>python_packages</td><td>List of python packages</td></tr>
     * <tr><td>python_requirements</td><td>Path to a pip requirements.txt file in the source</td></tr>
     * </table>
     *
     * @param solutionUrl String URL of the SCM solution
     * @return String contents of the puppet module
     */
    public String createPuppetModule(String solutionUrl) {
        // Fetch the solution entry from the SCM
        Solution solution = getScmSolution(solutionUrl);

        // Create a velocity template vars map from the entry
        Map<String, Object> vars = puppetTemplateVars(solution);

        // Create the puppet module
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                                                           PUPPET_TEMPLATE,
                                                           "UTF-8",
                                                           vars);
    }

    /**
     * Retrieve and decode an SCM entry.
     *
     * @param entryUrl String URL of the catalogue entry
     * @return Map<String, Object> deserialization of the json response
     *
     */
    public Solution getScmSolution(String entryUrl) {
        Solution solution = null;
        RestTemplate rest = new RestTemplate();

        try {
            solution = rest.getForObject(entryUrl, Solution.class);
        }
        catch (RestClientException ex) {
            logger.error("Failed to get SSC solution (" + entryUrl + ")", ex);
        }

        return solution;
    }

    /**
     * Retieve and return listing of all solutions available.
     *
     */
    public List<Solution> getSolutions() {
        return getSolutions(null);
    }

    /**
     * Return the Solutions for a specific Problem.
     *
     * @param problem Problem to find Solutions for, or all Solutions if null.
     * @return List<Solution> list of Solutions if any.
     *
     */
    public List<Solution> getSolutions(Problem problem) {
        StringBuilder url = new StringBuilder();
        RestTemplate rest = new RestTemplate();
        Entries solutions;

        url.append(SCM_URL).append("/solutions");
        if (problem != null) {
            url.append("?problem={problem_id}");
            solutions = rest.getForObject(url.toString(),
                                          Entries.class,
                                          problem.getId());
        }
        else {
            solutions = rest.getForObject(url.toString(), Entries.class);
        }

        return usefulSolutions(solutions.getSolutions());
    }

    /**
     * Return list of Solutions in solutions that are usable in this portal.
     *
     * Finds Solutions in solutions that can be used in this
     * portal. Either they refer to at least one image we can use, or
     * supply the information we need to create an image at runtime.
     *
     * Where a Solution has image(s) already, filter the set to those
     * we can use. Currently this means at a cloud provider we can
     * use, and we assume the image already has the portal
     * infrastructure in place.
     *
     * @param solutions List<Solution> solutions from the SSC
     * @return List<Solution> subset of solutions that are usable
     *
     */
    private List<Solution> usefulSolutions(List<Solution> solutions) {
        ArrayList<Solution> useful = new ArrayList<Solution>();

        // Collect our set of available providers
        Set<String> providers = new HashSet<String>();
        for (CloudComputeService ccs: cloudComputeServices) {
            providers.add(ccs.getId());
        }
        
        for (Solution solution: solutions) {
        	useful.add(solution);
        	// Solution with toolbox with at least one image at a
            // provider we can use is useful.
            for (Map<String, String> image:
                     solution.getToolbox(true).getImages()) {
                if (providers.contains(image.get("provider"))) {
                    useful.add(solution);
                    break;
                }
            }
        }
        
        return useful;
    }

    /**
     * Return the Solution object for job (if known).
     *
     * @param job VEGLJob object
     * @returns Solution object if job has a solutionId
     */
    public Solution getJobSolution(VEGLJob job) {
        Solution solution = null;

        if (job != null) {
            String solutionId = job.getSolutionId();
            if (solutionId != null) {
                solution = getScmSolution(solutionId);
            }
        }

        return solution;
    }

    /**
     * Return a map of computeServiceId to imageIds valid for job.
     *
     * @return Map<String, Set<String>> with images for job, or null.
     */
    public Map<String, Set<String>> getJobImages(Integer jobId) {
        if (jobId == null) {
            return null;
        }

        Map<String, Set<String>> images = new HashMap<String, Set<String>>();
        VEGLJob job = jobManager.getJobById(jobId);
        if (job != null) {
            Solution solution = getJobSolution(job);
            if (solution != null) {
                for (Map<String, String> img:
                         solution.getToolbox(true).getImages()) {
                    String providerId = img.get("provider");
                    Set<String> vms = images.get(providerId);
                    if (vms == null) {
                        vms = new HashSet<String>();
                        images.put(providerId, vms);
                    }
                    vms.add(img.get("image_id"));
                }
            }
        }

        return images;
    }

    /**
     * Return a Set of compute service ids with images for job with jobId.
     *
     * @return Set<String> of compute service ids for job, or null if jobId == null.
     */
    public Set<String> getJobProviders(Integer jobId) {
        Map<String, Set<String>> images = getJobImages(jobId);
        if (images != null) {
            return images.keySet();
        }
        return null;
    }

    private Map<String, Object> puppetTemplateVars(Solution solution) {
        Map<String, Object> vars = new HashMap<String, Object>();
        // Make sure we have full Toolbox details.
        Toolbox toolbox = solution.getToolbox(true);

        vars.put("sc_name", safeScName(toolbox));
        vars.put("source", toolbox.getSource());

        ArrayList<String> systemPackages = new ArrayList<String>();
        ArrayList<String> pythonPackages = new ArrayList<String>();
        ArrayList<String> requirements = new ArrayList<String>();
        // Merge dependencies from solution and toolbox
        dependencies(toolbox.getDependencies(),
                     systemPackages,
                     pythonPackages,
                     requirements);
        dependencies(solution.getDependencies(),
                     systemPackages,
                     pythonPackages,
                     requirements);
        vars.put("system_packages", systemPackages);
        vars.put("python_packages", pythonPackages);
        vars.put("python_requirements", requirements);
        return vars;
    }

    private void dependencies(List<Map<String, String>> deps,
                              List<String> systemPackages,
                              List<String> pythonPackages,
                              List<String> requirements) {
        for (Map<String, String> dep: deps) {
            switch (dep.get("type")) {
            case "system":
                systemPackages.add(dep.get("name"));
                break;
            case "python":
                if (dep.containsKey("path")) {
                    requirements.add(dep.get("path"));
                }
                else {
                    pythonPackages.add(dep.get("name"));
                }
                break;
            default:
                logger.warn("Unknown dependency type (" + dep + ")");
            }
        }
    }

    /**
     * Return a safe name for the scientific code used by toolbox.
     *
     * The name will be used to generate puppet classes and the path
     * where the code will be installed on the VM.
     *
     * Simple solution: strip out all non-word characters as defined
     * by the java regex spec.
     *
     */
    private String safeScName(Toolbox toolbox) {
        return toolbox.getName().replaceAll("\\W", "");
    }

    /**
     * @return the vlScmSnapshotDao
     */
    public VLScmSnapshotDao getVlScmSnapshotDao() {
        return vlScmSnapshotDao;
    }

    /**
     * @param vlScmSnapshotDao the vlScmSnapshotDao to set
     */
    public void setVlScmSnapshotDao(VLScmSnapshotDao vlScmSnapshotDao) {
        this.vlScmSnapshotDao = vlScmSnapshotDao;
    }

    /**
     * @return the velocityEngine
     */
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    /**
     * @param velocityEngine the velocityEngine to set
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
}
