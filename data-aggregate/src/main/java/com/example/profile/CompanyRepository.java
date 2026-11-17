package com.example.profile;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {
}
