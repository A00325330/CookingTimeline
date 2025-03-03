package com.tus.group_project.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tus.group_project.model.UE;


@Repository
public interface UERepository extends CrudRepository<UE, Integer>{

}
