package com.joshi.twitterclone.repository.jobs;

import com.joshi.twitterclone.model.jobs.JobListing;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobListingRepository extends MongoRepository<JobListing, String> {
    List<JobListing> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<JobListing> findByPosterUsernameOrderByCreatedAtDesc(String posterUsername);
    List<JobListing> findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus status, String city);
}