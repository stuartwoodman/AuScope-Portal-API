package org.auscope.portal.server.vegl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;



/**
 * A Job Parameter is a single 'typed' (very loosely) key/value pairing.
 *
 * A typical Job will have one or more parameter values created as the job is constructed. The parameter
 * set is made available to any job scripts that get run
 *
 * @author Josh Vote
 *
 */
@Entity
@Table(name="parameters")
public class VglParameter implements Serializable, Cloneable {

    private static final long serialVersionUID = -7474027234400180238L;


    /** The primary key for this parameter*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /** The name of this parameter*/
    private String name;
    
    /** The value (as a string) of this parameter*/
    private String value;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_solutions_id")
    private VLJobSolution jobSolution;
    
    @JsonInclude
    @Transient
    private String solution;
    
    
    /**
     * Default constructor
     */
    public VglParameter() {
        this(null, null);
    }

    /**
     * Default constructor
     */
    public VglParameter(String name, String value) {
        this(null, name, value, null);
    }

    public VglParameter(String name, String value, VLJobSolution jobSolution) {
        this(null, name, value, jobSolution);
    }

    /**
     * Construct a fully populated instance
     */
    public VglParameter(Integer id, String name, String value, VLJobSolution jobSolution) {
        super();
        this.id = id;
        this.name = name;
        this.value = value;
        this.jobSolution = jobSolution;
    }

    /**
     * The primary key for this parameter
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * The primary key for this parameter
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The name of this parameter
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The name of this parameter
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value (as a string) of this parameter
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * The value (as a string) of this parameter
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The VLJobSolution that owns this parameter
     * 
     * @return
     */
    public VLJobSolution getJobSolution() {
        return jobSolution;
    }
    
    public void setJobSolution(VLJobSolution jobSolution) {
        this.jobSolution = jobSolution;
    }
    
    public String getSolution() {
        return jobSolution.getSolutionId();
    }
    

    /**
     * Tests two VglJobParameter objects for equality based on job id and name
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof VglParameter) {
            return jobSolution.getId().equals(((VglParameter) o).jobSolution.getId()) &&
                    this.name.equals(((VglParameter) o).name) &&
                    this.value.equals(((VglParameter) o).value);
        }
        return false;
    }

    @Override
    public Object clone() {
        VglParameter newParameter = new VglParameter();
        newParameter.setName(name);
        newParameter.setValue(value);
        newParameter.setJobSolution(this.jobSolution);
        return newParameter;
    }
}
