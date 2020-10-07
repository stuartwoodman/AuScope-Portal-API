package org.auscope.portal.server.vegl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A specialisation of a generic cloud job for the VEGL Portal
 *
 * A VEGL job is assumed to write all output to a specific cloud location
 * @author Josh Vote
 *
 */
@Entity
@Table(name = "jobs")
public class VEGLJob extends CloudJob implements Cloneable {
    private static final long serialVersionUID = -57851899164623641L;
    
    @SuppressWarnings("unused")
    @Transient
    private final Log logger = LogFactory.getLog(this.getClass());
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String registeredUrl;
    private Integer seriesId;
    private boolean emailNotification;
    private String processTimeLog;
    private String storageBucket;
    private String promsReportUrl;
    private String computeVmRunCommand;

    /**
     * max walltime for the job. 0 or null indicate that no walltime applies to the job
     */
    private Integer walltime;
    private boolean containsPersistentVolumes;

    /** Time when the job executes as opposed to when the job was submitted **/
    private Date executeDate;

    /** A map of VglParameter objects keyed by their parameter names*/
    @JsonIgnore
    @OneToMany(mappedBy = "job", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<VLJobSolution> jobSolutions;
    
    /** Set of job solution values, these are propagated from the VLJobSolution objects **/
    @JsonInclude
    @Transient
    private Set<VglParameter> jobParameters;
    
    /** A list of VglDownload objects associated with this job*/
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    private List<VglDownload> jobDownloads;

    /** A list of FileInformation objects associated with this job*/
    /*
    private List<FileInformation> jobFiles = new ArrayList<>();
    */

    /** A set of Solutions associated with this job */
    @ElementCollection
    @CollectionTable(name="job_solutions", joinColumns=@JoinColumn(name="job_id"))
    @Column(name="solution_id")
    private Set<String> solutionIds;
    
    public Set<String> getSolutionIds() {
        return solutionIds;
    }
    public void setSolutionIds(Set<String> solutionIds) {
        this.solutionIds = solutionIds;
    }
    
    public void addSolutionId(String solutionId) {
        this.solutionIds.add(solutionId);
    }

    /**
     * A set of annotations associated with this job.
     */
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="job_annotations", joinColumns=@JoinColumn(name="job_id"))
    @Column(name="value")
    private Set<String> annotations;
    
    /*
     * CloudJob parameters
     */
    /** Descriptive name of this job */
    protected String name;
    /** Long description of this job */
    protected String description;
    /** Email address of job submitter */
    protected String emailAddress;
    /** user name of job submitter */
    protected String user;
    /** date/time when this job was submitted */
    protected Date submitDate;
    /** date/time when this job was processed */
    protected Date processDate;
    /** descriptive status of this job */
    protected String status;

    /** the ID of the VM that will be used to run this job */
    protected String computeVmId;
    /** the ID of the VM instance that is running this job (will be null if no job is currently running) */
    protected String computeInstanceId;
    /** The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null */
    protected String computeInstanceType;
    /** The name of the key to inject into the instance at startup for root access. Can be null */
    protected String computeInstanceKey;
    /** The unique ID of the storage service this job has been using */
    protected String computeServiceId;

    /** The key prefix for all files associated with this job in the specified storage bucket */
    protected String storageBaseKey;
    /** The unique ID of the storage service this job has been using */
    protected String storageServiceId;

    transient protected Map<String, String> properties = new HashMap<String, String>();

    
    /**
     * Creates an unitialised VEGLJob
     */
    public VEGLJob() {
        super();
    }

    /**
     * 
     */
    public Integer getId() {
		return id;
	}

    /**
     * 
     */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
     * Sets the processTimeLog
     * @param String time
     */
    public void setProcessTimeLog(String processTimeLog) {
        this.processTimeLog=processTimeLog;

    }

    /**
     * @return the processTimeLog
     */
    public String getProcessTimeLog() {
        return processTimeLog;
    }

    /**
     * Gets where this job has been registered
     * @return
     */
    public String getRegisteredUrl() {
        return registeredUrl;
    }

    /**
     * Sets where this job has been registered
     * @param registeredUrl
     */
    public void setRegisteredUrl(String registeredUrl) {
        this.registeredUrl = registeredUrl;
    }

    /**
     * Gets the ID of the series this job belongs to
     * @return
     */
    public Integer getSeriesId() {
        return seriesId;
    }

    /**
     * Sets the ID of the series this job belongs to
     * @param seriesId
     */
    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    /**
     * Gets the email notification flag for this job
     * @return
     */
    public boolean getEmailNotification() {
        return emailNotification;
    }

    /**
     * Sets the email notification flag for this job
     * @param seriesId
     */
    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    /**
     * A list of VglDownload objects associated with this job
     * @return
     */
    public List<VglDownload> getJobDownloads() {
        return (jobDownloads != null) ? jobDownloads : new ArrayList<VglDownload>();
    }

    /**
     * A list of VglDownload objects associated with this job
     * @param jobDownloads
     */
    public void setJobDownloads(List<VglDownload> jobDownloads) {
        if (this.jobDownloads == null) {
            this.jobDownloads = new ArrayList<VglDownload>();
        } 
        this.jobDownloads.clear();
        if (jobDownloads != null) {
            for (VglDownload dl : jobDownloads) {
                dl.setParent(this);
                this.jobDownloads.add(dl);
            }
        }
    }

    public Set<String> getAnnotations() {
        return (annotations != null) ? annotations : new HashSet<String>();
    }

    public void setAnnotations(Collection<String> annotations) {
        if(this.annotations == null) {
            this.annotations = new HashSet<String>();
        } else {
            this.annotations.clear();
        }

        this.annotations.addAll(annotations);
    }
    
    public Set<VLJobSolution> getJobSolutions() {
        return (jobSolutions != null) ? jobSolutions : new HashSet<VLJobSolution>();
    }
    
    public void setJobSolutions(Set<VLJobSolution> jobSolutions) {
        if(this.jobSolutions == null) {
            this.jobSolutions = new HashSet<VLJobSolution>();
        } 
        this.jobSolutions.clear();
        if (jobSolutions != null) {
            for (VLJobSolution js: jobSolutions) {
                js.setJob(this);
                this.jobSolutions.add(js);
            }
        }
    }
    
    public void addJobSolution(VLJobSolution jobSolution) {
        if(this.jobSolutions == null) {
            this.jobSolutions = new HashSet<VLJobSolution>();
        }
        jobSolutions.add(jobSolution);
    }
    
    public Set<VglParameter> getJobParameters() {
        HashSet<VglParameter> jobParameters = new HashSet<VglParameter>();
        if(jobSolutions != null) {
            for (VLJobSolution solution: jobSolutions) {
                if(solution.getJobParameters() != null) {
                    for(VglParameter parameter: solution.getJobParameters()) {
                        jobParameters.add(parameter);
                    }
                }
            }
        }
        return jobParameters;
    }
    
    /**
     * Similar to clone but ensures compatibility with hibernate. No IDs or references (except for immutable ones)
     * will be shared by the clone and this object.
     * @return
     */
    public VEGLJob safeClone() {
        VEGLJob newJob = new VEGLJob();
        newJob.setComputeInstanceId(this.getComputeInstanceId());
        newJob.setComputeInstanceKey(this.getComputeInstanceKey());
        newJob.setComputeInstanceType(this.getComputeInstanceType());
        newJob.setComputeServiceId(this.getComputeServiceId());
        newJob.setComputeVmId(this.getComputeVmId());
        newJob.setComputeVmRunCommand(this.getComputeVmRunCommand());
        newJob.setDescription(this.getDescription());
        newJob.setEmailAddress(this.getEmailAddress());
        newJob.setName(this.getName());
        newJob.setRegisteredUrl(this.getRegisteredUrl());
        newJob.setSeriesId(this.getSeriesId());
        newJob.setStatus(this.getStatus()); //change the status
        newJob.setStorageServiceId(this.getStorageServiceId());
        newJob.setStorageBaseKey(this.getStorageBaseKey());
        newJob.setSubmitDate(this.getSubmitDate()); //this job isn't submitted yet
        newJob.setUser(this.getUser());
        newJob.setStorageBucket(this.getStorageBucket());
        newJob.setWalltime(this.getWalltime());
        newJob.setExecuteDate(this.getExecuteDate());
        newJob.setPromsReportUrl(this.getPromsReportUrl());
        newJob.setContainsPersistentVolumes(this.isContainsPersistentVolumes());

        List<VglDownload> newDownloads = new ArrayList<>();
        for (VglDownload dl : this.getJobDownloads()) {
            VglDownload dlClone = (VglDownload) dl.clone();
            dlClone.setId(null);
            newDownloads.add(dlClone);
        }
        newJob.setJobDownloads(newDownloads);

        for (String key : properties.keySet()) {
            newJob.setProperty(key, getProperty(key));
        }

        newJob.setSolutionIds(new HashSet<>(this.getSolutionIds()));
        
        // JobSolutionObjects (containing parameters)
        Set<VLJobSolution> newJobSolutionObjects = new HashSet<VLJobSolution>();
        for(VLJobSolution solution: jobSolutions) {
            newJobSolutionObjects.add((VLJobSolution)solution.clone());
        }
        newJob.setJobSolutions(newJobSolutionObjects);

        return newJob;
    }

    /**
     * The storage bucket name that will receive job artifacts (usually unique to user)
     */
    @Override
    public String getStorageBucket() {
        return storageBucket;
    }

    /**
     * The storage bucket name that will receive job artifacts (usually unique to user)
     * @param storageBucket
     */
    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    /**
     * The walltime in minutes.
     * @return Walltime in minutes or null if no walltime is set.
     */
    public Integer getWalltime() {
        return walltime;
    }

    public boolean isWalltimeSet() {
        return getWalltime()!=null && getWalltime()>0;
    }

    /**
     * Set the walltime in minutes
     * @param walltime
     */
    public void setWalltime(Integer walltime) {
        this.walltime = walltime;
    }
    
    public boolean isContainsPersistentVolumes() {
        return containsPersistentVolumes;
    }

    public void setContainsPersistentVolumes(boolean containsPersistentVolumes) {
        this.containsPersistentVolumes = containsPersistentVolumes;
    }

    /**
     * @return The date of job execution
     */
    public Date getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
    }

    /**
     * @return The URL of the associated PROMS Report
     */
    public String getPromsReportUrl() {
        return promsReportUrl;
    }

    public void setPromsReportUrl(String promsReportUrl) {
        this.promsReportUrl = promsReportUrl;
    }

    /**
     * The command that will be used to run the python run script. If null, most providers will use 'python'
     * @return
     */
    public String getComputeVmRunCommand() {
        return computeVmRunCommand;
    }

    /**
     * The command that will be used to run the python run script. If null, most providers will use 'python'
     * @param computeVmRunCommand
     */
    public void setComputeVmRunCommand(String computeVmRunCommand) {
        this.computeVmRunCommand = computeVmRunCommand;
    }

    @Override
    public String toString() {
        return "VEGLJob [registeredUrl=" + registeredUrl + ", seriesId="
                + seriesId + ", id=" + id + ", name=" + name + ", description="
                + description + "]";
    }
    
    public String setProperty(String key, String value) {
        if (value == null) {
            String oldValue = properties.get(key);
            properties.remove(key);
            return oldValue;
        }
        return properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Descriptive name of this job
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Descriptive name of this job
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Long description of this job
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Long description of this job
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Email address of job submitter
     *
     * @return
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Email address of job submitter
     *
     * @param emailAddress
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * user name of job submitter
     *
     * @return
     */
    @Override
    public String getUser() {
        return user;
    }

    /**
     * user name of job submitter
     *
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * date/time when this job was submitted
     *
     * @return
     */
    public Date getSubmitDate() {
        return submitDate;
    }

    /**
     * date/time when this job was submitted
     *
     * @param submitDate
     */
    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    /**
     * date/time when this job was processed
     *
     * @return
     */
    public Date getProcessDate() {
        return processDate;
    }

    /**
     * date/time when this job was processed
     *
     * @param processDate
     */
    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    /**
     * date/time when this job was submitted (expects a date in the format CloudJob.DATE_FORMAT)
     *
     * @param submitDate
     * @throws ParseException
     */
    public void setSubmitDate(String submitDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        this.setSubmitDate(sdf.parse(submitDate));
    }

    /**
     * descriptive status of this job
     *
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * descriptive status of this job
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * the ID of the VM that will be used to run this job
     *
     * @return
     */
    public String getComputeVmId() {
        return computeVmId;
    }

    /**
     * the ID of the VM that will be used to run this job
     *
     * @param computeVmId
     */
    public void setComputeVmId(String computeVmId) {
        this.computeVmId = computeVmId;
    }

    /**
     * the ID of the VM instance that is running this job (will be null if no job is currently running)
     *
     * @return
     */
    public String getComputeInstanceId() {
        return computeInstanceId;
    }

    /**
     * the ID of the VM instance that is running this job (will be null if no job is currently running)
     *
     * @param computeInstanceId
     */
    public void setComputeInstanceId(String computeInstanceId) {
        this.computeInstanceId = computeInstanceId;
    }

    /**
     * The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null
     */
    public String getComputeInstanceType() {
        return computeInstanceType;
    }

    /**
     * The type of the compute instance to start (size of memory, number of CPUs etc) - eg m1.large. Can be null
     */
    public void setComputeInstanceType(String computeInstanceType) {
        this.computeInstanceType = computeInstanceType;
    }

    /**
     * The name of the key to inject into the instance at startup for root access. Can be null
     */
    public String getComputeInstanceKey() {
        return computeInstanceKey;
    }

    /**
     * The name of the key to inject into the instance at startup for root access. Can be null
     */
    public void setComputeInstanceKey(String computeInstanceKey) {
        this.computeInstanceKey = computeInstanceKey;
    }

    /**
     * The unique ID of the compute service this job has been using
     *
     * @return
     */
    public String getComputeServiceId() {
        return computeServiceId;
    }

    /**
     * The unique ID of the compute service this job has been using
     *
     * @param computeServiceId
     */
    public void setComputeServiceId(String computeServiceId) {
        this.computeServiceId = computeServiceId;
    }

    /**
     * The unique ID of the storage service this job has been using
     *
     * @return
     */
    public String getStorageServiceId() {
        return storageServiceId;
    }

    /**
     * The unique ID of the storage service this job has been using
     *
     * @param storageServiceId
     */
    public void setStorageServiceId(String storageServiceId) {
        this.storageServiceId = storageServiceId;
    }

    /**
     * The key prefix for all files associated with this job in the specified storage bucket
     *
     * @return
     */
    @Override
    public String getStorageBaseKey() {
        return storageBaseKey;
    }

    /**
     * The key prefix for all files associated with this job in the specified storage bucket
     *
     * @param storageBaseKey
     */
    @Override
    public void setStorageBaseKey(String storageBaseKey) {
        this.storageBaseKey = storageBaseKey;
    }

}
