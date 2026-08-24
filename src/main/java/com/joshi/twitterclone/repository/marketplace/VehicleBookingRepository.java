package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.VehicleBooking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleBookingRepository extends MongoRepository<VehicleBooking, String> {
    List<VehicleBooking> findByRenterUsernameOrderByBookedAtDesc(String renterUsername);
    List<VehicleBooking> findByOwnerUsernameOrderByBookedAtDesc(String ownerUsername);
}