package com.example.demo.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.domain.User;

@Mapper
public interface UserRepository {

	int insert(User user);

	Optional<User> findByUserId(@Param("userId") String userId);

	int update(User user);

	int deleteByUserId(@Param("userId") String userId);
}
