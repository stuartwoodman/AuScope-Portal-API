package org.auscope.portal.server.vegl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Represents a Solution owned by a VLJob. Each solution will contain a list of
 * VLParameters that contain the values of the Solution variables.
 *   
 * @author woo392
 *
 */
@Entity
@Table(name = "job_solutions")
public class VLJobSolution implements Serializable, Cloneable {

    private static final long serialVersionUID = 2812589770285896093L;

    // Auto-incrementing primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // The parent VLJob
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private VEGLJob job;
    
    // The associated Solution
    @Column(name = "solution_id")
    private String solutionId;
    
    // The Solution variable values 
    @OneToMany(mappedBy = "jobSolution", fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<VglParameter> jobParameters;
    
    public VLJobSolution() {
        this(null, null, null);
    }
    
    public VLJobSolution(VEGLJob job, String solutionId) {
        this(null, job, solutionId);
    }
    
    public VLJobSolution(Integer id, VEGLJob job, String solutionId) {
        this(null, job, solutionId, null);
    }
    
    public VLJobSolution(Integer id, VEGLJob job, String solutionId, Set<VglParameter> jobParameters) {
        super();
        this.id = id;
        this.job = job;
        this.solutionId = solutionId;
        this.jobParameters = jobParameters;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public VEGLJob getJob() {
        return job;
    }
    
    public void setJob(VEGLJob job) {
        this.job = job;
    }
    
    public String getSolutionId() {
        return solutionId;
    }
    
    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }
    
    public Set<VglParameter> getJobParameters() {
        return (jobParameters != null) ? jobParameters : new HashSet<VglParameter>();
    }
    
    public void setJobParameters(Set<VglParameter> jobParameters) {
        if(this.jobParameters == null) {
            this.jobParameters = new HashSet<VglParameter>();
        } 
        this.jobParameters.clear();
        if (jobParameters != null) {
            for (VglParameter p: jobParameters) {
                p.setJobSolution(this);
                this.jobParameters.add(p);
            }
        }
    }
    
    public void addJobParameter(VglParameter parameter) {
        if(this.jobParameters == null) {
            this.jobParameters = new HashSet<VglParameter>();
        }
        jobParameters.add(parameter);
    }
    
    @Override
    public Object clone() {
        VLJobSolution newJobSolution = new VLJobSolution();
        newJobSolution.setJob(getJob());
        newJobSolution.setSolutionId(getSolutionId());
        Set<VglParameter> newJobParameters = new HashSet<VglParameter>();
        for(VglParameter parameter: jobParameters) {
            newJobParameters.add((VglParameter)parameter.clone());
        }
        newJobSolution.setJobParameters(newJobParameters);
        return newJobSolution;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VLJobSolution)) {
            return false;
        }
        return this.solutionId.equals(((VLJobSolution)obj).solutionId) && this.job.getId().equals(((VLJobSolution)obj).job.getId());
    }

}
