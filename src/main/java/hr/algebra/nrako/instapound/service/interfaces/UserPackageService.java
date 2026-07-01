package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.enums.PackageType;
import hr.algebra.nrako.instapound.model.dto.response.PackageInfoResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;

import java.util.List;

public interface UserPackageService {
    List<PackageInfoResponse> getAllPackages();
    UserPackage getPackageByType(PackageType packageType);
    PackageInfoResponse getPackageInfoForUser(User user);
    boolean requestPackageChange(User user, PackageType newPackage);
    boolean canChangePackage(User user);
    void processPendingPackageChanges();
    UserPackage save(UserPackage userPackage);
}
