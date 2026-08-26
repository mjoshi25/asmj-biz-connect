package com.joshi.twitterclone.repository.jobs;

import com.joshi.twitterclone.model.jobs.JobApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends MongoRepository<JobApplication, String> {
    List<JobApplication> findByJobIdOrderByAppliedAtDesc(String jobId);
    List<JobApplication> findByApplicantUsernameOrderByAppliedAtDesc(String applicantUsername);
    List<JobApplication> findByPosterUsernameOrderByAppliedAtDesc(String posterUsername);
    Optional<JobApplication> findByJobIdAndApplicantUsername(String jobId, String applicantUsername);
}