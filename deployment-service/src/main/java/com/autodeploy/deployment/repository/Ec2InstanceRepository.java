package com.autodeploy.deployment.repository;

import com.autodeploy.deployment.entity.Ec2Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface Ec2InstanceRepository extends JpaRepository<Ec2Instance, UUID> {

    Optional<Ec2Instance> findByInstanceId(String instanceId);

    Optional<Ec2Instance> findFirstByStatusOrderByActiveDeploymentsAsc(String status);
}
