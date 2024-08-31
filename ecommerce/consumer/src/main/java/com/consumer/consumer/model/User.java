package com.consumer.consumer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonIgnoreProperties("owner")
	private Store store;


	public Store getStore() {
			return store;
	}

	public void setStore(Store store) {
			this.store = store;
			if (store != null) {
					store.setOwner(this);
			}
	}
	
	@Column(name = "name")
	private String name;

	@Column(name = "gender")
	private String gender;
	
	@Column(name = "age")
	private int age;
	
	public User() {
		
	}
	
	public User(String name, String gender, int age) {
		super();
		this.name = name;
		this.gender = gender;
		this.age = age;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return this.name;
	}

	public String getGender() {
		return this.gender;
	}

	public int getAge() {
		return this.age;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}



	public void setAge(int age) {
		this.age = age;
	}
}