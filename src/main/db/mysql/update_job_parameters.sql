# Update the job_parameters table to reference the new job_solutions table.
# NOTE: Only necessary for existing databases that weren't created with the current anvgl.sql script. 

# Add new ID column to job_solutions
ALTER TABLE job_solutions ADD COLUMN `id` int;

# Populate column with ascending integers
SET @id_increment := 0;
UPDATE job_solutions
SET id = @id_increment := @id_increment + 1;

# Add primary key, not null and auto-increment
ALTER TABLE job_solutions ADD PRIMARY KEY (id);
ALTER TABLE job_solutions MODIFY COLUMN `id` int NOT NULL AUTO_INCREMENT;

# Backup parameters table (command line)
#mysqldump db_name parameters > vgl_parameters.sql

# Wipe parameters table
DELETE FROM parameters;

# Drop type and jobId columns (and job constraint)
ALTER TABLE parameters DROP COLUMN type;
ALTER TABLE parameters DROP FOREIGN KEY `parameters_ibfk_1`;
ALTER TABLE parameters DROP COLUMN jobId;

# Add job_solutions ID and FK constraint to parameters
ALTER TABLE parameters ADD COLUMN job_solutions_id int NOT NULL;
ALTER TABLE parameters ADD CONSTRAINT `parameters_ibfk_1` FOREIGN KEY (`job_solutions_id`) REFERENCES `job_solutions` (`id`) ON DELETE CASCADE;
