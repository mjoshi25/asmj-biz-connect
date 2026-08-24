package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.InsuranceQuote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceQuoteRepository extends MongoRepository<InsuranceQuote, String> {
    List<InsuranceQuote> findByApplicantUsernameOrderByRequestedAtDesc(String applicantUsername);
    List<InsuranceQuote> findByInsurerUsernameOrderByRequestedAtDesc(String insurerUsername);
}