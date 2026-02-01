package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.dto.PackageChangeRequest;
import hr.algebra.nrako.instapound.model.dto.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;

import java.util.List;

/**
 * User Package Service Interface
 * Manages user packages and package changes
 */
public interface UserPackageService {

    /**
     * Get all available packages
     */
    List<PackageInfoResponse> getAllPackages();

    /**
     * Get package by type
     */
    UserPackage getPackageByType(PackageType packageType);

    /**
     * Get package info for a user
     */
    PackageInfoResponse getPackageInfoForUser(User user);

    /**
     * Request a package change (effective next day)
     */
    boolean requestPackageChange(User user, PackageType newPackage);

    /**
     * Check if user can change package today
     */
    boolean canChangePackage(User user);

    /**
     * Process pending package changes (scheduled task)
     */
    void processPendingPackageChanges();

    /**
     * Create or update a package (admin only)
     */
    UserPackage savePackage(UserPackage userPackage);
}
